/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;


/**
 * preparedStatement 包装类
 *
 * @author shenxun
 *
 */
public class TPreparedStatementWrapper extends TStatementWrapper implements java.sql.PreparedStatement {
	protected final String sql;

	public TPreparedStatementWrapper(Statement targetStatement, TConnectionWrapper connectionWrapper,
			TDataSourceWrapper dataSourceWrapper, String sql) {
		super(targetStatement, connectionWrapper, dataSourceWrapper);
		this.sql = sql;
	}

	public void addBatch() throws SQLException {
		((PreparedStatement) targetStatement).addBatch();
	}

	public void clearParameters() throws SQLException {
		((PreparedStatement) targetStatement).clearParameters();
	}

	public boolean execute() throws SQLException {
		//SqlType sqlType =  getSqlType(sql);
		//recordTimes(sqlType);
		//return ((PreparedStatement)targetStatement).execute();

		SqlType sqlType = getSqlType(sql);
		if (sqlType == SqlType.SELECT || sqlType == SqlType.SELECT_FOR_UPDATE || sqlType == SqlType.SHOW) {
			executeQuery();
			return true;
		} else if (sqlType == SqlType.INSERT || sqlType == SqlType.UPDATE || sqlType == SqlType.DELETE
				|| sqlType == SqlType.REPLACE|| sqlType== SqlType.TRUNCATE|| sqlType == SqlType.CREATE
				|| sqlType== SqlType.DROP|| sqlType == SqlType.LOAD|| sqlType== SqlType.MERGE) {
			executeUpdate();
			return false;
		} else {
			throw new SQLException("only select, insert, update, delete,replace,truncate,create,drop,load,merge sql is supported");
		}
	}

	public ResultSet executeQuery() throws SQLException {
		ensureResultSetIsEmpty();
		recordReadTimes();
		increaseConcurrentRead();
		long time0 = System.currentTimeMillis();
		Exception e0 = null;

		try {
			currentResultSet = new TResultSetWrapper(this, ((PreparedStatement) targetStatement).executeQuery());
			return currentResultSet;
		} catch (SQLException e) {
			decreaseConcurrentRead();
			e0 = e;
			throw e;
		} finally {
			recordSql(sql, System.currentTimeMillis() - time0, e0);
		}
	}

	public int executeUpdate() throws SQLException {
		ensureResultSetIsEmpty();
		recordWriteTimes();
		increaseConcurrentWrite();
		long time0 = System.currentTimeMillis();
		Exception e0 = null;

		try {
			return ((PreparedStatement) targetStatement).executeUpdate();
		} catch (SQLException e) {
			e0 = e;
			throw e;
		} finally {
			decreaseConcurrentWrite();
			recordSql(sql, System.currentTimeMillis() - time0, e0);
		}
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		//这里直接返回元数据
		return ((PreparedStatement) targetStatement).getMetaData();
	}

	public ParameterMetaData getParameterMetaData() throws SQLException {
		//这里直接返回原数据
		return ((PreparedStatement) targetStatement).getParameterMetaData();
	}

	public void setArray(int i, Array x) throws SQLException {

		((PreparedStatement) targetStatement).setArray(i, x);
	}

	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
		((PreparedStatement) targetStatement).setAsciiStream(parameterIndex, x, length);
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		((PreparedStatement) targetStatement).setBigDecimal(parameterIndex, x);
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
		((PreparedStatement) targetStatement).setBinaryStream(parameterIndex, x, length);
	}

	public void setBlob(int i, Blob x) throws SQLException {
		((PreparedStatement) targetStatement).setBlob(i, x);
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		((PreparedStatement) targetStatement).setBoolean(parameterIndex, x);
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		((PreparedStatement) targetStatement).setByte(parameterIndex, x);
	}

	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		((PreparedStatement) targetStatement).setBytes(parameterIndex, x);
	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
		((PreparedStatement) targetStatement).setCharacterStream(parameterIndex, reader, length);
	}

	public void setClob(int i, Clob x) throws SQLException {
		((PreparedStatement) targetStatement).setClob(i, x);
	}

	public void setDate(int parameterIndex, Date x) throws SQLException {
		((PreparedStatement) targetStatement).setDate(parameterIndex, x);
	}

	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		((PreparedStatement) targetStatement).setDate(parameterIndex, x, cal);
	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		((PreparedStatement) targetStatement).setDouble(parameterIndex, x);
	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		((PreparedStatement) targetStatement).setFloat(parameterIndex, x);
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		((PreparedStatement) targetStatement).setInt(parameterIndex, x);
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		((PreparedStatement) targetStatement).setLong(parameterIndex, x);
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		((PreparedStatement) targetStatement).setNull(parameterIndex, sqlType);
	}

	public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
		((PreparedStatement) targetStatement).setNull(paramIndex, sqlType, typeName);
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		((PreparedStatement) targetStatement).setObject(parameterIndex, x);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		((PreparedStatement) targetStatement).setObject(parameterIndex, x, targetSqlType);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
		((PreparedStatement) targetStatement).setObject(parameterIndex, x, targetSqlType, scale);
	}

	public void setRef(int i, Ref x) throws SQLException {
		((PreparedStatement) targetStatement).setRef(i, x);
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		((PreparedStatement) targetStatement).setShort(parameterIndex, x);
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		((PreparedStatement) targetStatement).setString(parameterIndex, x);
	}

	public void setTime(int parameterIndex, Time x) throws SQLException {
		((PreparedStatement) targetStatement).setTime(parameterIndex, x);
	}

	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
		((PreparedStatement) targetStatement).setTime(parameterIndex, x, cal);
	}

	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
		((PreparedStatement) targetStatement).setTimestamp(parameterIndex, x);
	}

	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
		((PreparedStatement) targetStatement).setTimestamp(parameterIndex, x, cal);
	}

	public void setURL(int parameterIndex, URL x) throws SQLException {
		((PreparedStatement) targetStatement).setURL(parameterIndex, x);
	}

	@Deprecated
	public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
		((PreparedStatement) targetStatement).setUnicodeStream(parameterIndex, x, length);
	}

	//
	//	public Connection getConnection() throws SQLException {
	//		return connectionWrapper;
	//	}

	public boolean isClosed() throws SQLException {
		return ((PreparedStatement) targetStatement).isClosed();
	}

	public void setPoolable(boolean poolable) throws SQLException {
		((PreparedStatement) targetStatement).setPoolable(poolable);
	}

	public boolean isPoolable() throws SQLException {
		return ((PreparedStatement) targetStatement).isPoolable();
	}

	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		try {
			return (T) this;
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.getClass().isAssignableFrom(iface);
	}

	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		((PreparedStatement) targetStatement).setRowId(parameterIndex, x);
	}

	public void setNString(int parameterIndex, String value) throws SQLException {
		((PreparedStatement) targetStatement).setNString(parameterIndex, value);
	}

	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
		((PreparedStatement) targetStatement).setNCharacterStream(parameterIndex, value, length);
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		((PreparedStatement) targetStatement).setNClob(parameterIndex, value);
	}

	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
		((PreparedStatement) targetStatement).setClob(parameterIndex, reader, length);

	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		((PreparedStatement) targetStatement).setBlob(parameterIndex, inputStream, length);
	}

	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
		((PreparedStatement) targetStatement).setNClob(parameterIndex, reader, length);
	}

	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
		((PreparedStatement) targetStatement).setSQLXML(parameterIndex, xmlObject);
	}

	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
		((PreparedStatement) targetStatement).setAsciiStream(parameterIndex, x, length);
	}

	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
		((PreparedStatement) targetStatement).setBinaryStream(parameterIndex, x, length);
	}

	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
		((PreparedStatement) targetStatement).setCharacterStream(parameterIndex, reader, length);
	}

	public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
		((PreparedStatement) targetStatement).setAsciiStream(parameterIndex, x);
	}

	public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
		((PreparedStatement) targetStatement).setBinaryStream(parameterIndex, x);
	}

	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		((PreparedStatement) targetStatement).setCharacterStream(parameterIndex, reader);
	}

	public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
		((PreparedStatement) targetStatement).setNCharacterStream(parameterIndex, value);
	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		((PreparedStatement) targetStatement).setClob(parameterIndex, reader);
	}

	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
		((PreparedStatement) targetStatement).setBlob(parameterIndex, inputStream);
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		((PreparedStatement) targetStatement).setNClob(parameterIndex, reader);
	}

}
