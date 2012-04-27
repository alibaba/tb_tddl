/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule;

import java.util.Map;
import java.util.Set;

import com.taobao.tddl.interact.sqljep.Comparative;

/**
 * 若分库有两条规则：
 * 规则一：columnA、columnB?，若columnB没有，则取columnB的所有值域（由描点信息获得）全表扫描
 * 规则二：columnA、columnC。
 * 若sql只包含columnA，则走规则一
 * 若sql只包含columnA、columnC，则走规则二
 * 
 * 顺序+优先最大匹配，先匹配所有列，找不到再按去除可选列之后匹配
 * 
 * @author linxuan
 *
 */
public interface Rule<T> {

	public class RuleColumn {
		/**
		 * 若optional==true，则选择rule时，sql可以不包含该列。到时对该列值域做遍历
		 */
		public final boolean needAppear; //
		/**
		 * sql中的列名，必须是大写，这里在setter显示的设置成大写了
		 */
		public final String key;

		public RuleColumn(String name, boolean optional) {
			this.key = name.toUpperCase();
			this.needAppear = optional;
		}
	}

	/**
	 * @return 规则计算需要的列
	 */
	Map<String, RuleColumn> getRuleColumns();

	Set<RuleColumn> getRuleColumnSet(); //同上

	/**
	 * 列值对求值
	 * @param columnValues 列值对。个数与getRuleColumns相同。
	 * @param dynamicExtraContext 动态的额外参数。比如从ThreadLocal中传入的表名前缀
	 * @return 根据一组列值对计算结果
	 */
	T eval(Map<String/*列名*/, Object/*列值*/> columnValues, Object outerContext);

	/**
	 * 比较树匹配
	 * @param sqlArgs 从SQL提取出来的比较树 
	 * getRuleColumns包含的必选列（optional=false）必须在sqlArgs里面有。可选列可以没有
	 *     key：  String列名
	 *     value: sql中按该列提取出的比较树Comparative，已经绑定了参数
	 * @param ctx 规则执行的上下文。用于关联规则执行时，规则间必要信息的传递。对于EnumerativeRule来说。在库表规则有公共列时，
	 *            会在每一个库规则的值下面，执行表规则；执行时库规则产生该值的描点信息将以该参数传入。
	 * @param outerCtx 动态的额外参数。比如从ThreadLocal中传入的表名前缀
	 * @return 规则计算结果，和得到这个结果的所有数据。
	 */
	Map<T, ? extends Object> calculate(Map<String/*列名*/, Comparative> sqlArgs, Object ctx, Object outerCtx);

	/**
	 * 不反回每个结果对应的得到该结果的输入值（描点）集合
	 */
	Set<T> calculateNoTrace(Map<String/*列名*/, Comparative/*比较树*/> sqlArgs, Object ctx, Object outerCtx);
	
    T calculateVnodeNoTrace(String key, Object ctx, Object outerCtx);
}
