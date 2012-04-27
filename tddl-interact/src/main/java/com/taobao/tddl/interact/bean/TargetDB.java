/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 目标数据库特征 包含读写目标ds的id 以及该ds中符合要求的表名列表。
 * 
 * @author shenxun
 * 
 */
public class TargetDB{
	/**
	 * 这个库在TDatasource索引中的索引
	 */
	private String dbIndex;

	/**
	 * 这个规则下的符合查询条件的表名列表
	 */
	private Map<String, Field> tableNames;
	/**
	 * 反向输出的sql,如果reverseOutput不为false,则这里不会为null. 但仍然可能为一个empty list
	 */
	private List<ReverseOutput> outputSQL;

	/**
	 * 返回表名的结果集
	 * 
	 * @return 空Set if 没有表 表名结果集
	 */
	public Set<String> getTableNames() {
		if (tableNames == null) {
			return null;
		}
		return tableNames.keySet();
	}

	public void setTableNames(Map<String, Field> tableNames) {
		this.tableNames = tableNames;
	}

	public List<ReverseOutput> getOutputSQL() {
		return outputSQL;
	}

	public Map<String, Field> getTableNameMap() {
		return tableNames;
	}

	public void setOutputSQL(List<ReverseOutput> outputSQL) {
		this.outputSQL = outputSQL;
	}

	public void addOneTable(String table) {
		if (tableNames == null) {
			tableNames = new HashMap<String, Field>();
		}
		tableNames.put(table, Field.EMPTY_FIELD);
	}

	public void addOneTable(String table, Field field) {
		if (tableNames == null) {
			tableNames = new HashMap<String, Field>();
		}
		tableNames.put(table, field);
	}

	public void addOneTableWithSameTable(String table, Field field) {
		if (tableNames == null) {
			tableNames = new HashMap<String, Field>();
			tableNames.put(table, field);
		} else {
			Field inField = tableNames.get(table);
			if (inField == null) {
				tableNames.put(table, field);
			} else {
				if (field.sourceKeys != null) {
					for (Map.Entry<String, Set<Object>> entry : field.sourceKeys
							.entrySet()) {
						inField.sourceKeys.get(entry.getKey()).addAll(
								entry.getValue());
					}
				}
			}
		}
	}

	public String getDbIndex() {
		return dbIndex;
	}

	public void setDbIndex(String dbIndex) {
		this.dbIndex = dbIndex;
	}

	@Override
	public String toString() {
		return "TargetDB [dbIndex=" + dbIndex + ", outputSQL=" + outputSQL
				+ ", tableNames=" + tableNames + "]";
	}

}
