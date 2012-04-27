/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.mockdatasource;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.taobao.tddl.common.mockdatasource.MockDataSource.ExecuteInfo;
import com.taobao.tddl.common.mockdatasource.MockDataSource.QueryResult;


public class MockResultSet implements ResultSet{
	private final MockDataSource mds; 
	private ResultSetMetaData resultSetMetaData;
	public final Map<String/*列名*/,Integer/*列序号*/> columns; 
	public final List<Object[]> rows;
	private int cursor = -1;
	public MockResultSet(MockDataSource mockDataSource, Map<String,Integer> columns, List<Object[]> values){
		this.mds = mockDataSource;
		this.columns = columns;
		this.rows = values;
		resultSetMetaData = new MockResultSetMetaData(columns);
	}
	public MockResultSet(MockDataSource mockDataSource, QueryResult res) {
		this.mds = mockDataSource;
		if (res != null) {
			this.columns = res.columns;
			this.rows = res.rows;
		} else {
			this.columns = new HashMap<String,Integer>(0);
			this.rows = new ArrayList<Object[]>(0);
		}
		resultSetMetaData = new MockResultSetMetaData(columns);
	}
	public boolean absolute(int row) throws SQLException {
		throw new UnsupportedOperationException("absolute");
	}

	public void afterLast() throws SQLException {
		throw new UnsupportedOperationException("afterLast");
	}

	public void beforeFirst() throws SQLException {
		throw new UnsupportedOperationException("beforeFirst");
	}

	public void cancelRowUpdates() throws SQLException {
		throw new UnsupportedOperationException("cancelRowUpdates");
	}

	public void clearWarnings() throws SQLException {
		throw new UnsupportedOperationException("clearWarnings");
	}

	private boolean closed;
	private int closeInvocatingTimes = 0;
	
	public void close() throws SQLException {
		closed = true;
		closeInvocatingTimes ++;
		
	}
	protected void checkClose() throws SQLException{
		if(closed)
			throw new SQLException("closed");
	}
	public void deleteRow() throws SQLException {
		throw new UnsupportedOperationException("deleteRow");
	}

	public void closeInternal(boolean removeThis) throws SQLException{
		throw new UnsupportedOperationException("closeInternal");
	}

	public int findColumn(String columnName) throws SQLException {
		throw new UnsupportedOperationException("findColumn");
	}

	public boolean first() throws SQLException {
		throw new UnsupportedOperationException("first");
	}

	
	public Array getArray(int i) throws SQLException {
		return (Array) getObject(i);
	}

//	public Object getVal(int i ){
//		return result[i];
//	}
	public Array getArray(String colName) throws SQLException {
		throw new UnsupportedOperationException("getArray(String colName)");
	}

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("getAsciiStream(int columnIndex)");
	}

	public InputStream getAsciiStream(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getAsciiStream(String columnName)");
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return (BigDecimal) getObject(columnIndex);
	}

	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getBigDecimal(String columnName)");
	}

	public BigDecimal getBigDecimal(int columnIndex, int scale)
			throws SQLException {
		throw new UnsupportedOperationException("getBigDecimal(int columnIndex, int scale)");
	}

	public BigDecimal getBigDecimal(String columnName, int scale)
			throws SQLException {
		throw new UnsupportedOperationException("getBigDecimal(String columnName, int scale)");
	}

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return (InputStream) getObject(columnIndex);
	}

	public InputStream getBinaryStream(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getBinaryStream(String columnName)");
	}

	public Blob getBlob(int i) throws SQLException {
		return (Blob) getObject(i);
	}

	public Blob getBlob(String colName) throws SQLException {
		throw new UnsupportedOperationException("getBlob(String colName)");
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		return (Boolean) getObject(columnIndex);
	}

	public boolean getBoolean(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getBoolean(String columnName)");
	}

	public byte getByte(int columnIndex) throws SQLException {
		return (Byte) getObject(columnIndex);
	}

	public byte getByte(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getByte(String columnName)");
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		return (byte[]) getObject(columnIndex);
	}

	public byte[] getBytes(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getBytes(String columnName)");
	}

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return (Reader) getObject(columnIndex);
	}

	public Reader getCharacterStream(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getCharacterStream(String columnName)");
	}

	public Clob getClob(int i) throws SQLException {
		return (Clob) getObject(i);
	}

	public Clob getClob(String colName) throws SQLException {
		throw new UnsupportedOperationException("getClob(String colName)");
	}

	public int getConcurrency() throws SQLException {
		throw new UnsupportedOperationException("getConcurrency");
	}

	public String getCursorName() throws SQLException {
		throw new UnsupportedOperationException("getCursorName");
	}

	public Date getDate(int columnIndex) throws SQLException {
		return (Date) getObject(columnIndex);
	}

	public Date getDate(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getDate(String columnName)");
	}

	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		throw new UnsupportedOperationException("getDate(int columnIndex, Calendar cal)");
	}

	public Date getDate(String columnName, Calendar cal) throws SQLException {
		throw new UnsupportedOperationException("getDate(String columnName, Calendar cal)");
	}

	public double getDouble(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("getDouble(int columnIndex)");
	}

	public double getDouble(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getDouble(String columnName)");
	}

	public int getFetchDirection() throws SQLException {
		throw new UnsupportedOperationException("getFetchDirection");
	}

	public int getFetchSize() throws SQLException {
		throw new UnsupportedOperationException("getFetchSize");
	}

	public float getFloat(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("getFloat(int columnIndex)");
	}

	public float getFloat(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getFloat(String columnName)");
	}

	public int getInt(int columnIndex) throws SQLException {
		Long value=(Long) this.rows.get(this.cursor)[columnIndex-1];
		return value.intValue();
	}

	public int getInt(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getInt(String columnName)");
	}

	public long getLong(int columnIndex) throws SQLException {
		Long value=(Long) this.rows.get(this.cursor)[columnIndex-1];
		return value.longValue();
	}

	public long getLong(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getLong(String columnName)");
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		MockDataSource.record(new ExecuteInfo(this.mds, "ResultSet.getMetaData", null, null));
		return this.resultSetMetaData;
	}

	public Object getObject(int columnIndex) throws SQLException {
		//throw new UnsupportedOperationException("getObject(int columnIndex)");
		return this.rows.get(cursor)[columnIndex-1];
	}

	public Object getObject(String columnName) throws SQLException {
		//throw new UnsupportedOperationException("getObject(String columnName)");
		return this.rows.get(cursor)[this.columns.get(columnName)-1];
	}

	public Object getObject(int i, Map<String, Class<?>> map)
			throws SQLException {
		throw new UnsupportedOperationException("getObject(int i, Map<String, Class<?>> map)");
	}

	public Object getObject(String colName, Map<String, Class<?>> map)
			throws SQLException {
		throw new UnsupportedOperationException("getObject(String colName, Map<String, Class<?>> map)");
	}

	public Ref getRef(int i) throws SQLException {
		throw new UnsupportedOperationException("getRef(int i)");
	}

	public Ref getRef(String colName) throws SQLException {
		throw new UnsupportedOperationException("getRef(String colName)");
	}

	public int getRow() throws SQLException {
		throw new UnsupportedOperationException("getRow");
	}

	public short getShort(int columnIndex) throws SQLException {
		return (Short)this.getObject(columnIndex);
	}

	public short getShort(String columnName) throws SQLException {
		return (Short)this.getObject(columnName);
	}

	public Statement getStatement() throws SQLException {
		throw new UnsupportedOperationException("getStatement");
	}

	public String getString(int columnIndex) throws SQLException {
		return (String)this.getObject(columnIndex);
	}

	public String getString(String columnName) throws SQLException {
		return (String)this.getObject(columnName);
	}

	public Time getTime(int columnIndex) throws SQLException {
		return (Time)this.getObject(columnIndex);
	}

	public Time getTime(String columnName) throws SQLException {
		return (Time)this.getObject(columnName);
	}

	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		throw new UnsupportedOperationException("getTime(int columnIndex, Calendar cal)");
	}

	public Time getTime(String columnName, Calendar cal) throws SQLException {
		throw new UnsupportedOperationException("getTime(String columnName, Calendar cal)");
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return (Timestamp)this.getObject(columnIndex);
	}

	public Timestamp getTimestamp(String columnName) throws SQLException {
		return (Timestamp)this.getObject(columnName);
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws SQLException {
		throw new UnsupportedOperationException("getTimestamp(int columnIndex, Calendar cal)");
	}

	public Timestamp getTimestamp(String columnName, Calendar cal)
			throws SQLException {
		throw new UnsupportedOperationException("getTimestamp(String columnName, Calendar cal)");
	}

	/**
	 * iBatis会用到这个方法
	 */
	public int getType() throws SQLException {
		return ResultSet.TYPE_FORWARD_ONLY;
	}

	public URL getURL(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("getURL(int columnIndex)");
	}

	public URL getURL(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getURL(String columnName)");
	}

	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("getUnicodeStream(int columnIndex)");
	}

	public InputStream getUnicodeStream(String columnName) throws SQLException {
		throw new UnsupportedOperationException("getUnicodeStream(String columnName)");
	}

	public SQLWarning getWarnings() throws SQLException {
		throw new UnsupportedOperationException("getWarnings");
	}

	public void insertRow() throws SQLException {
		throw new UnsupportedOperationException("insertRow");
	}

	public boolean isAfterLast() throws SQLException {
		throw new UnsupportedOperationException("isAfterLast");
	}

	public boolean isBeforeFirst() throws SQLException {
		throw new UnsupportedOperationException("isBeforeFirst");
	}

	public boolean isFirst() throws SQLException {
		throw new UnsupportedOperationException("isFirst");
	}

	public boolean isLast() throws SQLException {
		throw new UnsupportedOperationException("isLast");
	}

	public boolean last() throws SQLException {
		throw new UnsupportedOperationException("last");
	}

	public void moveToCurrentRow() throws SQLException {
		throw new UnsupportedOperationException("moveToCurrentRow");
	}

	public void moveToInsertRow() throws SQLException {
		throw new UnsupportedOperationException("moveToInsertRow");
	}

	private int nextInvokingTimes = 0;
	protected boolean hasNext() {
		cursor++;
		return cursor < this.rows.size();
	}
	private long nextSleepTime = 0;
	public boolean next() throws SQLException {
		try {
			Thread.sleep(nextSleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		nextInvokingTimes++;
		return hasNext();
	}

	public boolean previous() throws SQLException {
		throw new UnsupportedOperationException("previous");
	}

	public void refreshRow() throws SQLException {
		throw new UnsupportedOperationException("refreshRow");
	}

	public boolean relative(int rows) throws SQLException {
		throw new UnsupportedOperationException("relative");
	}

	public boolean rowDeleted() throws SQLException {
		throw new UnsupportedOperationException("rowDeleted");
	}

	public boolean rowInserted() throws SQLException {
		throw new UnsupportedOperationException("rowInserted");
	}

	public boolean rowUpdated() throws SQLException {
		throw new UnsupportedOperationException("rowUpdated");
	}

	public void setFetchDirection(int direction) throws SQLException {
		throw new UnsupportedOperationException("setFetchDirection");
	}

	public void setFetchSize(int rows) throws SQLException {
		throw new UnsupportedOperationException("setFetchSize");
	}

	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new UnsupportedOperationException("updateArray(int columnIndex, Array x)");
	}

	public void updateArray(String columnName, Array x) throws SQLException {
		throw new UnsupportedOperationException("updateArray(String columnName, Array x)");
	}

	public void updateAsciiStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		throw new UnsupportedOperationException("updateAsciiStream(int columnIndex, InputStream x, int length)");
	}

	public void updateAsciiStream(String columnName, InputStream x, int length)
			throws SQLException {
		throw new UnsupportedOperationException("updateAsciiStream(String columnName, InputStream x, int length)");
	}

	public void updateBigDecimal(int columnIndex, BigDecimal x)
			throws SQLException {
		throw new UnsupportedOperationException("updateBigDecimal(int columnIndex, BigDecimal x)");
	}

	public void updateBigDecimal(String columnName, BigDecimal x)
			throws SQLException {
		throw new UnsupportedOperationException("updateBigDecimal(String columnName, BigDecimal x)");
	}

	public void updateBinaryStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		throw new UnsupportedOperationException("updateBinaryStream(int columnIndex, InputStream x, int length)");
	}

	public void updateBinaryStream(String columnName, InputStream x, int length)
			throws SQLException {
		throw new UnsupportedOperationException("updateBinaryStream(String columnName, InputStream x, int length)");
	}

	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new UnsupportedOperationException("updateBlob(int columnIndex, Blob x)");
	}

	public void updateBlob(String columnName, Blob x) throws SQLException {
		throw new UnsupportedOperationException("updateBlob(String columnName, Blob x)");
	}

	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new UnsupportedOperationException("updateBoolean(int columnIndex, boolean x)");
	}

	public void updateBoolean(String columnName, boolean x) throws SQLException {
		throw new UnsupportedOperationException("updateBoolean(String columnName, boolean x)");
	}

	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new UnsupportedOperationException("updateByte(int columnIndex, byte x)");
	}

	public void updateByte(String columnName, byte x) throws SQLException {
		throw new UnsupportedOperationException("updateByte(String columnName, byte x)");
	}

	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		throw new UnsupportedOperationException("updateBytes(int columnIndex, byte[] x)");
	}

	public void updateBytes(String columnName, byte[] x) throws SQLException {
		throw new UnsupportedOperationException("updateBytes(String columnName, byte[] x)");
	}

	public void updateCharacterStream(int columnIndex, Reader x, int length)
			throws SQLException {
		throw new UnsupportedOperationException("updateCharacterStream(int columnIndex, Reader x, int length)");
	}

	public void updateCharacterStream(String columnName, Reader reader,
			int length) throws SQLException {
		throw new UnsupportedOperationException("updateCharacterStream(String columnName, Reader reader, int length)");
	}

	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new UnsupportedOperationException("updateClob(int columnIndex, Clob x)");
	}

	public void updateClob(String columnName, Clob x) throws SQLException {
		throw new UnsupportedOperationException("updateClob(String columnName, Clob x)");
	}

	public void updateDate(int columnIndex, Date x) throws SQLException {
		throw new UnsupportedOperationException("updateDate(int columnIndex, Date x)");
	}

	public void updateDate(String columnName, Date x) throws SQLException {
		throw new UnsupportedOperationException("updateDate(String columnName, Date x)");
	}

	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new UnsupportedOperationException("updateDouble(int columnIndex, double x)");
	}

	public void updateDouble(String columnName, double x) throws SQLException {
		throw new UnsupportedOperationException("updateDouble(String columnName, double x)");
	}

	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new UnsupportedOperationException("updateFloat(int columnIndex, float x)");
	}

	public void updateFloat(String columnName, float x) throws SQLException {
		throw new UnsupportedOperationException("updateFloat(String columnName, float x)");
	}

	public void updateInt(int columnIndex, int x) throws SQLException {
		throw new UnsupportedOperationException("updateInt(int columnIndex, int x)");
	}

	public void updateInt(String columnName, int x) throws SQLException {
		throw new UnsupportedOperationException("updateInt(String columnName, int x)");
	}

	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new UnsupportedOperationException("updateLong(int columnIndex, long x)");
	}

	public void updateLong(String columnName, long x) throws SQLException {
		throw new UnsupportedOperationException("updateLong(String columnName, long x)");
	}

	public void updateNull(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("updateNull(int columnIndex)");
	}

	public void updateNull(String columnName) throws SQLException {
		throw new UnsupportedOperationException("updateNull(String columnName)");
	}

	public void updateObject(int columnIndex, Object x) throws SQLException {
		throw new UnsupportedOperationException("updateObject(int columnIndex, Object x)");
	}

	public void updateObject(String columnName, Object x) throws SQLException {
		throw new UnsupportedOperationException("updateObject(String columnName, Object x)");
	}

	public void updateObject(int columnIndex, Object x, int scale)
			throws SQLException {
		throw new UnsupportedOperationException("updateObject(int columnIndex, Object x, int scale)");
	}

	public void updateObject(String columnName, Object x, int scale)
			throws SQLException {
		throw new UnsupportedOperationException("updateObject(String columnName, Object x, int scale)");
	}

	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new UnsupportedOperationException("updateRef(int columnIndex, Ref x)");
	}

	public void updateRef(String columnName, Ref x) throws SQLException {
		throw new UnsupportedOperationException("updateRef(String columnName, Ref x)");
	}

	public void updateRow() throws SQLException {
		throw new UnsupportedOperationException("updateRow");
	}

	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new UnsupportedOperationException("updateShort(int columnIndex, short x)");
	}

	public void updateShort(String columnName, short x) throws SQLException {
		throw new UnsupportedOperationException("updateShort(String columnName, short x)");
	}

	public void updateString(int columnIndex, String x) throws SQLException {
		throw new UnsupportedOperationException("updateString(int columnIndex, String x)");
	}

	public void updateString(String columnName, String x) throws SQLException {
		throw new UnsupportedOperationException("updateString(String columnName, String x)");
	}

	public void updateTime(int columnIndex, Time x) throws SQLException {
		throw new UnsupportedOperationException("updateTime(int columnIndex, Time x)");
	}

	public void updateTime(String columnName, Time x) throws SQLException {
		throw new UnsupportedOperationException("updateTime(String columnName, Time x)");
	}

	public void updateTimestamp(int columnIndex, Timestamp x)
			throws SQLException {
		throw new UnsupportedOperationException("updateTimestamp(int columnIndex, Timestamp x)");
	}

	public void updateTimestamp(String columnName, Timestamp x)
			throws SQLException {
		throw new UnsupportedOperationException("updateTimestamp(String columnName, Timestamp x)");
	}

	public boolean wasNull() throws SQLException {
		Object[] objects=this.rows.get(this.cursor);
		return null==objects[objects.length-1];
	}
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		// TODO Auto-generated method stub
		return false;
	}
	public RowId getRowId(int columnIndex) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}
	public RowId getRowId(String columnLabel) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}
	public void updateRowId(int columnIndex, RowId x) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateRowId(String columnLabel, RowId x) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public int getHoldability() throws SQLException
	{
		// TODO Auto-generated method stub
		return 0;
	}
	public boolean isClosed() throws SQLException
	{
		// TODO Auto-generated method stub
		return false;
	}
	public void updateNString(int columnIndex, String nString)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateNString(String columnLabel, String nString)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateNClob(String columnLabel, NClob nClob)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public NClob getNClob(int columnIndex) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}
	public NClob getNClob(String columnLabel) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}
	public SQLXML getSQLXML(int columnIndex) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}
	public SQLXML getSQLXML(String columnLabel) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}
	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateSQLXML(String columnLabel, SQLXML xmlObject)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public String getNString(int columnIndex) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}
	public String getNString(String columnLabel) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}
	public Reader getNCharacterStream(int columnIndex) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}
	public Reader getNCharacterStream(String columnLabel) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}
	public void updateNCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateNCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateAsciiStream(int columnIndex, InputStream x, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateBinaryStream(int columnIndex, InputStream x, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateAsciiStream(String columnLabel, InputStream x, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateBinaryStream(String columnLabel, InputStream x,
			long length) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateBlob(int columnIndex, InputStream inputStream, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateBlob(String columnLabel, InputStream inputStream,
			long length) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateClob(int columnIndex, Reader reader, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateClob(String columnLabel, Reader reader, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateNClob(int columnIndex, Reader reader, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateNClob(String columnLabel, Reader reader, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateNCharacterStream(int columnIndex, Reader x)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateNCharacterStream(String columnLabel, Reader reader)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateAsciiStream(int columnIndex, InputStream x)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateBinaryStream(int columnIndex, InputStream x)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateCharacterStream(int columnIndex, Reader x)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateAsciiStream(String columnLabel, InputStream x)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateBinaryStream(String columnLabel, InputStream x)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateCharacterStream(String columnLabel, Reader reader)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateBlob(int columnIndex, InputStream inputStream)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateBlob(String columnLabel, InputStream inputStream)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateClob(int columnIndex, Reader reader) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateClob(String columnLabel, Reader reader)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateNClob(int columnIndex, Reader reader) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
	public void updateNClob(String columnLabel, Reader reader)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	
}
