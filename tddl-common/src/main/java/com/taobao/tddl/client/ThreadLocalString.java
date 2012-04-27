/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.client;

public class ThreadLocalString {
	public static final String ROUTE_CONDITION = "ROUTE_CONDITION";
	
	public static final String IS_EXIST_QUITE = "IS_EXIST_QUITE";
	
	public static final String DB_SELECTOR="DB_SELECTOR";
	
	/**
	 * 指定在哪个规则上执行
	 */
	public static final String RULE_SELECTOR="RULE_SELECTOR";
	
	/**
	 * 让GroupDataSource在指定序号的DATASOURCE上执行操作
	 */
	public static final String DATASOURCE_INDEX = "DATASOURCE_INDEX";
	
	public static final String TABLE_MERGE_SORT_TABLENAME = "TABLE_MERGE_SORT_TABLENAME";
	
	public static final String TABLE_MERGE_SORT_POOL = "TABLE_MERGE_SORT_POOL";
	
	public static final String TABLE_MERGE_SORT_VIRTUAL_TABLE_NAME="TABLE_MERGE_SORT_VIRTUAL_TABLE_NAME";
	
	/**
	 * 指定是否使用并行执行
	 */
	public static final String PARALLEL_EXECUTE="PARALLEL_EXECUTE";
}
