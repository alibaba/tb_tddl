/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule.enumerator;

import java.util.Set;

/**
 * 枚举器，提供了根据每步自增数 自增获取所有枚举值的操作
 * 主要是用于解决一个规则引擎中最大的难题
 * sql 条件 :id>100 and id < 200;
 * 这种条件是无法直接代入规则引擎中进行计算然后简单的取交集来计算的，具体请参见相关文档的介绍。
 * 
 * 所以解决的方法就是把100~200之间的所有值都按照atomicIncreatementValue的设定值进行枚举。
 * 枚举出的值被放入set后返回给调用者。
 * 
 * @author shenxun
 *
 */
public interface Enumerator {
	/**
	 * @param condition 条件
	 * @param cumulativeTimes 值的个数，对于部分连续的函数来说，他完成一轮累加的次数是有限的，这里要求输入这个次数
	 * @param atomIncrValue 引起值域发生最小变动的定义域原子增数值。ex:如果对于dayofweek这样的函数来说，引起值域
	 * 发生变化的定义域的最小变动范围为1天。
	 * @param needMergeValueInCloseInterval 是否需要对> < >= <= 进行计算。
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Set<Object> getEnumeratedValue(Comparable condition,Integer cumulativeTimes,Comparable<?> atomicIncreatementValue
			,boolean needMergeValueInCloseInterval);
//	 void setNeedMergeValueInCloseInterval(boolean needMergeValueInCloseInterval);
}
