/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common;

/**
 * @author huali
 * 
 * 数据库更新命令，只能是Insert或者Update
 * 包含了命令类型，操作的表名，主键字段名称和主键值
 * 主键的值是long或者String类型。
 */
public class SyncCommand {
	public static enum TYPE {
		UPDATE,
		INSERT
	};
	
	/**
	 * 命令类型，Update或者Insert
	 */
	private TYPE type;
	
	/**
	 * 数据库名称
	 */
	private String dbName;

	/**
	 * 被操作的表的名称
	 */
	private String tableName;
	
	/**
	 * 主键的列名称
	 */
	private String columnName;
	
	/**
	 *主键的值，类型是long或者String 
	 */
	private Object value;

	public TYPE getType() {
		return type;
	}

	public String getTableName() {
		return tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public Object getValue() {
		return value;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
