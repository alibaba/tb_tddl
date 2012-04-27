/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	///*
// * 	This program is free software; you can redistribute it and/or modify it under the terms of 
// * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
// * or (at your option) any later version. 
// * 
// * 	This program is distributed in the hope that it will be useful, 
// * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
// * See the GNU General Public License for more details. 
// * 	You should have received a copy of the GNU General Public License along with this program; 
// * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
// */
//package com.taobao.tddl.common.route;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import com.taobao.tddl.common.sqljep.function.Abs;
//import com.taobao.tddl.common.sqljep.function.AddDate;
//import com.taobao.tddl.common.sqljep.function.AddMonths;
//import com.taobao.tddl.common.sqljep.function.AddTime;
//import com.taobao.tddl.common.sqljep.function.Case;
//import com.taobao.tddl.common.sqljep.function.Ceil;
//import com.taobao.tddl.common.sqljep.function.Concat;
//import com.taobao.tddl.common.sqljep.function.Convert;
//import com.taobao.tddl.common.sqljep.function.Datediff;
//import com.taobao.tddl.common.sqljep.function.Day;
//import com.taobao.tddl.common.sqljep.function.DayName;
//import com.taobao.tddl.common.sqljep.function.DayOfWeek;
//import com.taobao.tddl.common.sqljep.function.DayOfYear;
//import com.taobao.tddl.common.sqljep.function.Decode;
//import com.taobao.tddl.common.sqljep.function.Floor;
//import com.taobao.tddl.common.sqljep.function.Hash;
//import com.taobao.tddl.common.sqljep.function.Hour;
//import com.taobao.tddl.common.sqljep.function.IndistinctMatching;
//import com.taobao.tddl.common.sqljep.function.Initcap;
//import com.taobao.tddl.common.sqljep.function.Instr;
//import com.taobao.tddl.common.sqljep.function.LastDay;
//import com.taobao.tddl.common.sqljep.function.Left;
//import com.taobao.tddl.common.sqljep.function.Length;
//import com.taobao.tddl.common.sqljep.function.Lower;
//import com.taobao.tddl.common.sqljep.function.Lpad;
//import com.taobao.tddl.common.sqljep.function.Ltrim;
//import com.taobao.tddl.common.sqljep.function.MakeDate;
//import com.taobao.tddl.common.sqljep.function.MakeTime;
//import com.taobao.tddl.common.sqljep.function.Microsecond;
//import com.taobao.tddl.common.sqljep.function.Minute;
//import com.taobao.tddl.common.sqljep.function.Modulus;
//import com.taobao.tddl.common.sqljep.function.Month;
//import com.taobao.tddl.common.sqljep.function.MonthJanuaryIs1;
//import com.taobao.tddl.common.sqljep.function.MonthName;
//import com.taobao.tddl.common.sqljep.function.MonthsBetween;
//import com.taobao.tddl.common.sqljep.function.NextDay;
//import com.taobao.tddl.common.sqljep.function.Nvl;
//import com.taobao.tddl.common.sqljep.function.PostfixCommand;
//import com.taobao.tddl.common.sqljep.function.Power;
//import com.taobao.tddl.common.sqljep.function.Replace;
//import com.taobao.tddl.common.sqljep.function.Right;
//import com.taobao.tddl.common.sqljep.function.Round;
//import com.taobao.tddl.common.sqljep.function.Rpad;
//import com.taobao.tddl.common.sqljep.function.Rtrim;
//import com.taobao.tddl.common.sqljep.function.Second;
//import com.taobao.tddl.common.sqljep.function.Sign;
//import com.taobao.tddl.common.sqljep.function.SubDate;
//import com.taobao.tddl.common.sqljep.function.SubTime;
//import com.taobao.tddl.common.sqljep.function.Substring;
//import com.taobao.tddl.common.sqljep.function.TDB;
//import com.taobao.tddl.common.sqljep.function.TTab;
//import com.taobao.tddl.common.sqljep.function.ToChar;
//import com.taobao.tddl.common.sqljep.function.ToDate;
//import com.taobao.tddl.common.sqljep.function.ToNumber;
//import com.taobao.tddl.common.sqljep.function.Translate;
//import com.taobao.tddl.common.sqljep.function.Trim;
//import com.taobao.tddl.common.sqljep.function.Trunc;
//import com.taobao.tddl.common.sqljep.function.Upper;
//import com.taobao.tddl.common.sqljep.function.WeekOfYear;
//import com.taobao.tddl.common.sqljep.function.Year;
//
///**
// * 
// * @author struct
// *
// */
//public abstract class FuncRegister {
//
//	final public static Map<String, PostfixCommand> ruleFunTab = new HashMap<String,PostfixCommand>();
//	static{
//		ruleFunTab.put("abs", new Abs());
//	    ruleFunTab.put("power", new Power());
//	    ruleFunTab.put("mod", new Modulus());
//	    ruleFunTab.put("substr", new Substring());
//	    ruleFunTab.put("sign", new Sign());
//	    ruleFunTab.put("ceil", new Ceil());
//	    ruleFunTab.put("floor", new Floor());
//	    ruleFunTab.put("trunc", new Trunc());
//	    ruleFunTab.put("round", new Round());
//	    ruleFunTab.put("length", new Length());
//	    ruleFunTab.put("concat", new Concat());
//	    ruleFunTab.put("instr", new Instr());
//	    ruleFunTab.put("trim", new Trim());
//	    ruleFunTab.put("rtrim", new Rtrim());
//	    ruleFunTab.put("ltrim", new Ltrim());
//	    ruleFunTab.put("rpad", new Rpad());
//	    ruleFunTab.put("lpad", new Lpad());
//	    ruleFunTab.put("lower", new Lower());
//	    ruleFunTab.put("upper", new Upper());
//	    ruleFunTab.put("translate", new Translate());
//	    ruleFunTab.put("replace", new Replace());
//	    ruleFunTab.put("initcap", new Initcap());
//	    ruleFunTab.put("value", new Nvl());
//	    ruleFunTab.put("decode", new Decode());
//	    ruleFunTab.put("to_char", new ToChar());
//	    ruleFunTab.put("to_number", new ToNumber());
//	    ruleFunTab.put("imatch", new IndistinctMatching());         // replacement for of Oracle's SOUNDEX
//	    ruleFunTab.put("months_between", new MonthsBetween());
//	    ruleFunTab.put("add_months", new AddMonths());
//	    ruleFunTab.put("last_day", new LastDay());
//	    ruleFunTab.put("next_day", new NextDay());
//	    ruleFunTab.put("to_date", new ToDate());
//	    ruleFunTab.put("case", new Case());                 // replacement for CASE WHEN digit = 0 THEN ...;WHEN digit = 1 THEN ...;ELSE ... END CASE
//	    ruleFunTab.put("index", new Instr());                                               // maxdb
//	    ruleFunTab.put("num", new ToNumber());                              // maxdb
//	    ruleFunTab.put("chr", new ToChar());                                // maxdb
//	    ruleFunTab.put("dayname", new DayName());                   // maxdb
//	    ruleFunTab.put("adddate", new AddDate());                           // maxdb
//	    ruleFunTab.put("subdate", new SubDate());                           // maxdb
//	    ruleFunTab.put("addtime", new AddTime());                   // maxdb
//	    ruleFunTab.put("subtime", new SubTime());                           // maxdb
//	    ruleFunTab.put("year", new Year());                                         // maxdb
//	    ruleFunTab.put("month", new Month());                                       // maxdb
//	    ruleFunTab.put("day", new Day());                                                   // maxdb
//	    ruleFunTab.put("dayofmonth", new Day());                            // maxdb
//	    ruleFunTab.put("hour", new Hour());                                         // maxdb
//	    ruleFunTab.put("minute", new Minute());                                     // maxdb
//	    ruleFunTab.put("second", new Second());                             // maxdb
//	    ruleFunTab.put("microsecond", new Microsecond());   // maxdb
//	    ruleFunTab.put("datediff", new Datediff());                         // maxdb
//	    ruleFunTab.put("dayofweek", new DayOfWeek());       // maxdb
//	    ruleFunTab.put("weekofyear", new WeekOfYear());     // maxdb
//	    ruleFunTab.put("dayofyear", new DayOfYear());               // maxdb
//	    ruleFunTab.put("dayname", new DayName());                   // maxdb
//	    ruleFunTab.put("monthname", new MonthName());       // maxdb
//	    ruleFunTab.put("makedate", new MakeDate());         // maxdb
//	    ruleFunTab.put("maketime", new MakeTime());         // maxdb
//	    ruleFunTab.put("hash", new Hash());         //
//	    ruleFunTab.put("tdb", new TDB());
//	    ruleFunTab.put("ttab", new TTab());
//	    ruleFunTab.put("conv", new Convert());
//	    ruleFunTab.put("right", new Right());
//	    ruleFunTab.put("left", new Left());
//	    ruleFunTab.put("MonthJanuaryIs1", new MonthJanuaryIs1());
//	}
////	
////	/* 默认1000 */
////	private LRUMap map;
////	private Lock mapLock = new ReentrantLock(false);
////	private int LRUMapSize = 10000;
////
////	private Map<Table,TableRule> tableRuleMap = new HashMap<Table,TableRule>();
////	private Map<String,Function> functionMap = new HashMap<String,Function>();
////	private Map<String,PostfixCommand> ruleFunctionMap = new HashMap<String,PostfixCommand>();
////	
////	protected ObjectPool[] defaultPools;
////	protected ObjectPool[] readPools;
////	protected ObjectPool[] writePools;
////	private String ruleConfig;
////	private String functionConfig;
////	private String ruleFunctionConfig;
////	
////	private String defaultPool;
////	private String readPool;
////	private String writePool;
////	private boolean needParse = true;
////	private boolean needEvaluate = true;
////	
////	public AbstractQueryRouter(){
////		ruleFunctionMap.putAll(ruleFunTab);
////	}
////	
////	public String getRuleConfig() {
////		return ruleConfig;
////	}
////	
////	/**
////	 * 创建一个新的sql parser
////	 * @param connection 
////	 * @param sql
////	 * @return Parser
////	 */
////	public  abstract Parser newParser(String sql);
////	
////	public void setRuleConfig(String ruleConfig) {
////		this.ruleConfig = ruleConfig;
////	}
////	
////	public void setReadPool(String readPool) {
////		this.readPool = readPool;
////	}
////
////	public String getReadPool() {
////		return readPool;
////	}
////
////	public String getWritePool() {
////		return writePool;
////	}
////
////	public void setWritePool(String writePool) {
////		this.writePool = writePool;
////	}
////	
////	public ObjectPool[] doRoute(DatabaseConnection connection,String sql,boolean ispreparedStatment, Object[] parameters) {
////		if(sql == null) return defaultPools;
////		if(needParse){
////			return selectPool(connection, sql,ispreparedStatment,parameters);
////		}else{
////			return this.defaultPools;
////		}
////		
////	}
////	
////	/**
////	 * 返回Query 被route到目标地址 ObjectPool集合
////	 * 如果返回null，则是属于DatabaseConnection 自身属性设置的请求。
////	 * @param connection
////	 * @param sql
////	 * @param parameters
////	 * @return
////	 */
////	@SuppressWarnings("unchecked")
////	protected ObjectPool[] selectPool(DatabaseConnection connection,String sql,boolean ispreparedStatment,Object[] parameters){
////		List<String> poolNames = new ArrayList<String>();
////		
////		Statment statment = parseSql(connection,sql);
////
////		if(statment != null){
////			if(logger.isDebugEnabled()){
////				Expression expression = statment.getExpression();
////				logger.debug("Sql:["+sql +"] Expression=["+expression+"]");
////			}
////		}
////		
////		Map<Table,Map<Column,Comparative>> tables = null;
////		DMLStatment dmlStatment = null;
////		if(statment instanceof DMLStatment){
////			dmlStatment = ((DMLStatment)statment);
////			if(needEvaluate){
////				tables = dmlStatment.evaluate(parameters);
////			}
////		}else if(statment instanceof PropertyStatment){
////			setProperty(connection,(PropertyStatment)statment,parameters);
////			return null;
////		}else if(statment instanceof StartTansactionStatment){
////			return null;
////		}else if(statment instanceof CommitStatment){
////			return null;
////		}else if(statment instanceof RollbackStatment){
////			return null;
////		}
////		
////		if(tables != null && tables.size() >0){
////			Set<Map.Entry<Table,Map<Column,Comparative>>> entrySet  = tables.entrySet();
////			for(Map.Entry<Table,Map<Column,Comparative>> entry : entrySet){
////				Map<Column,Comparative> columnMap = entry.getValue();
////				TableRule tableRule = this.tableRuleMap.get(entry.getKey());
////				
////				/**
////				 * 如果存在table Rule 则需要看是否有Rule
////				 */
////				if(tableRule != null){
////					
////					if(columnMap == null || ispreparedStatment){
////						String[] pools = dmlStatment.isReadStatment()?tableRule.readPools:tableRule.writePools;
////						if(pools == null){
////							pools = tableRule.defaultPools;
////						}
////						for(String poolName : pools){
////							if(!poolNames.contains(poolName)){
////								poolNames.add(poolName);
////							}
////						}
////						if(logger.isDebugEnabled()){
////							logger.debug("Sql:["+sql +"] no Column rule, using table:"+tableRule.table +" default rules:"+Arrays.toString(tableRule.defaultPools));
////						}
////						continue;
////					}
////					List<String> groupMatched = new ArrayList(); 
////					for(Rule rule:tableRule.ruleList){
////						if(rule.group != null){
////							if(groupMatched.contains(rule.group)){
////								continue;
////							}
////						}
////						
////						//如果参数比必须的参数小，则继续下一条规则
////						if(columnMap.size()<rule.parameterMap.size()){
////							continue;
////						}else{
////							
////							boolean matched = true;
////
////							//如果查询语句中包含了该规则不需要的参数，则该规则将被忽略
////							for(Column exclude : rule.excludes){
////								
////								Comparable condition = columnMap.get(exclude);
////								if(condition != null){
////									matched = false;
////									break;
////								}
////							}
////							
////							//如果不匹配将继续下一条规则
////							if(!matched) continue;
////							
////							Comparable[] comparables= new Comparable[rule.parameterMap.size()];
////							//规则中的参数必须在dmlstatement中存在，否则这个规则将不启作用
////							for(Map.Entry<Column,Integer> parameter : rule.cloumnMap.entrySet()){
////								
////								Comparative condition = columnMap.get(parameter.getKey());
////								if(condition != null){
////									
////									//如果规则忽略 数组的 参数，并且参数有array 参数，则忽略该规则
////									if(rule.ignoreArray && condition instanceof ComparativeBaseList){
////										matched = false;
////										break;
////									}
////									
////									comparables[parameter.getValue()] = (Comparative)condition.clone();
////								}else{
////									matched = false;
////									break;
////								}
////							}
////							
////							//如果不匹配将继续下一条规则
////							if(!matched) continue;
////							
////							try {
////								Comparable<?> result = rule.rowJep.getValue(comparables);
////								if(result instanceof Comparative){
////									matched = (Boolean)((Comparative)result).getValue();
////								}else{
////									matched = (Boolean)result;
////								}
////								
////								if(matched){
////									if(rule.group != null){
////										groupMatched.add(rule.group);
////									}
////									String[] pools = dmlStatment.isReadStatment()?rule.readPools:rule.writePools;
////									if(pools == null){
////										pools = rule.defaultPools;
////									}
////									if(pools != null){
////										for(String poolName : pools){
////											if(!poolNames.contains(poolName)){
////												poolNames.add(poolName);
////											}
////										}
////									}else{
////										logger.warn("rule:"+rule.name+" matched, but pools is null");
////									}
////									
////									if(logger.isDebugEnabled()){
////										logger.debug("Sql:["+sql +"] matched rule:"+rule.name);
////									}
////								}
////							} catch (com.taobao.tddl.common.sqljep.ParseException e) {
////								//logger.error("parse rule error:"+rule.expression,e);
////							}
////						}
////					}
////					
////					//如果所有规则都无法匹配，则默认采用TableRule中的pool设置。 
////					if(poolNames.size() == 0){
////						if(logger.isDebugEnabled()){
////							logger.debug("no rule matched, using table default rules:"+Arrays.toString(tableRule.defaultPools));
////						}
////						String[] pools = dmlStatment.isReadStatment()?tableRule.readPools:tableRule.writePools;
////						if(pools == null){
////							pools = tableRule.defaultPools;
////						}
////						
////						for(String poolName : pools){
////							if(!poolNames.contains(poolName)){
////								poolNames.add(poolName);
////							}
////						}
////					}
////				}
////			}
////		}else{
////			
////			//如果sql语句中没有包含table，则采用TableRule中没有name的配置,一般情况下只有一条该规则，而且只有defaultPool启作用
////			TableRule tableRule =  this.tableRuleMap.get(null);
////			if(tableRule != null && tableRule.defaultPools != null && tableRule.defaultPools.length >0){
////				for(String poolName : tableRule.defaultPools){
////					if(!poolNames.contains(poolName)){
////						poolNames.add(poolName);
////					}
////				}
////			}
////		}
////		//匹配规则技术。
////		ObjectPool[] pools = new ObjectPool[poolNames.size()];
////		int i=0;
////		for(String name :poolNames){
////			pools[i++] = ProxyRuntimeContext.getInstance().getPoolMap().get(name);
////		}
////
////		if(logger.isDebugEnabled()){
////			logger.debug("Sql:["+sql +"] route to pools:"+poolNames);
////		}
////		
////		if(pools == null || pools.length == 0){
////			if(dmlStatment != null){
////				pools = dmlStatment.isReadStatment()? this.readPools: this.writePools;
////			}
////			if(pools == null || pools.length == 0 || pools[0] == null) pools = this.defaultPools;
////		}
////		
////		return pools;
////	}
////	
////	/**
////	 * 根据 PropertyStatment 设置相关连接的属性 
////	 * @param conn 当前请求的连接
////	 * @param statment 当前请求的Statment
////	 * @param parameters 
////	 */
////	protected abstract void setProperty(DatabaseConnection conn,PropertyStatment statment,Object[] parameters);
////	
////	public void init() throws InitialisationException {
////		defaultPools = new ObjectPool[]{ProxyRuntimeContext.getInstance().getPoolMap().get(defaultPool)};
////		
////		if(defaultPools == null || defaultPools[0] == null){
////			throw new InitialisationException("default pool required!");
////		}
////		if(this.readPool != null && !StringUtil.isEmpty(readPool)){
////			readPools = new ObjectPool[]{ProxyRuntimeContext.getInstance().getPoolMap().get(this.readPool)};
////		}
////		if(this.writePool != null && !StringUtil.isEmpty(writePool)){
////			writePools = new ObjectPool[]{ProxyRuntimeContext.getInstance().getPoolMap().get(writePool)};
////		}
////		map = new LRUMap(this.LRUMapSize);
////		
////		class ConfigCheckTread extends Thread{
////			long lastRuleModified;
////			long lastFunFileModified;
////			long lastRuleFunctionFileModified;
////			File ruleFile;
////			File funFile;
////			File ruleFunctionFile;
////			//变动检查。
////			private ConfigCheckTread(){
////			
////				this.setDaemon(true);
////				this.setName("ruleConfigCheckThread");
////				ruleFile = new File(AbstractQueryRouter.this.ruleConfig);
////				funFile = new File(AbstractQueryRouter.this.functionConfig);
////				lastRuleModified = ruleFile.lastModified();
////				lastFunFileModified = funFile.lastModified();
////				if(AbstractQueryRouter.this.ruleFunctionConfig != null){
////					ruleFunctionFile = new File(AbstractQueryRouter.this.ruleFunctionConfig);
////					lastRuleFunctionFileModified = ruleFunctionFile.lastModified();
////				}
////			}
////			public void run(){
////				while(true){
////					try {
////						Thread.sleep(5000l);
////						Map<String,Function> funMap = null;
////						Map<String,PostfixCommand> ruleFunMap = null;
////						Map<Table,TableRule> tableRuleMap = null;
////						try{
////							if(AbstractQueryRouter.this.functionConfig != null){
////								if(funFile.lastModified() != lastFunFileModified){
////									try{
////										funMap = loadFunctionMap(AbstractQueryRouter.this.functionConfig);
////									}catch(ConfigurationException exception){
////									}
////									
////								}
////							}
////							if(AbstractQueryRouter.this.ruleFunctionConfig != null){
////								if(ruleFunctionFile.lastModified() != lastRuleFunctionFileModified){
////									ruleFunMap = loadRuleFunctionMap(AbstractQueryRouter.this.ruleFunctionConfig);
////								}
////							}
////							
////							if(AbstractQueryRouter.this.ruleConfig != null){
////								if(ruleFile.lastModified() != lastRuleModified || 
////										(AbstractQueryRouter.this.ruleFunctionConfig != null && ruleFunctionFile.lastModified() != lastRuleFunctionFileModified)){
////									tableRuleMap = loadConfig(AbstractQueryRouter.this.ruleConfig);
////								}
////							}
////							
////							if(funMap != null){
////								AbstractQueryRouter.this.functionMap = funMap;
////							}
////							
////							if(ruleFunMap != null){
////								AbstractQueryRouter.this.ruleFunctionMap = ruleFunMap;
////							}
////							
////							if(tableRuleMap != null){
////								AbstractQueryRouter.this.tableRuleMap = tableRuleMap;
////							}
////							
////						}catch(ConfigurationException e){
////							
////						}finally{
////							if(funFile != null && funFile.exists()){
////								lastFunFileModified = funFile.lastModified();
////							}
////							if(ruleFunctionFile != null && ruleFunctionFile.exists()){
////								lastRuleFunctionFileModified = ruleFunctionFile.lastModified();
////							}
////							if(ruleFile != null && ruleFile.exists()){
////								lastRuleModified = ruleFile.lastModified();
////							}
////						}
////					} catch (InterruptedException e) {
////					}
////				}
////			}
////		}
////		
////		if(needParse){
////			boolean configNeedCheck = false;
////			
////			
////			if(AbstractQueryRouter.this.functionConfig != null){
////				this.functionMap = loadFunctionMap(AbstractQueryRouter.this.functionConfig);
////				configNeedCheck = true;
////			}else{
////				needEvaluate = false;
////			}
////			
////			if(AbstractQueryRouter.this.ruleFunctionConfig != null){
////				AbstractQueryRouter.this.ruleFunctionMap = loadRuleFunctionMap(AbstractQueryRouter.this.ruleFunctionConfig);
////				configNeedCheck =true;
////			}
////			
////			if(AbstractQueryRouter.this.ruleConfig != null){
////				this.tableRuleMap = loadConfig(this.ruleConfig);
////				configNeedCheck =true;
////			}else{
////				needEvaluate = false;
////			}
////			
////			if(configNeedCheck){
////				new ConfigCheckTread().start();
////			}
////		}
////	}
////	
////	public static Map<String,Function> loadFunctionMap(String configFileName){
////		FunctionLoader<String,Function> loader = new FunctionLoader<String,Function>(){
////
////			@Override
////			public void initBeanObject(BeanObjectEntityConfig config,
////					Function bean) {
////				bean.setName(config.getName());
////			}
////
////			@Override
////			public void putToMap(Map<String, Function> map, Function value) {
////				map.put(value.getName(), value);
////			}
////			
////		};
////		
////		loader.setDTD("/com/meidusa/amoeba/xml/function.dtd");
////		loader.setDTDSystemID("function.dtd");
////		return loader.loadFunctionMap(configFileName);
////	}
////	
////	public static Map<String,PostfixCommand> loadRuleFunctionMap(String configFileName){
////		FunctionLoader<String,PostfixCommand> loader = new FunctionLoader<String,PostfixCommand>(){
////			@Override
////			public void initBeanObject(BeanObjectEntityConfig config,
////					PostfixCommand bean) {
////				bean.setName(config.getName());
////			}
////
////			@Override
////			public void putToMap(Map<String, PostfixCommand> map, PostfixCommand value) {
////				map.put(value.getName(), value);
////			}
////			
////		};
////		
////		loader.setDTD("/com/meidusa/amoeba/xml/function.dtd");
////		loader.setDTDSystemID("function.dtd");
////		
////		Map<String,PostfixCommand> tempRuleFunMap = new HashMap<String,PostfixCommand>();
////		Map<String,PostfixCommand> defindMap = loader.loadFunctionMap(configFileName);
////		tempRuleFunMap.putAll(ruleFunTab);
////		tempRuleFunMap.putAll(defindMap);
////		return tempRuleFunMap;
////	}
////	
////	private Map<Table,TableRule> loadConfig(String configFileName){
////		DocumentBuilder db;
////
////        try {
////            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
////            dbf.setValidating(true);
////            dbf.setNamespaceAware(false);
////            db = dbf.newDocumentBuilder();
////            db.setEntityResolver(new EntityResolver() {
////                public InputSource resolveEntity(String publicId, String systemId) {
////                	if (systemId.endsWith("rule.dtd")) {
////                	      InputStream in = AbstractQueryRouter.class.getResourceAsStream("/com/meidusa/amoeba/xml/rule.dtd");
////                	      if (in == null) {
////                		LogLog.error("Could not find [rule.dtd]. Used [" + AbstractQueryRouter.class.getClassLoader() 
////                			     + "] class loader in the search.");
////                		return null;
////                	      } else {
////                		return new InputSource(in);
////                	      }
////            	    } else {
////            	      return null;
////            	    }
////                }
////            });
////            
////            db.setErrorHandler(new ErrorHandler() {
////                public void warning(SAXParseException exception) {
////                }
////
////                public void error(SAXParseException exception) throws SAXException {
////                    logger.error(exception.getMessage() + " at (" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ")");
////                    throw exception;
////                }
////
////                public void fatalError(SAXParseException exception) throws SAXException {
////                    logger.fatal(exception.getMessage() + " at (" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ")");
////                    throw exception;
////                }
////            });
////           return loadConfigurationFile(configFileName, db);
////        } catch (Exception e) {
////            logger.fatal("Could not load configuration file, failing", e);
////            throw new ConfigurationException("Error loading configuration file " + configFileName, e);
////        }
////	}
////
////	private Map<Table,TableRule> loadConfigurationFile(String fileName, DocumentBuilder db) throws InitialisationException {
////        Document doc = null;
////        InputStream is = null;
////        Map<Table,TableRule> tableRuleMap = new HashMap<Table,TableRule>();
////        try {
////            is = new FileInputStream(new File(fileName));
////            doc = db.parse(is);
////        } catch (Exception e) {
////            final String s = "Caught exception while loading file " + fileName;
////            logger.error(s, e);
////            throw new ConfigurationException(s, e);
////        } finally {
////            if (is != null) {
////                try {
////                    is.close();
////                } catch (IOException e) {
////                    logger.error("Unable to close input stream", e);
////                }
////            }
////        }
////        Element rootElement = doc.getDocumentElement();
////        NodeList children = rootElement.getChildNodes();
////        int childSize = children.getLength();
////
////        for (int i = 0; i < childSize; i++) {
////            Node childNode = children.item(i);
////
////            if (childNode instanceof Element) {
////                Element child = (Element) childNode;
////
////                final String nodeName = child.getNodeName();
////                if (nodeName.equals("tableRule")) {
////                	TableRule rule = loadTableRule(child);
////                	tableRuleMap.put(rule.table.getName() == null?null:rule.table, rule);
////                }
////            }
////        }
////
////        if (logger.isInfoEnabled()) {
////            logger.info("Loaded rule configuration from: " + fileName);
////        }
////        return tableRuleMap;
////	}
////
////	private TableRule loadTableRule(Element current) throws InitialisationException {
////		TableRule tableRule = new TableRule();
////		String name = current.getAttribute("name");
////		String schemaName = current.getAttribute("schema");
////		Table table = new Table();
////		table.setName(name);
////		if(!StringUtil.isEmpty(schemaName)){
////			Schema schema = new Schema();
////			
////			schema.setName(schemaName);
////			table.setSchema(schema);
////		}
////
////		tableRule.table = table;
////		String defaultPools = current.getAttribute("defaultPools");
////		if(defaultPools != null){
////			tableRule.defaultPools = readTokenizedString(defaultPools," ,");
////		}
////		
////		String readPools = current.getAttribute("readPools");
////		if(readPools != null){
////			tableRule.readPools = readTokenizedString(readPools," ,");
////		}
////		
////		
////		String writePools = current.getAttribute("writePools");
////		if(writePools != null){
////			tableRule.writePools = readTokenizedString(writePools," ,");
////		}
////		
////		
////		NodeList children = current.getChildNodes();
////		int childSize = children.getLength();
////
////        for (int i = 0; i < childSize; i++) {
////            Node childNode = children.item(i);
////
////            if (childNode instanceof Element) {
////                Element child = (Element) childNode;
////
////                final String nodeName = child.getNodeName();
////                if (nodeName.equals("rule")) {
////                	tableRule.ruleList.add(loadRule(child,tableRule));
////                }
////            }
////        }
////		return tableRule;
////	}
////
////	private  Rule loadRule(Element current, TableRule tableRule) throws InitialisationException {
////		Rule rule = new Rule();
////		rule.name = current.getAttribute("name");
////		String group = current.getAttribute("group");
////		
////		rule.group = StringUtil.isEmpty(group)?null:group;
////		
////		String ignoreArray = current.getAttribute("ignoreArray");
////		rule.ignoreArray = Boolean.parseBoolean(ignoreArray);
////		
////		Element expression = DocumentUtil.getTheOnlyElement(current, "expression");
////		rule.expression = expression.getTextContent();
////		Element defaultPoolsNode = DocumentUtil.getTheOnlyElement(current, "defaultPools");
////		
////		if(defaultPoolsNode != null){
////			String defaultPools = defaultPoolsNode.getTextContent();
////			rule.defaultPools = readTokenizedString(defaultPools," ,");
////		}
////		
////		Element readPoolsNode = DocumentUtil.getTheOnlyElement(current, "readPools");
////		if(readPoolsNode != null){
////			rule.readPools = readTokenizedString(readPoolsNode.getTextContent()," ,");
////		}
////		
////		Element writePoolsNode = DocumentUtil.getTheOnlyElement(current, "writePools");
////		if(writePoolsNode != null){
////			rule.writePools = readTokenizedString(writePoolsNode.getTextContent()," ,");
////		}
////
////		Element parametersNode = DocumentUtil.getTheOnlyElement(current, "parameters");
////		if(parametersNode != null){
////			String[] tokens  = readTokenizedString(parametersNode.getTextContent()," ,");
////			int index = 0;
////			for(String parameter:tokens){
////				rule.parameterMap.put(parameter, index);
////				Column column = new Column();
////				column.setName(parameter);
////				column.setTable(tableRule.table);
////				rule.cloumnMap.put(column, index);
////				index++;
////			}
////			
////			tokens  = readTokenizedString(parametersNode.getAttribute("excludes")," ,");
////			if(tokens != null){
////				for(String parameter:tokens){
////					Column column = new Column();
////					column.setName(parameter);
////					column.setTable(tableRule.table);
////					rule.excludes.add(column);
////				}
////			}
////		}
////		
////		rule.rowJep = new RowJEP(rule.expression);
////		try {
////			//parese the Expression grammer。
////			rule.rowJep.parseExpression(rule.parameterMap,(Map<String,Variable>)null,this.ruleFunctionMap);
////		} catch (com.taobao.tddl.common.sqljep.ParseException e) {
////			throw new InitialisationException("parser expression:"+rule.expression+" error",e);
////		}
////		return rule;
////	}
////
////	public static String[] readTokenizedString(String string,String delim){
////		if(string == null|| string.trim().length() == 0) return null;
////		StringTokenizer tokenizer = new StringTokenizer(string,delim);
////		String[] tokens = new String[tokenizer.countTokens()];
////		int index = 0;
////		while(tokenizer.hasMoreTokens()){
////			String token = tokenizer.nextToken().trim();
////			tokens[index++] = token;
////		}
////		if(tokens.length>0){
////			return tokens;
////		}
////		return null;
////	}
////	
////	public int getLRUMapSize() {
////		return LRUMapSize;
////	}
////
////
////	public String getDefaultPool() {
////		return defaultPool;
////	}
////
////	public void setDefaultPool(String defaultPoolName) {
////		this.defaultPool = defaultPoolName;
////	}
////
////	public void setLRUMapSize(int mapSize) {
////		LRUMapSize = mapSize;
////	}
////
////	public String getFunctionConfig() {
////		return functionConfig;
////	}
////
////	public void setFunctionConfig(String functionConfig) {
////		this.functionConfig = functionConfig;
////	}
////
////	public boolean isNeedParse() {
////		return needParse;
////	}
////
////	public void setNeedParse(boolean needParse) {
////		this.needParse = needParse;
////	}
////
////	public void setRuleFunctionConfig(String ruleFunctionConfig) {
////		this.ruleFunctionConfig = ruleFunctionConfig;
////	}
////	
////	public ObjectPool getObjectPool(Object key){
////		if(key instanceof String){
////			return ProxyRuntimeContext.getInstance().getPoolMap().get(key);
////		}else{
////			for(ObjectPool pool : ProxyRuntimeContext.getInstance().getPoolMap().values()){
////				if(pool.hashCode() == key.hashCode()){
////					return pool;
////				}
////			}
////		}
////		return null;
////	}
////	
////	/* (non-Javadoc)
////	 * @see com.taobao.tdl.client.route.QueryRouter#parseSql(com.taobao.tdl.client.net.DatabaseConnection, java.lang.String)
////	 */
////	public Statment parseSql(DatabaseConnection connection,String sql){
////		Statment statment = null;
////		
////		String defaultSchema = (connection ==null || StringUtil.isEmpty(connection.getSchema())) ?null: connection.getSchema();
////		
////		int sqlWithSchemaHashcode = defaultSchema != null? (defaultSchema.hashCode()^sql.hashCode()):sql.hashCode();
////		mapLock.lock();
////		try{
////			statment = (Statment)map.get(sqlWithSchemaHashcode);
////		}finally{
////			mapLock.unlock();
////		}
////		if(statment == null){
////			synchronized (sql) {
////				statment = (Statment)map.get(sqlWithSchemaHashcode);
////				if(statment != null) return statment;
////				
////				Parser parser = newParser(sql);
////				parser.setFunctionMap(this.functionMap);
////				if(defaultSchema != null){
////					Schema schema = new Schema();
////					schema.setName(defaultSchema);
////					parser.setDefaultSchema(schema);
////				}
////				try {
////					
////					try{
////						statment = parser.doParse();
////						mapLock.lock();
////						try{
////							map.put(sqlWithSchemaHashcode, statment);
////						}finally{
////							mapLock.unlock();
////						}
////					}catch(Error e){
////						logger.error(sql,e);
////						return null;
////					}
////					
////				} catch (ParseException e) {
////					logger.error(sql,e);
////				}
////			}
////		}
////		return statment;
////	}
////	
////	public int parseParameterCount(DatabaseConnection connection,String sql){
////		Statment statment = parseSql(connection,sql);
////		return statment.getParameterCount();
////	}
//	
//}
