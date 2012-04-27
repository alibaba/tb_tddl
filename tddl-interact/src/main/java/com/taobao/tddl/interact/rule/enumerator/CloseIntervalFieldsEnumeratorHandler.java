/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule.enumerator;

import java.util.Set;

import com.taobao.tddl.interact.sqljep.Comparative;

public interface CloseIntervalFieldsEnumeratorHandler {
	 /**
	 * @param source
	 * @param retValue
	 * @param cumulativeTimes
	 * @param atomIncrValue
	 */
	void processAllPassableFields(Comparative source ,Set<Object> retValue,Integer cumulativeTimes,Comparable<?> atomIncrValue);
		/**
		 * 穷举出从from到to中的所有值，根据自增value
		 * 
		 * @param from
		 * @param to
		 */
	abstract void mergeFeildOfDefinitionInCloseInterval(
				Comparative from, Comparative to, Set<Object> retValue,Integer cumulativeTimes,Comparable<?> atomIncrValue);

	
}
