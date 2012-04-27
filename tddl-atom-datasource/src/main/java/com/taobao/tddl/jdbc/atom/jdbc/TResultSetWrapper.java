/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * ResultSet包装类
 * 
 * @author shenxun
 *
 */
public class TResultSetWrapper implements ResultSet {
	private final TStatementWrapper tStatementWrapper;
	//	private final TConnectionWrapper tConnectionWrapper;
	private final ResultSet targetResultSet;

	public TResultSetWrapper(TStatementWrapper tStatementWrapper, ResultSet resultSet) {
		super();
		this.tStatementWrapper = tStatementWrapper;
		//		this.tConnectionWrapper = tConnectionWrapper;
		this.targetResultSet = resultSet;
	}

	public boolean absolute(int row) throws SQLException {
		return this.targetResultSet.absolute(row);
	}

	public void afterLast() throws SQLException {
		this.targetResultSet.afterLast();
	}

	public void beforeFirst() throws SQLException {
		this.targetResultSet.beforeFirst();
	}

	public void cancelRowUpdates() throws SQLException {
		this.targetResultSet.cancelRowUpdates();
	}

	public void clearWarnings() throws SQLException {
		this.targetResultSet.clearWarnings();
	}

	private boolean isClosed = false;

	public void close() throws SQLException {
		if (isClosed) {
			return;
		}
		tStatementWrapper.decreaseConcurrentRead();
		isClosed = true;
		this.targetResultSet.close();
	}

	public void deleteRow() throws SQLException {
		this.targetResultSet.deleteRow();
	}

	public int findColumn(String columnName) throws SQLException {
		return this.targetResultSet.findColumn(columnName);
	}

	public boolean first() throws SQLException {
		return this.targetResultSet.first();
	}

	public Array getArray(int i) throws SQLException {
		return this.targetResultSet.getArray(i);
	}

	public Array getArray(String colName) throws SQLException {
		return this.targetResultSet.getArray(colName);
	}

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return this.targetResultSet.getAsciiStream(columnIndex);
	}

	public InputStream getAsciiStream(String columnName) throws SQLException {
		return this.targetResultSet.getAsciiStream(columnName);
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return this.targetResultSet.getBigDecimal(columnIndex);
	}

	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		return this.targetResultSet.getBigDecimal(columnName);
	}

	@Deprecated
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return this.targetResultSet.getBigDecimal(columnIndex, scale);
	}

	@Deprecated
	public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
		return this.targetResultSet.getBigDecimal(columnName, scale);
	}

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return this.targetResultSet.getBinaryStream(columnIndex);
	}

	public InputStream getBinaryStream(String columnName) throws SQLException {
		return this.targetResultSet.getBinaryStream(columnName);
	}

	public Blob getBlob(int i) throws SQLException {
		return this.targetResultSet.getBlob(i);
	}

	public Blob getBlob(String colName) throws SQLException {
		return this.targetResultSet.getBlob(colName);
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		return this.targetResultSet.getBoolean(columnIndex);
	}

	public boolean getBoolean(String columnName) throws SQLException {
		return this.targetResultSet.getBoolean(columnName);
	}

	public byte getByte(int columnIndex) throws SQLException {
		return this.targetResultSet.getByte(columnIndex);
	}

	public byte getByte(String columnName) throws SQLException {
		return this.targetResultSet.getByte(columnName);
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		return this.targetResultSet.getBytes(columnIndex);
	}

	public byte[] getBytes(String columnName) throws SQLException {
		return this.targetResultSet.getBytes(columnName);
	}

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return this.targetResultSet.getCharacterStream(columnIndex);
	}

	public Reader getCharacterStream(String columnName) throws SQLException {
		return this.targetResultSet.getCharacterStream(columnName);
	}

	public Clob getClob(int i) throws SQLException {
		return this.targetResultSet.getClob(i);
	}

	public Clob getClob(String colName) throws SQLException {
		return this.targetResultSet.getClob(colName);
	}

	public int getConcurrency() throws SQLException {
		return this.targetResultSet.getConcurrency();
	}

	public String getCursorName() throws SQLException {
		return this.targetResultSet.getCursorName();
	}

	public Date getDate(int columnIndex) throws SQLException {
		return this.targetResultSet.getDate(columnIndex);
	}

	public Date getDate(String columnName) throws SQLException {
		return this.targetResultSet.getDate(columnName);
	}

	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return this.targetResultSet.getDate(columnIndex, cal);
	}

	public Date getDate(String columnName, Calendar cal) throws SQLException {
		return this.targetResultSet.getDate(columnName, cal);
	}

	public double getDouble(int columnIndex) throws SQLException {
		return this.targetResultSet.getDouble(columnIndex);
	}

	public double getDouble(String columnName) throws SQLException {
		return this.targetResultSet.getDouble(columnName);

	}

	public int getFetchDirection() throws SQLException {
		return this.targetResultSet.getFetchDirection();
	}

	public int getFetchSize() throws SQLException {
		return this.targetResultSet.getFetchSize();
	}

	public float getFloat(int columnIndex) throws SQLException {
		return this.targetResultSet.getFloat(columnIndex);
	}

	public float getFloat(String columnName) throws SQLException {
		return this.targetResultSet.getFloat(columnName);
	}

	public int getInt(int columnIndex) throws SQLException {
		return this.targetResultSet.getInt(columnIndex);
	}

	public int getInt(String columnName) throws SQLException {
		return this.targetResultSet.getInt(columnName);
	}

	public long getLong(int columnIndex) throws SQLException {
		return this.targetResultSet.getLong(columnIndex);
	}

	public long getLong(String columnName) throws SQLException {
		return this.targetResultSet.getLong(columnName);
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		//直接返回resultSetMetaData
		return this.targetResultSet.getMetaData();
	}

	public Object getObject(int columnIndex) throws SQLException {
		return this.targetResultSet.getObject(columnIndex);
	}

	public Object getObject(String columnName) throws SQLException {
		return this.targetResultSet.getObject(columnName);
	}

	public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
		return this.targetResultSet.getObject(i, map);
	}

	public Object getObject(String colName, Map<String, Class<?>> map) throws SQLException {
		return this.targetResultSet.getObject(colName, map);
	}

	public Ref getRef(int i) throws SQLException {
		return this.targetResultSet.getRef(i);
	}

	public Ref getRef(String colName) throws SQLException {
		return this.targetResultSet.getRef(colName);
	}

	public int getRow() throws SQLException {
		return this.targetResultSet.getRow();
	}

	public short getShort(int columnIndex) throws SQLException {
		return this.targetResultSet.getShort(columnIndex);
	}

	public short getShort(String columnName) throws SQLException {
		return this.targetResultSet.getShort(columnName);
	}

	public Statement getStatement() throws SQLException {
		return tStatementWrapper;
	}

	public String getString(int columnIndex) throws SQLException {
		return this.targetResultSet.getString(columnIndex);
	}

	public String getString(String columnName) throws SQLException {
		return this.targetResultSet.getString(columnName);
	}

	public Time getTime(int columnIndex) throws SQLException {
		return this.targetResultSet.getTime(columnIndex);
	}

	public Time getTime(String columnName) throws SQLException {
		return this.targetResultSet.getTime(columnName);
	}

	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return this.targetResultSet.getTime(columnIndex, cal);
	}

	public Time getTime(String columnName, Calendar cal) throws SQLException {
		return this.targetResultSet.getTime(columnName, cal);
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return this.targetResultSet.getTimestamp(columnIndex);
	}

	public Timestamp getTimestamp(String columnName) throws SQLException {
		return this.targetResultSet.getTimestamp(columnName);
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		return this.targetResultSet.getTimestamp(columnIndex, cal);
	}

	public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
		return this.targetResultSet.getTimestamp(columnName, cal);
	}

	public int getType() throws SQLException {
		return this.targetResultSet.getType();
	}

	public URL getURL(int columnIndex) throws SQLException {
		return this.targetResultSet.getURL(columnIndex);
	}

	public URL getURL(String columnName) throws SQLException {
		return this.targetResultSet.getURL(columnName);
	}

	@Deprecated
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return this.targetResultSet.getUnicodeStream(columnIndex);
	}

	@Deprecated
	public InputStream getUnicodeStream(String columnName) throws SQLException {
		return this.targetResultSet.getUnicodeStream(columnName);
	}

	public SQLWarning getWarnings() throws SQLException {
		return this.targetResultSet.getWarnings();
	}

	public void insertRow() throws SQLException {
		this.targetResultSet.insertRow();
	}

	public boolean isAfterLast() throws SQLException {
		return this.targetResultSet.isAfterLast();
	}

	public boolean isBeforeFirst() throws SQLException {
		return this.targetResultSet.isBeforeFirst();
	}

	public boolean isFirst() throws SQLException {
		return this.targetResultSet.isFirst();
	}

	public boolean isLast() throws SQLException {
		return this.targetResultSet.isLast();
	}

	public boolean last() throws SQLException {
		return this.targetResultSet.last();
	}

	public void moveToCurrentRow() throws SQLException {
		this.targetResultSet.moveToCurrentRow();
	}

	public void moveToInsertRow() throws SQLException {
		this.targetResultSet.moveToInsertRow();
	}

	public boolean next() throws SQLException {
		return this.targetResultSet.next();
	}

	public boolean previous() throws SQLException {
		return this.targetResultSet.previous();
	}

	public void refreshRow() throws SQLException {
		this.targetResultSet.refreshRow();
	}

	public boolean relative(int rows) throws SQLException {
		return this.targetResultSet.relative(rows);
	}

	public boolean rowDeleted() throws SQLException {
		return this.targetResultSet.rowDeleted();
	}

	public boolean rowInserted() throws SQLException {
		return this.targetResultSet.rowInserted();
	}

	public boolean rowUpdated() throws SQLException {
		return this.targetResultSet.rowUpdated();
	}

	public void setFetchDirection(int direction) throws SQLException {
		this.targetResultSet.setFetchDirection(direction);
	}

	public void setFetchSize(int rows) throws SQLException {
		this.targetResultSet.setFetchSize(rows);
	}

	public void updateArray(int columnIndex, Array x) throws SQLException {
		this.targetResultSet.updateArray(columnIndex, x);
	}

	public void updateArray(String columnName, Array x) throws SQLException {
		this.targetResultSet.updateArray(columnName, x);
	}

	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		this.targetResultSet.updateAsciiStream(columnIndex, x, length);
	}

	public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
		this.targetResultSet.updateAsciiStream(columnName, x, length);
	}

	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		this.targetResultSet.updateBigDecimal(columnIndex, x);
	}

	public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
		this.targetResultSet.updateBigDecimal(columnName, x);
	}

	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		this.targetResultSet.updateBinaryStream(columnIndex, x, length);
	}

	public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
		this.targetResultSet.updateBinaryStream(columnName, x, length);
	}

	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		this.targetResultSet.updateBlob(columnIndex, x);
	}

	public void updateBlob(String columnName, Blob x) throws SQLException {
		this.targetResultSet.updateBlob(columnName, x);
	}

	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		this.targetResultSet.updateBoolean(columnIndex, x);
	}

	public void updateBoolean(String columnName, boolean x) throws SQLException {
		this.targetResultSet.updateBoolean(columnName, x);
	}

	public void updateByte(int columnIndex, byte x) throws SQLException {
		this.targetResultSet.updateByte(columnIndex, x);
	}

	public void updateByte(String columnName, byte x) throws SQLException {
		this.targetResultSet.updateByte(columnName, x);
	}

	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		this.targetResultSet.updateBytes(columnIndex, x);
	}

	public void updateBytes(String columnName, byte[] x) throws SQLException {
		this.targetResultSet.updateBytes(columnName, x);
	}

	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		this.targetResultSet.updateCharacterStream(columnIndex, x, length);
	}

	public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
		this.targetResultSet.updateCharacterStream(columnName, reader, length);
	}

	public void updateClob(int columnIndex, Clob x) throws SQLException {
		this.targetResultSet.updateClob(columnIndex, x);
	}

	public void updateClob(String columnName, Clob x) throws SQLException {
		this.targetResultSet.updateClob(columnName, x);
	}

	public void updateDate(int columnIndex, Date x) throws SQLException {
		this.targetResultSet.updateDate(columnIndex, x);
	}

	public void updateDate(String columnName, Date x) throws SQLException {
		this.targetResultSet.updateDate(columnName, x);
	}

	public void updateDouble(int columnIndex, double x) throws SQLException {
		this.targetResultSet.updateDouble(columnIndex, x);
	}

	public void updateDouble(String columnName, double x) throws SQLException {
		this.targetResultSet.updateDouble(columnName, x);
	}

	public void updateFloat(int columnIndex, float x) throws SQLException {
		this.targetResultSet.updateFloat(columnIndex, x);
	}

	public void updateFloat(String columnName, float x) throws SQLException {
		this.targetResultSet.updateFloat(columnName, x);
	}

	public void updateInt(int columnIndex, int x) throws SQLException {
		this.targetResultSet.updateInt(columnIndex, x);
	}

	public void updateInt(String columnName, int x) throws SQLException {
		this.targetResultSet.updateInt(columnName, x);
	}

	public void updateLong(int columnIndex, long x) throws SQLException {
		this.targetResultSet.updateLong(columnIndex, x);
	}

	public void updateLong(String columnName, long x) throws SQLException {
		this.targetResultSet.updateLong(columnName, x);
	}

	public void updateNull(int columnIndex) throws SQLException {
		this.targetResultSet.updateNull(columnIndex);
	}

	public void updateNull(String columnName) throws SQLException {
		this.targetResultSet.updateNull(columnName);
	}

	public void updateObject(int columnIndex, Object x) throws SQLException {
		this.targetResultSet.updateObject(columnIndex, x);
	}

	public void updateObject(String columnName, Object x) throws SQLException {
		this.targetResultSet.updateObject(columnName, x);
	}

	public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
		this.targetResultSet.updateObject(columnIndex, x);
	}

	public void updateObject(String columnName, Object x, int scale) throws SQLException {
		this.targetResultSet.updateObject(columnName, x, scale);
	}

	public void updateRef(int columnIndex, Ref x) throws SQLException {
		this.targetResultSet.updateRef(columnIndex, x);
	}

	public void updateRef(String columnName, Ref x) throws SQLException {
		this.targetResultSet.updateRef(columnName, x);
	}

	public void updateRow() throws SQLException {
		this.targetResultSet.updateRow();
	}

	public void updateShort(int columnIndex, short x) throws SQLException {
		this.targetResultSet.updateShort(columnIndex, x);
	}

	public void updateShort(String columnName, short x) throws SQLException {
		this.targetResultSet.updateShort(columnName, x);
	}

	public void updateString(int columnIndex, String x) throws SQLException {
		this.targetResultSet.updateString(columnIndex, x);
	}

	public void updateString(String columnName, String x) throws SQLException {
		this.targetResultSet.updateString(columnName, x);
	}

	public void updateTime(int columnIndex, Time x) throws SQLException {
		this.targetResultSet.updateTime(columnIndex, x);
	}

	public void updateTime(String columnName, Time x) throws SQLException {
		this.targetResultSet.updateTime(columnName, x);
	}

	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		this.targetResultSet.updateTimestamp(columnIndex, x);
	}

	public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
		this.targetResultSet.updateTimestamp(columnName, x);
	}

	public boolean wasNull() throws SQLException {
		return this.targetResultSet.wasNull();
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.getClass().isAssignableFrom(iface);
	}

	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		try {
			return (T) this;
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}

	public RowId getRowId(int columnIndex) throws SQLException {
		return this.targetResultSet.getRowId(columnIndex);
	}

	public RowId getRowId(String columnLabel) throws SQLException {
		return this.targetResultSet.getRowId(columnLabel);
	}

	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		this.targetResultSet.updateRowId(columnIndex, x);
	}

	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		this.targetResultSet.updateRowId(columnLabel, x);
	}

	public int getHoldability() throws SQLException {
		return this.targetResultSet.getHoldability();
	}

	public boolean isClosed() throws SQLException {
		return this.targetResultSet.isClosed();
	}

	public void updateNString(int columnIndex, String nString) throws SQLException {
		this.targetResultSet.updateNString(columnIndex, nString);
	}

	public void updateNString(String columnLabel, String nString) throws SQLException {
		this.targetResultSet.updateNString(columnLabel, nString);
	}

	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		this.targetResultSet.updateNClob(columnIndex, nClob);
	}

	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		this.targetResultSet.updateNClob(columnLabel, nClob);
	}

	public NClob getNClob(int columnIndex) throws SQLException {
		return this.targetResultSet.getNClob(columnIndex);
	}

	public NClob getNClob(String columnLabel) throws SQLException {
		return this.targetResultSet.getNClob(columnLabel);
	}

	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return this.targetResultSet.getSQLXML(columnIndex);
	}

	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return this.targetResultSet.getSQLXML(columnLabel);
	}

	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		this.targetResultSet.updateSQLXML(columnIndex, xmlObject);
	}

	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		this.targetResultSet.updateSQLXML(columnLabel, xmlObject);
	}

	public String getNString(int columnIndex) throws SQLException {
		return this.targetResultSet.getNString(columnIndex);
	}

	public String getNString(String columnLabel) throws SQLException {
		return this.targetResultSet.getNString(columnLabel);
	}

	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return this.targetResultSet.getNCharacterStream(columnIndex);
	}

	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return this.targetResultSet.getNCharacterStream(columnLabel);
	}

	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		this.targetResultSet.updateNCharacterStream(columnIndex, x, length);
	}

	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		this.targetResultSet.updateNCharacterStream(columnLabel, reader);
	}

	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		this.targetResultSet.updateAsciiStream(columnIndex, x, length);
	}

	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		this.targetResultSet.updateBinaryStream(columnIndex, x, length);
	}

	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		this.targetResultSet.updateCharacterStream(columnIndex, x, length);
	}

	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		this.targetResultSet.updateAsciiStream(columnLabel, x, length);
	}

	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		this.targetResultSet.updateBinaryStream(columnLabel, x, length);
	}

	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		this.targetResultSet.updateCharacterStream(columnLabel, reader, length);
	}

	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		this.targetResultSet.updateBlob(columnIndex, inputStream, length);
	}

	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		this.targetResultSet.updateBlob(columnLabel, inputStream, length);
	}

	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		this.targetResultSet.updateClob(columnIndex, reader, length);
	}

	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		this.targetResultSet.updateClob(columnLabel, reader, length);
	}

	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		this.targetResultSet.updateNClob(columnIndex, reader, length);
	}

	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		this.targetResultSet.updateNClob(columnLabel, reader, length);
	}

	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		this.targetResultSet.updateNCharacterStream(columnIndex, x);
	}

	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		this.targetResultSet.updateNCharacterStream(columnLabel, reader);
	}

	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		this.targetResultSet.updateAsciiStream(columnIndex, x);
	}

	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		this.targetResultSet.updateBinaryStream(columnIndex, x);
	}

	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		this.targetResultSet.updateCharacterStream(columnIndex, x);
	}

	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		this.targetResultSet.updateAsciiStream(columnLabel, x);
	}

	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		this.targetResultSet.updateBinaryStream(columnLabel, x);
	}

	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		this.targetResultSet.updateCharacterStream(columnLabel, reader);
	}

	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		this.targetResultSet.updateBlob(columnIndex, inputStream);
	}

	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		this.targetResultSet.updateBlob(columnLabel, inputStream);
	}

	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		this.targetResultSet.updateClob(columnIndex, reader);
	}

	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		this.targetResultSet.updateClob(columnLabel, reader);
	}

	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		this.targetResultSet.updateNClob(columnIndex, reader);
	}

	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		this.targetResultSet.updateNClob(columnLabel, reader);
	}

}
