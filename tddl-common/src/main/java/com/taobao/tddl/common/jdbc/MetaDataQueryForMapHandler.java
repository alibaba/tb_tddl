/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.jdbc;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
//这里导致了tddl对spring的依赖
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 第一次通过select * 获取列名和sqlType并缓存。之后通过select columns 获取
 * 
 * @author linxuan
 *
 */
public class MetaDataQueryForMapHandler implements QueryForMapHandler {
	private static final Log log = LogFactory.getLog(MetaDataQueryForMapHandler.class);

	private ConcurrentHashMap<String/*小写表名*/, TableMetaData/*表的原信息*/> tableMetaDatas = new ConcurrentHashMap<String, TableMetaData>();
	//ColumnMapRowMapper导致了tddl对spring的依赖，这样不太好吧？
	private ConcurrentHashMap<String/*小写表名*/, ColumnMapRowMapper> rowMappers = new ConcurrentHashMap<String, ColumnMapRowMapper>();

	public MetaDataQueryForMapHandler(){
		
	}
	
	public Map<String, Object> queryForMap(JdbcTemplate jdbcTemplate, String tableName, String selectColumns, String whereSql, Object[] args) {
		tableName = tableName.toLowerCase();
		TableMetaData tmd = tableMetaDatas.get(tableName);
		StringBuilder sql = new StringBuilder("select ");
		if (tmd == null) {
			sql.append(selectColumns == null ? "*" : selectColumns);
		} else {
			sql.append(tmd.commaColumnNames);
		}
		//如果总sql的数量是固定且比较小的话,能否干脆把这整个sql也放在缓存里？
		//并且这个sql的长度其实是可以在创建之前就算出来的
		sql.append(" from ").append(tableName).append(" ").append(whereSql);

		if (log.isDebugEnabled()) {
			log.debug("sql=[" + sql.toString() + "], args=" + Arrays.asList(args));
		}

		try {
			return convert(jdbcTemplate.queryForObject(sql.toString(), args, getRowMapper(tableName)));
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (DataAccessException e) {
			log.error("sql=[" + sql.toString() + "], args=" + Arrays.asList(args), e);
			throw e;
		}
	}

	public TableMetaData getTableMetaData(String tableName) {
		if (tableMetaDatas.get(tableName) == null) {
			throw new IllegalStateException("Must be called after queryForMap called at least once on table "
					+ tableName);
		}
		return this.tableMetaDatas.get(tableName);
	}

	/**
	 * @param tableName 小写表名
	 */
	private ColumnMapRowMapper getRowMapper(String tableName) {
		ColumnMapRowMapper rowMapper = rowMappers.get(tableName);
		if (rowMapper == null) {
			rowMapper = new CachedColumnMapRowMapper(tableName);
			rowMappers.putIfAbsent(tableName, rowMapper);
			return rowMappers.get(tableName);
		}
		return rowMapper;
	}

	/**
	 * 1. 缓存 ResultSetMetaData 的部分值
	 * 2. 将结果Map中的列名(key)转为小写
	 */
	private class CachedColumnMapRowMapper extends ColumnMapRowMapper {
		private final String tableName; //小写表名

		public CachedColumnMapRowMapper(String tableName) {
			this.tableName = tableName;
		}

		@Override
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			TableMetaData tmd = tableMetaDatas.get(tableName);
			if (tmd == null) {
				//获取一行记录，同时初始化metaData 
				initMetaData(tableName, rs.getMetaData());
				tmd = tableMetaDatas.get(tableName);
				if(tmd == null){
					log.warn("MetaData is still null after initMetaData().");
					return super.mapRow(rs, rowNum);
				}
			}
			//根据已有的metadata做转换 。保证用缓存的metadata而不用再获取metadata
			Map mapOfColValues = super.createColumnMap(tmd.columns.length);
			for (int i = 1; i <= tmd.columns.length; i++) {
				String key = getColumnKey(tmd.columnNames[i - 1]);
				Object obj = getResultSetValue(tmd, rs, i);
				mapOfColValues.put(key, obj);
			}
			return mapOfColValues;
		}

		protected String getColumnKey(String columnName) {
			return columnName.toLowerCase();
		}

		/**
		 * 基于JdbcUtils.getResultSetValue(ResultSet rs, int index)只是将其中Meta的访问变为访问本地缓存
		 * @param index：the column index，the first column is 1, the second is 2, ... 
		 */
		private Object getResultSetValue(TableMetaData tmd, ResultSet rs, int index) throws SQLException {
			Object obj = rs.getObject(index);
			if (obj instanceof Blob) {
				obj = rs.getBytes(index);
			}
			else if (obj instanceof Clob) {
				obj = rs.getString(index);
			}
			else if (obj != null && obj.getClass().getName().startsWith("oracle.sql.TIMESTAMP")) {
				obj = rs.getTimestamp(index);
			}
			else if (obj != null && obj.getClass().getName().startsWith("oracle.sql.DATE")) {
				String metaDataClassName = tmd.columns[index-1].className;
				if ("java.sql.Timestamp".equals(metaDataClassName) ||
						"oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
					obj = rs.getTimestamp(index);
				}
				else {
					obj = rs.getDate(index);
				}
			}
			else if (obj != null && obj instanceof java.sql.Date) {
				if ("java.sql.Timestamp".equals(tmd.columns[index-1].className)) {
					obj = rs.getTimestamp(index);
				}
			}
			return obj;			
		}
	}

	/**
	 * @param tableName 小写表名
	 */
	private void initMetaData(String tableName, ResultSetMetaData rsmd) {
		try {
			int columnCount = rsmd.getColumnCount();
			String[] columnNames = new String[columnCount];
			ColumnMetaData[] columns = new ColumnMetaData[columnCount];
			for (int i = 1; i <= columnCount; i++) {
				columnNames[i-1] = rsmd.getColumnName(i).toLowerCase();
				int sqlType = rsmd.getColumnType(i);
				if(sqlType == java.sql.Types.DATE){
					sqlType = java.sql.Types.TIMESTAMP;
				}
				int scale = rsmd.getScale(i);
				String className = rsmd.getColumnClassName(i);
				columns[i-1] = new ColumnMetaData(sqlType, scale, className);
			}
			TableMetaData tmd = new TableMetaData(columnNames, columns);
			this.tableMetaDatas.putIfAbsent(tableName, tmd);
		} catch (SQLException e) {
			log.warn("Fetch Metadata from resultSet failed.", e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T convert(Object obj){
		return (T)obj;
	}

	/*public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}*/
}
