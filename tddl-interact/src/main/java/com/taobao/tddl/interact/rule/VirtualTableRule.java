/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.taobao.tddl.interact.rule.bean.DBType;
import com.taobao.tddl.interact.rule.virtualnode.DBTableMap;
import com.taobao.tddl.interact.rule.virtualnode.TableSlotMap;

/**
 * TDataSource持有所有虚拟表名到该对象的引用
 * tddl-client根据解析/预解析结果取得虚拟表名
 * 根据虚拟表名取得对应的VirtualTableRule对象
 * 
 * @author linxuan
 *
 */
public interface VirtualTableRule<D, T> {

	/**
	 * 库规则链
	 */
	List<Rule<String>> getDbShardRules();

	/**
	 * 表规则链
	 */
	List<Rule<String>> getTbShardRules();

	/**
	 * 将库规则链计算后的结果，转化为最终结果
	 * @param value
	 * @param dynamicExtraContext
	 */
	//String mapDbKey(D value);

	/**
	 * 将表规则链计算后的结果，转化为最终结果
	 * @param value
	 * @param dynamicExtraContext
	 * @return
	 */
	//String mapTbKey(T value);

	/**
	 * @return 虚拟表名和没有表规则时的默认表名
	 */
	//String getVirtualTbName();
	
	/**
	 * @return 虚拟表所在的虚拟库名，没有库规则时的默认库名
	 */
	//String getVirtualDbName();

	/**
	 * 返回本规则实际对应的全部库表拓扑结构
	 * @return key:dbIndex; value:实际物理表名的集合
	 */
	Map<String, Set<String>> getActualTopology();

	Object getOuterContext();
	
	public TableSlotMap getTableSlotMap();
	
	public DBTableMap getDbTableMap();

	//=========================================================================
	// 规则和其他属性的分割线
	//=========================================================================

	DBType getDbType();

	boolean isAllowReverseOutput();

	boolean isAllowFullTableScan();

	boolean isNeedRowCopy();

	List<String> getUniqueKeys();
	
	public String getTbNamePattern();
}
