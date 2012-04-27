/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.taobao.tddl.interact.bean.ComparativeMapChoicer;
import com.taobao.tddl.interact.bean.Field;
import com.taobao.tddl.interact.bean.MatcherResult;
import com.taobao.tddl.interact.bean.MatcherResultImp;
import com.taobao.tddl.interact.bean.TargetDB;
import com.taobao.tddl.interact.rule.Rule.RuleColumn;
import com.taobao.tddl.interact.rule.Samples.SamplesCtx;
import com.taobao.tddl.interact.rule.bean.AdvancedParameter;
import com.taobao.tddl.interact.rule.ruleimpl.VirtualNodeGroovyRule;
import com.taobao.tddl.interact.rule.util.RuleUtils;
import com.taobao.tddl.interact.sqljep.Comparative;

/**
 * 1.在分库列和分表列没有交集的情况下，各自计算，结果做笛卡尔组合。
 * 
 * 2.在分库列和分表列有交集，公共列的附加参数完全相同的情况下，公共列库表规则的描点总是相同的。
 *   表的描点要经过库规则计算后按库分类，在每个库内，只对产生该库的描点值进行表规则计算来确定表
 *   否则可能会产生不在本库的表。例如库%2表%8：
 *       id%2=0--db0：t0,t2,t4,t6
 *       id%2=1--db1: t1,t3,t5,t7
 *   表的描点0-7需要先经过库规则计算，按库结果分成两类db0:0,2,4,5和db1:1,3,5,7再分表进行表规则计算
 * 
 * 3.在分库列和分表列 列名相同 类型不同 的情况下，各自描点取并集，再按2的方式计算。比如库1_month 表1_day，
 *   单月db0，双月db1；一天一张表。则time in（2月5日 3月8日 4月20日）正确结果应该是：
 *       db0: t_5,t_20
 *       db1: t_8
 *   上述例子是描点相同的情况。对于描点不同的情况例如 5月31日<=time<=6月2日。
 *   库的描点是5月31日，6月1日共2个；表是5月31日、6月1日、6月2日共3个，正确的结果应该是：
 *       db0: t_1,t_2
 *       db1: t_31
 *   上述列子是表的描点多于库，同时包含了库的描点。表的描点必须经过库规则分类再计算。
 *   对于表的描点不包含全部库描点的情况，例如：库描点a,b,c表描点b,c,d ....
 *   
 * 4.不考虑库表规则的公共列（参数）在类型相同的情况下，迭代次数的不同。
 *   列名和类型相同，配不同的迭代次数是意义不大的，例如库%2表%8，计算库的时候2个描点，计算表需要8个描点，
 *   但是由于是同一个列，表的8个描点需要全部走一遍库规则，进行按库分离聚合再计算表，才能保证结果的正确：
 *   如果库的迭代次数大于表的，例如库id%8 表id%3, 统一按库的描点表规则会多计算几次
 *       DB0:t0,t1,t2; DB1:t0,t1,t2
 *   忽略这种情况，所以公共列（参数）在类型相同时，要求应用按最大迭代次数配成一致。
 *    
 * @author linxuan
 *
 */
public class VirtualTableRuleMatcher {

	private static final boolean isCrossInto;
	static {
		//提供一个可配置的机会，但是在绝大多数默认的情况下，不想影响接口层次和代码结构
		String str = System.getProperty("com.taobao.tddl.rule.isCrossIntoCalculate", "false");
		isCrossInto = Boolean.parseBoolean(str);
	}
	
	/**
	 * @param comparativeMapChoicer
	 * @param args sql的参数列表
	 * @param rule sql中虚拟表名对应的规则
	 * @return
	 */
	public MatcherResult match(boolean needSourceKey,ComparativeMapChoicer choicer, List<Object> args, VirtualTableRule<String, String> rule) {
		if (needSourceKey) {
	        //暂时不支持虚拟节点
			return matchWithSourceKey(choicer, args, rule);
		} else {
			return matchNoSourceKey(choicer, args, rule);
		}
	}

	private MatcherResult matchNoSourceKey(ComparativeMapChoicer choicer, List<Object> args,
			VirtualTableRule<String, String> rule) {
		//所有库表规则中用到的列和对应的比较树,作为一个规则链匹配时绑定参数的缓存。
		//一开始就把所有的取出来不够优化。可能有些是不需要取的。也就省了些绑定参数的操作
		Map<String, Comparative> allRuleArgs = new HashMap<String, Comparative>(2);
		Map<String, Comparative> dbRuleArgs = new HashMap<String, Comparative>(2); //匹配的规则所对应的列名和比较树
		Map<String, Comparative> tbRuleArgs = new HashMap<String, Comparative>(2); //匹配的规则所对应的列名和比较树
		Object outerCtx = rule.getOuterContext();
		
		//竟然不需要defaultDbValue和defaultTbValue了
		Rule<String> dbRule = findMatchedRule(allRuleArgs, rule.getDbShardRules(), dbRuleArgs, choicer, args, rule);
		Rule<String> tbRule = findMatchedRule(allRuleArgs, rule.getTbShardRules(), tbRuleArgs, choicer, args, rule);

		Map<String, Set<String>> topology;
		if (dbRule == null && tbRule == null) {
			//若无库规则，静态拓扑里面只有一个库，则无论没表规则也好，全表扫也好，结果都和静态拓扑一样
			//若有库规则，那么是全库扫，则无论没表规则也好，全表扫也好，结果仍然和静态拓扑一样！！
			topology = rule.getActualTopology(); //不猜不知道，世界真奇妙
		} else if (dbRule == null) {
			Set<String> tbValues = tbRule.calculateNoTrace(tbRuleArgs, null, outerCtx);
			//1.无库规则 2.有但是没匹配到 ；这时表规则一定不为空，且和库规则全无关联，自己算自己的
			topology = new HashMap<String, Set<String>>(rule.getActualTopology().size());
			for (String dbValue : rule.getActualTopology().keySet()) {
				topology.put(dbValue, tbValues);
			}
		} else if (tbRule == null) {
		    //dbRule is VirtualNodeGroovyRule, just do the FullTableScan;
			if(dbRule instanceof VirtualNodeGroovyRule){
				topology = rule.getActualTopology();
			}else{
				//1.无表规则 2.有但是没匹配到 ；这时库规则一定不为空，且和表规则全无关联，自己算自己的
				Set<String> dbValues = dbRule.calculateNoTrace(dbRuleArgs, null, outerCtx);
				topology = new HashMap<String, Set<String>>(dbValues.size());
				for (String dbValue : dbValues) {
					topology.put(dbValue, rule.getActualTopology().get(dbValue));
				}
			}
		} else {
			//看表规则和库规则的列有无交集
			Set<String> commonSet = getCommonColumnSet(dbRule, tbRule);
			String[] commonColumn = commonSet == null ? null : commonSet.toArray(new String[commonSet.size()]);
			if (commonColumn == null || commonColumn.length == 0) {
				//无交集
				//modify by junyu,2011-9-23,增加虚拟节点
				Set<String> tbValues = tbRule.calculateNoTrace(tbRuleArgs, null, outerCtx);
				Set<String> dbValues = null;
				if(dbRule instanceof VirtualNodeGroovyRule){
					//说明dbRule是虚拟映射
					topology = new HashMap<String, Set<String>>();
					for(String tab:tbValues){
					    String db = dbRule.calculateVnodeNoTrace(tab, null, outerCtx);
					    if(!topology.containsKey(db)){
					    	Set<String> tbSet=new HashSet<String>();
					    	tbSet.add(tab);
					    	topology.put(db, tbSet);
					    }else{
					    	topology.get(db).add(tab);
					    }
					}
				}else{
					dbValues = dbRule.calculateNoTrace(dbRuleArgs, null, outerCtx);
					topology = new HashMap<String, Set<String>>(dbValues.size());
					for (String dbValue : dbValues) {
						topology.put(dbValue, tbValues);
					}
				}
			} else {
				//有交集
				if (!isCrossInto) {
					topology = crossNoSourceKey1(dbRule, dbRuleArgs, tbRule, tbRuleArgs, commonColumn, outerCtx);
				} else {
					topology = crossNoSourceKey2(dbRule, dbRuleArgs, tbRule, tbRuleArgs, commonSet, outerCtx);
				}
			}
		}
		
		return new MatcherResultImp(buildTargetDbList(topology), dbRuleArgs, tbRuleArgs);
	}
	
	private MatcherResult matchWithSourceKey(ComparativeMapChoicer choicer, List<Object> args,
			VirtualTableRule<String, String> rule) {
		//所有库表规则中用到的列和对应的比较树,作为一个规则链匹配时绑定参数的缓存。
		//一开始就把所有的取出来不够优化。可能有些是不需要取的。也就省了些绑定参数的操作
		Map<String, Comparative> allRuleArgs = new HashMap<String, Comparative>(2);
		Map<String, Comparative> dbRuleArgs = new HashMap<String, Comparative>(2); //匹配的规则所对应的列名和比较树
		Map<String, Comparative> tbRuleArgs = new HashMap<String, Comparative>(2); //匹配的规则所对应的列名和比较树
		Object outerCtx = rule.getOuterContext();

		//竟然不需要defaultDbValue和defaultTbValue了
		Rule<String> dbRule = findMatchedRule(allRuleArgs, rule.getDbShardRules(), dbRuleArgs, choicer, args, rule);
		Rule<String> tbRule = findMatchedRule(allRuleArgs, rule.getTbShardRules(), tbRuleArgs, choicer, args, rule);

		Map<String, Map<String, Field>> topology;
		if (dbRule == null && tbRule == null) {
			//若无库规则，静态拓扑里面只有一个库，则无论没表规则也好，全表扫也好，结果都和静态拓扑一样
			//若有库规则，那么是全库扫，则无论没表规则也好，全表扫也好，结果仍然和静态拓扑一样！！
			topology = new HashMap<String, Map<String, Field>>(rule.getActualTopology().size());
			for (Map.Entry<String, Set<String>> e : rule.getActualTopology().entrySet()) {
				topology.put(e.getKey(), toMapField(e.getValue()));
			}
		} else if (dbRule == null) {
			//1.无库规则 2.有但是没匹配到 ；这时表规则一定不为空，且和库规则全无关联，自己算自己的
			Map<String, Samples> tbValues = cast(tbRule.calculate(tbRuleArgs, null, outerCtx));
			topology = new HashMap<String, Map<String, Field>>(rule.getActualTopology().size());
			for (String dbValue : rule.getActualTopology().keySet()) {
				topology.put(dbValue, toMapField(tbValues));
			}
		} else if (tbRule == null) {
			//if dbRule is VirtualNodeGroovyRule,just do the fullTableScan
			if(dbRule instanceof VirtualNodeGroovyRule){
				topology = new HashMap<String, Map<String, Field>>(rule.getActualTopology().size());
				for (Map.Entry<String, Set<String>> e : rule.getActualTopology().entrySet()) {
					topology.put(e.getKey(), toMapField(e.getValue()));
				}
			}else{
				//1.无表规则 2.有但是没匹配到 ；这时库规则一定不为空，且和表规则全无关联，自己算自己的
				Set<String> dbValues = dbRule.calculateNoTrace(dbRuleArgs, null, outerCtx);
				topology = new HashMap<String, Map<String, Field>>(dbValues.size());
				for (String dbValue : dbValues) {
					topology.put(dbValue, toMapField(rule.getActualTopology().get(dbValue)));
				}
			}
		} else {
			//看表规则和库规则的列有无交集
			Set<String> commonSet = getCommonColumnSet(dbRule, tbRule);
			String[] commonColumn = commonSet == null ? null : commonSet.toArray(new String[commonSet.size()]);
			if (commonColumn == null || commonColumn.length == 0) {
				//无交集
				//modify by junyu,2011-10-24,原本没有加这个，需要测试下id in group 优化的部分
				if(dbRule instanceof VirtualNodeGroovyRule){
					Map<String,Samples> tbValues =cast(tbRule.calculate(tbRuleArgs, null, outerCtx));
					//说明dbRule是虚拟映射
					topology = new HashMap<String, Map<String,Field>>();
					Map<String,Map<String,Samples>> templogy=new HashMap<String,Map<String,Samples>>();
					for(Map.Entry<String,Samples> entry:tbValues.entrySet()){
					    String db = dbRule.calculateVnodeNoTrace(entry.getKey(), null, outerCtx);
					    if(!topology.containsKey(db)){
					    	Map<String,Samples> tbSet=new HashMap<String,Samples>();
					    	tbSet.put(entry.getKey(), entry.getValue());
					    	templogy.put(db, tbSet);
					    }else{
					    	templogy.get(db).put(entry.getKey(), entry.getValue());
					    }
					}
					
					for(Map.Entry<String,Map<String,Samples>> entry:templogy.entrySet()){
						topology.put(entry.getKey(), toMapField(entry.getValue()));
					}
				}else{
					Set<String> dbValues = dbRule.calculateNoTrace(dbRuleArgs, null, outerCtx);
					Map<String, Samples> tbValues = cast(tbRule.calculate(tbRuleArgs, null, outerCtx));
					topology = new HashMap<String, Map<String, Field>>(dbValues.size());
					for (String dbValue : dbValues) {
						topology.put(dbValue, toMapField(tbValues));
					}
				}
			} else { //有交集
				if (!isCrossInto) {
					topology = crossWithSourceKey1(dbRule, dbRuleArgs, tbRule, tbRuleArgs, commonColumn, outerCtx);
				} else {
					topology = crossWithSourceKey2(dbRule, dbRuleArgs, tbRule, tbRuleArgs, commonSet, outerCtx);
				}
			}
		}
		
		return new MatcherResultImp(buildTargetDbListWithSourceKey(topology), dbRuleArgs, tbRuleArgs);
	}

	private Map<String, Set<String>> crossNoSourceKey1(Rule<String> matchedDbRule,
			Map<String, Comparative> matchedDbRuleArgs, Rule<String> matchedTbRule,
			Map<String, Comparative> matchedTbRuleArgs, String[] commonColumn, Object outerCtx) {
		SamplesCtx dbRuleCtx = null; //对于表规则中与库规则列名相同而自增类型不同的列，将其表枚举结果加入库规则的枚举集
		Set<AdvancedParameter> diifTypeInCommon = diifTypeInCommon(matchedDbRule, matchedTbRule, commonColumn);
		if (diifTypeInCommon != null && !diifTypeInCommon.isEmpty()) {
			//公共列包含有枚举类型不同的列，例如库是1_month，表示1_day
			Map<String, Set<Object>> tbTypes = RuleUtils.getSamplingField(matchedTbRuleArgs, diifTypeInCommon);
			dbRuleCtx = new SamplesCtx(new Samples(tbTypes), SamplesCtx.merge);
		}
		Map<String, Samples> dbValues = cast(matchedDbRule.calculate(matchedDbRuleArgs, dbRuleCtx, outerCtx));
		Map<String, Set<String>> topology = new HashMap<String, Set<String>>(dbValues.size());
		for (Map.Entry<String, Samples> e : dbValues.entrySet()) {
			SamplesCtx tbRuleCtx = new SamplesCtx(e.getValue().subSamples(commonColumn), SamplesCtx.replace);
			Set<String> tbValues = matchedTbRule.calculateNoTrace(matchedTbRuleArgs, tbRuleCtx, outerCtx);
			topology.put(e.getKey(), tbValues);
		}
		return topology;
	}

	private Map<String, Map<String, Field>> crossWithSourceKey1(Rule<String> matchedDbRule,
			Map<String, Comparative> matchedDbRuleArgs, Rule<String> matchedTbRule,
			Map<String, Comparative> matchedTbRuleArgs, String[] commonColumn, Object outerCtx) {
		SamplesCtx dbRuleCtx = null; //对于表规则中与库规则列名相同而自增类型不同的列，将其表枚举结果加入库规则的枚举集
		Set<AdvancedParameter> diifTypeInCommon = diifTypeInCommon(matchedDbRule, matchedTbRule, commonColumn);
		if (diifTypeInCommon != null && !diifTypeInCommon.isEmpty()) {
			//公共列包含有枚举类型不同的列，例如库是1_month，表示1_day
			Map<String, Set<Object>> tbTypes = RuleUtils.getSamplingField(matchedTbRuleArgs, diifTypeInCommon);
			dbRuleCtx = new SamplesCtx(new Samples(tbTypes), SamplesCtx.merge);
		}
		Map<String, Samples> dbValues = cast(matchedDbRule.calculate(matchedDbRuleArgs, dbRuleCtx, outerCtx));
		Map<String, Map<String, Field>> topology = new HashMap<String, Map<String, Field>>(dbValues.size());
		for (Map.Entry<String, Samples> e : dbValues.entrySet()) {
			SamplesCtx tbRuleCtx = new SamplesCtx(e.getValue().subSamples(commonColumn), SamplesCtx.replace);
			Map<String, Samples> tbValues = cast(matchedTbRule.calculate(matchedTbRuleArgs, tbRuleCtx, outerCtx));
			topology.put(e.getKey(), toMapField(tbValues));
		}
		return topology;
	}

	private Map<String, Set<String>> crossNoSourceKey2(Rule<String> matchedDbRule,
			Map<String, Comparative> matchedDbRuleArgs, Rule<String> matchedTbRule,
			Map<String, Comparative> matchedTbRuleArgs, Set<String> commonSet, Object outerCtx) {
		//有交集
		String[] commonColumn = commonSet == null ? null : commonSet.toArray(new String[commonSet.size()]);
		Set<AdvancedParameter> dbParams = cast(matchedDbRule.getRuleColumnSet());
		Set<AdvancedParameter> tbParams = cast(matchedTbRule.getRuleColumnSet());
		Map<String, Set<Object>> dbEnumerates = RuleUtils.getSamplingField(matchedDbRuleArgs, dbParams);
		Set<AdvancedParameter> diifTypeInCommon = diifTypeInCommon(matchedDbRule, matchedTbRule, commonColumn);
		if (diifTypeInCommon != null && !diifTypeInCommon.isEmpty()) {
			//将自增类型不同的公共列的表枚举值加入库枚举值中
			Map<String, Set<Object>> diifTypeTbEnumerates = RuleUtils.getSamplingField(matchedTbRuleArgs,
					diifTypeInCommon);
			for (Map.Entry<String, Set<Object>> e : diifTypeTbEnumerates.entrySet()) {
				dbEnumerates.get(e.getKey()).addAll(e.getValue());
			}
		}
		Set<AdvancedParameter> tbOnly = new HashSet<AdvancedParameter>();
		for (AdvancedParameter param : tbParams) {
			if (!commonSet.contains(param.key)) {
				tbOnly.add(param);
			}
		}

		Map<String, Set<String>> topology = new HashMap<String, Set<String>>();
		if (tbOnly.isEmpty()) {
			//分库列完全包含了分表列
			for (Map<String, Object> dbSample : new Samples(dbEnumerates)) { //遍历笛卡尔抽样
				String dbIndex = matchedDbRule.eval(dbSample, outerCtx);
				String tbName = matchedTbRule.eval(dbSample, outerCtx);
				addToTopology(dbIndex, tbName, topology);
			}
		} else {
			Map<String, Set<Object>> tbEnumerates = RuleUtils.getSamplingField(matchedTbRuleArgs, tbOnly);//只有表的枚举
			Samples tbSamples = new Samples(tbEnumerates);
			for (Map<String, Object> dbSample : new Samples(dbEnumerates)) { //遍历库笛卡尔抽样
				String dbIndex = matchedDbRule.eval(dbSample, outerCtx);
				for (Map<String, Object> tbSample : tbSamples) { //遍历表中单独列的笛卡尔抽样
					dbSample.putAll(tbSample);
					String tbName = matchedTbRule.eval(dbSample, outerCtx);
					addToTopology(dbIndex, tbName, topology);
				}
			}
		}
		return topology;
	}

	private Map<String, Map<String, Field>> crossWithSourceKey2(Rule<String> matchedDbRule,
			Map<String, Comparative> matchedDbRuleArgs, Rule<String> matchedTbRule,
			Map<String, Comparative> matchedTbRuleArgs, Set<String> commonSet, Object outerCtx) {
		//有交集
		String[] commonColumn = commonSet == null ? null : commonSet.toArray(new String[commonSet.size()]);
		Set<AdvancedParameter> dbParams = cast(matchedDbRule.getRuleColumnSet());
		Set<AdvancedParameter> tbParams = cast(matchedTbRule.getRuleColumnSet());
		Map<String, Set<Object>> dbEnumerates = RuleUtils.getSamplingField(matchedDbRuleArgs, dbParams);
		Set<AdvancedParameter> diifTypeInCommon = diifTypeInCommon(matchedDbRule, matchedTbRule, commonColumn);
		if (diifTypeInCommon != null && !diifTypeInCommon.isEmpty()) {
			//将自增类型不同的公共列的表枚举值加入库枚举值中
			Map<String, Set<Object>> diifTypeTbEnumerates = RuleUtils.getSamplingField(matchedTbRuleArgs,
					diifTypeInCommon);
			for (Map.Entry<String, Set<Object>> e : diifTypeTbEnumerates.entrySet()) {
				dbEnumerates.get(e.getKey()).addAll(e.getValue());
			}
		}
		Set<AdvancedParameter> tbOnly = new HashSet<AdvancedParameter>();
		for (AdvancedParameter param : tbParams) {
			if (!commonSet.contains(param.key)) {
				tbOnly.add(param);
			}
		}

		Map<String, Map<String, Field>> topology = new HashMap<String, Map<String, Field>>();
		if (tbOnly.isEmpty()) {
			//分库列完全包含了分表列
			for (Map<String, Object> dbSample : new Samples(dbEnumerates)) { //遍历笛卡尔抽样
				String dbIndex = matchedDbRule.eval(dbSample, outerCtx);
				String tbName = matchedTbRule.eval(dbSample, outerCtx);
				addToTopologyWithSource(dbIndex, tbName, topology, dbSample, tbParams);
			}
		} else {
			Map<String, Set<Object>> tbEnumerates = RuleUtils.getSamplingField(matchedTbRuleArgs, tbOnly);//只有表的枚举
			Samples tbSamples = new Samples(tbEnumerates);
			for (Map<String, Object> dbSample : new Samples(dbEnumerates)) { //遍历库笛卡尔抽样
				String dbIndex = matchedDbRule.eval(dbSample, outerCtx);
				for (Map<String, Object> tbSample : tbSamples) { //遍历表中单独列的笛卡尔抽样
					dbSample.putAll(tbSample);
					String tbName = matchedTbRule.eval(dbSample, outerCtx);
					addToTopologyWithSource(dbIndex, tbName, topology, dbSample, tbParams);
				}
			}
		}
		return topology;
	}

	private static void addToTopology(String dbIndex, String tbName, Map<String, Set<String>> topology) {
		Set<String> tbNames = topology.get(dbIndex);
		if (tbNames == null) {
			tbNames = new HashSet<String>();
			topology.put(dbIndex, tbNames);
		}
		tbNames.add(tbName);
	}

	private static void addToTopologyWithSource(String dbIndex, String tbName,
			Map<String, Map<String, Field>> topology, Map<String, Object> tbSample, Set<AdvancedParameter> tbParams) {
		Map<String, Field> tbNames = topology.get(dbIndex);
		if (tbNames == null) {
			tbNames = new HashMap<String, Field>();
			topology.put(dbIndex, tbNames);
		}
		Field f = tbNames.get(tbName);
		if (f == null) {
			f = new Field(tbParams.size());
			tbNames.put(tbName, f);
		}
		for (AdvancedParameter ap : tbParams) {
			Set<Object> set = f.sourceKeys.get(ap.key);
			if (set == null) {
				set = new HashSet<Object>();
			}
			set.add(tbSample.get(ap.key));
		}
	}

	private Map<String, Field> toMapField(Map<String/*rule计算结果*/, Samples/*得到该结果的样本*/> values) {
		Map<String, Field> res = new HashMap<String, Field>(values.size());
		for (Map.Entry<String, Samples> e : values.entrySet()) {
			Field f = new Field(e.getValue().size());
			f.sourceKeys = e.getValue().getColumnEnumerates();
			res.put(e.getKey(), f);
		}
		return res;
	}

	private Map<String, Field> toMapField(Set<String> values) {
		Map<String, Field> res = new HashMap<String, Field>(values.size());
		for (String valule : values) {
			res.put(valule, null);
		}
		return res;
	}

	private List<TargetDB> buildTargetDbList(Map<String, Set<String>> topology) {
		List<TargetDB> targetDbList = new ArrayList<TargetDB>(topology.size());
		
		for (Map.Entry<String, Set<String>> e : topology.entrySet()) {
			TargetDB db = new TargetDB();
			Map<String, Field> tableNames = new HashMap<String, Field>(e.getValue().size());
			for (String tbName : e.getValue()) {
				tableNames.put(tbName, null);
			}
			db.setDbIndex(e.getKey());
			db.setTableNames(tableNames);
			targetDbList.add(db);
		}
		return targetDbList;
	}

	private List<TargetDB> buildTargetDbListWithSourceKey(Map<String, Map<String, Field>> topology) {
		List<TargetDB> targetDbList = new ArrayList<TargetDB>(topology.size());
		for (Map.Entry<String, Map<String, Field>> e : topology.entrySet()) {
			TargetDB db = new TargetDB();
			db.setDbIndex(e.getKey());
			db.setTableNames(e.getValue());
			targetDbList.add(db);
		}
		return targetDbList;
	}

	private static <T> Rule<T> findMatchedRule(Map<String, Comparative> allRuleColumnArgs, List<Rule<T>> shardRules,
			Map<String, Comparative> matchArgs, ComparativeMapChoicer choicer, List<Object> args,
			VirtualTableRule<String, String> rule) {
		Rule<T> matchedRule = null;
		if (shardRules != null && shardRules.size() != 0) {
			matchedRule = findMatchedRule(allRuleColumnArgs, shardRules, matchArgs, choicer, args);
			if (matchedRule == null) {
				//有分库或分表规则，但是没有匹配到，是否执行全部扫描
				if (!rule.isAllowFullTableScan()) {
					List<Set<String>> shardColumns = new LinkedList<Set<String>>();
					for(Rule<T> r : shardRules){
						Set<String> columnSet = new LinkedHashSet<String>();
						for(RuleColumn rc : r.getRuleColumnSet()){
							columnSet.add(rc.key);
						}
						shardColumns.add(columnSet);
					}
					throw new IllegalArgumentException("sql contain no sharding column:" + shardColumns);
				}
			}
		}
		return matchedRule;
	}

	/**
	 * @return 返回两个规则的公共列
	 */
	private static Set<String> getCommonColumnSet(Rule<String> matchedDbRule, Rule<String> matchedTbRule) {
		Set<String> res = null;
		for (String key : matchedDbRule.getRuleColumns().keySet()) {
			if (matchedTbRule.getRuleColumns().containsKey(key)) {
				if (res == null) {
					res = new HashSet<String>(1);
				}
				res.add(key);
			}
		}
		return res;
	}

	/**
	 * @return tbRule中和dbRule列名相同而自增类型不用的AdvancedParameter对象
	 */
	private static Set<AdvancedParameter> diifTypeInCommon(Rule<String> dbRule, Rule<String> tbRule,
			String[] commonColumn) {
		Set<AdvancedParameter> diifTypeInCommon = null;
		for (String common : commonColumn) {
			AdvancedParameter dbap = (AdvancedParameter) dbRule.getRuleColumns().get(common);
			AdvancedParameter tbap = (AdvancedParameter) tbRule.getRuleColumns().get(common);
			if (dbap.atomicIncreateType != tbap.atomicIncreateType) {
				if (diifTypeInCommon == null) {
					diifTypeInCommon = new HashSet<AdvancedParameter>(0);
				}
				diifTypeInCommon.add(tbap);
			}
		}
		return diifTypeInCommon;
	}

	/**
	 * 规则一：#a# #b?#
	 * 规则二：#a# #c?#
	 * 规则三：#b?# #d?#
	 * 参数为(a，c)，则选规则二; 参数为(a，d)则选规则一; 参数为(b)择选规则三
	 * @param <T>
	 * @param allRuleColumnArgs
	 * @param rules
	 * @param matchArgs
	 * @return
	 */
	private static <T> Rule<T> findMatchedRule(Map<String, Comparative> allRuleColumnArgs, List<Rule<T>> rules,
			Map<String, Comparative> matchArgs, ComparativeMapChoicer choicer, List<Object> args) {
		for (Rule<T> r : rules) {
			matchArgs.clear();
			for (RuleColumn ruleColumn : r.getRuleColumns().values()) {
				Comparative comparative = getComparative(ruleColumn.key, allRuleColumnArgs, choicer, args);
				if (comparative == null) {
					break;
				}
				matchArgs.put(ruleColumn.key, comparative);
			}
			if (matchArgs.size() == r.getRuleColumns().size()) {
				return r; //完全匹配
			}
		}

		for (Rule<T> r : rules) {
			matchArgs.clear();
			int mandatoryColumnCount = 0;
			for (RuleColumn ruleColumn : r.getRuleColumns().values()) {
				if (!ruleColumn.needAppear) {
					continue;
				}
				mandatoryColumnCount++;
				Comparative comparative = getComparative(ruleColumn.key, allRuleColumnArgs, choicer, args);
				if (comparative == null) {
					break;
				}
				matchArgs.put(ruleColumn.key, comparative);
			}

			if (mandatoryColumnCount != 0 && matchArgs.size() == mandatoryColumnCount) {
				return r; //必选列匹配
			}
		}

		//针对没有必选列的规则如：rule=..#a?#..#b?#.. 并且只有a或者b列在sql中有
		arule: for (Rule<T> r : rules) {
			matchArgs.clear();
			for (RuleColumn ruleColumn : r.getRuleColumns().values()) {
				if (ruleColumn.needAppear)
					continue arule; //如果当前规则有必选项，直接跳过,因为走到这里必选列已经不匹配了
				Comparative comparative = getComparative(ruleColumn.key, allRuleColumnArgs, choicer, args);
				if (comparative != null) {
					matchArgs.put(ruleColumn.key, allRuleColumnArgs.get(ruleColumn.key));
				}
			}
			if (matchArgs.size() != 0) {
				return r; //第一个全是可选列的规则，并且args包含该规则的部分可选列
			}
		}
		return null;
	}

	private static Comparative getComparative(String colName, Map<String, Comparative> allRuleColumnArgs,
			ComparativeMapChoicer comparativeMapChoicer, List<Object> args) {
		Comparative comparative = allRuleColumnArgs.get(colName); //先从缓存中获取
		if (comparative == null) {
			comparative = comparativeMapChoicer.getColumnComparative(args, colName);
			if (comparative != null) {
				allRuleColumnArgs.put(colName, comparative); //放入缓存
			}
		}
		return comparative;
	}

	@SuppressWarnings("unchecked")
	private static <T> T cast(Object obj) {
		return (T) obj;
	}
}
