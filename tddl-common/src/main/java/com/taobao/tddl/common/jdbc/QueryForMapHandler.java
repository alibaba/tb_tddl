/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.jdbc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlTypeValue;

/**
 * 从一个表取一个对象。
 * 1. 不能每次用select *
 * 2. 取得SQLType等需要的元信息
 * 
 * 解决两个问题：
 * 1. 避免select *
 * 2. 通过setObject时显式指定sqlType以避免Oracle产生过多的子执行计划
 * 
 * @author linxuan
 *
 */
public interface QueryForMapHandler {
	//这个类可以是static的
	static class ColumnMetaData {
		public final int sqlType;
		public final int scale;
		public final String className;

		public ColumnMetaData(int sqlType, int scale, String className) {
			this.sqlType = sqlType;
			this.scale = scale;
			this.className = className;
		}
	}

	//这个类可以是static的
	static class TableMetaData {
		private final Map<String/*小写列名*/, ColumnMetaData/*列的sqlType等*/> columnMetaDataMap;
		public final String[] columnNames; //小写列名
		public final ColumnMetaData[] columns; //columnNames对应的ColumnMetaData
		public final String commaColumnNames; //逗号分隔的小写列名

		public TableMetaData(String[] columnNames, ColumnMetaData[] columns) {
			if (columnNames == null || columnNames.length == 0 || columns == null || columns.length == 0
					|| columnNames.length != columns.length) {
				throw new IllegalArgumentException("columnNames or columns is null or empty or not match");
			}

			//复制以保证其内部不变的特性
			this.columnNames = new String[columnNames.length];
			this.columns = new ColumnMetaData[columns.length];
			System.arraycopy(columnNames, 0, this.columnNames, 0, columnNames.length);
			System.arraycopy(columns, 0, this.columns, 0, columns.length);

			StringBuilder sb = new StringBuilder();
			columnMetaDataMap = new HashMap<String, ColumnMetaData>(this.columnNames.length);
			for (int i = 0; i < this.columnNames.length; i++) {
				sb.append(",").append(this.columnNames[i]);
				columnMetaDataMap.put(this.columnNames[i], this.columns[i]);
			}
			this.commaColumnNames = sb.substring(1);
		}

		public ColumnMetaData getColumnMetaData(String columnName) {
			return this.columnMetaDataMap.get(columnName);
		}
	}

	/*
	public static class UseCachedMetaDataSetter implements PreparedStatementSetter {
		private final ColumnMetaData[] columns;
		private final Object[] args;

		public UseCachedMetaDataSetter(ColumnMetaData[] columns, Object[] args) {
			if (args != null && columns == null) {
				throw new IllegalArgumentException("ColumnMetaData is null");
			}
			if (args != null && columns.length != args.length) {
				throw new IllegalArgumentException("Parameters length can't match the cached colums length.");
			}
			this.columns = columns;
			this.args = args;
		}

		public void setValues(PreparedStatement ps) throws SQLException {
			if (args != null) {
				for (int i = 0; i < columns.length; i++) {
					//ps.setObject(parameterIndex, x, targetSqlType, scale);
					if (columns[i] != null) {
						ps.setObject(i + 1, args[i], columns[i].sqlType, columns[i].scale);
					} else {
						ps.setObject(i + 1, args[i]);
					}
				}
			}
		}
	}
	*/

	public static class UseCachedMetaDataSetter extends ArgTypePreparedStatementSetter {
		private static int[] getArgTypes(ColumnMetaData[] columns) {
			int[] argTypes = new int[columns.length];
			for (int i = 0; i < columns.length; i++) {
				argTypes[i] = columns[i] == null ? SqlTypeValue.TYPE_UNKNOWN : columns[i].sqlType;
			}
			return argTypes;
		}

		public UseCachedMetaDataSetter(ColumnMetaData[] columns, Object[] args) {
			super(args, getArgTypes(columns));
		}
	}
	
	
	/**
	 * 从一个表取得一个对象。
	 * @param tableName 表名
	 * @param selectColumns null则用select * ；否则用指定的；只在第一次查询时使用。之后用第一次查到的实际列名
	 * @param whereSql select xxx from xxx where xxx 中从where开始之后的sql
	 * @param args 
	 * @return
	 */
	Map<String, Object> queryForMap(JdbcTemplate jt, String tableName, String selectColumns, String whereSql,
			Object[] args);

	/**
	 * 在queryForMap之后调用。返回每个列的列名和对应的sqlType
	 * @param tableName 小写表名
	 * @return 对应table的TableMetaData
	 */
	TableMetaData getTableMetaData(String tableName);
}
