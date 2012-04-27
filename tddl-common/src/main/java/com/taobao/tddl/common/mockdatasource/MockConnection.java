/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.mockdatasource;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;

import com.taobao.tddl.common.exception.runtime.NotSupportException;
import com.taobao.tddl.common.mockdatasource.MockDataSource.ExecuteInfo;

public class MockConnection implements Connection {
	private MockDataSource mockDataSource;

	private long timeToClose = 0;
	private int closeInvokingTimes = 0;
	private boolean isClosed = false;
	private boolean autoCommit = true;
	private int commitInvokingTimes = 0;

	public void clearWarnings() throws SQLException {
		throw new NotSupportException("");
	}

	public MockConnection(String method, MockDataSource mockDataSource) {
		this.mockDataSource = mockDataSource;
		MockDataSource.record(new ExecuteInfo(this.mockDataSource, method, null, null));
	}

	public void close() throws SQLException {
		try {
			Thread.sleep(timeToClose);
		} catch (Exception e) {
		}
		closeInvokingTimes++;
		isClosed = true;
	}

	protected void checkClose() throws SQLException {
		if (isClosed)
			throw new SQLException("closed");
	}

	public void commit() throws SQLException {
        mockDataSource.checkState();
        checkClose();
        MockDataSource.record(new ExecuteInfo(mockDataSource, "commit", null, null));
        commitInvokingTimes++;
	}

	private static void checkPreException(String name) throws SQLException{
		SQLException e = MockDataSource.popPreException(name);
		if(e!=null){
			throw e;
		}
	}
	public Statement createStatement() throws SQLException {
		mockDataSource.checkState();
		checkPreException(MockDataSource.m_createStatement);
		return new MockStatement("createStatement", this.mockDataSource);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		mockDataSource.checkState();
		checkPreException(MockDataSource.m_createStatement);
		return new MockStatement("createStatement#int_int", this.mockDataSource);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		mockDataSource.checkState();
		checkPreException(MockDataSource.m_createStatement);
		return new MockStatement("createStatement#int_int_int", this.mockDataSource);
	}

	public boolean getAutoCommit() throws SQLException {
		mockDataSource.checkState();
		checkClose();
		return autoCommit;
	}

	public String getCatalog() throws SQLException {
		throw new NotSupportException("");
	}

	public int getHoldability() throws SQLException {
		//throw new NotSupportException("");
		return ResultSet.CLOSE_CURSORS_AT_COMMIT;
	}

	private int getMetadataInvokingTimes = 0;

	public DatabaseMetaData getMetaData() throws SQLException {
		mockDataSource.checkState();
		checkClose();
		getMetadataInvokingTimes++;
		return new MockDataBaseMetaData();
	}

	int transactionIsolation = -1;

	public int getTransactionIsolation() throws SQLException {
		mockDataSource.checkState();
		checkClose();

		return transactionIsolation;
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		throw new NotSupportException("");
	}

	public SQLWarning getWarnings() throws SQLException {
		throw new NotSupportException("");
	}

	public boolean isClosed() throws SQLException {
		mockDataSource.checkState();
		return isClosed;
	}

	private boolean isReadOnly;

	public boolean isReadOnly() throws SQLException {
		mockDataSource.checkState();
		return isReadOnly;
	}

	public String nativeSQL(String sql) throws SQLException {
		throw new NotSupportException("");
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		throw new NotSupportException("");
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		throw new NotSupportException("");
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		throw new NotSupportException("");
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		mockDataSource.checkState();
		checkPreException(MockDataSource.m_prepareStatement);
		return new MockPreparedStatement("prepareStatement", this.mockDataSource, sql);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		mockDataSource.checkState();
		checkPreException(MockDataSource.m_prepareStatement);
		return new MockPreparedStatement("prepareStatement#string_int", this.mockDataSource, sql);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		mockDataSource.checkState();
		checkPreException(MockDataSource.m_prepareStatement);
		return new MockPreparedStatement("prepareStatement#string_int[", this.mockDataSource, sql);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		mockDataSource.checkState();
		checkPreException(MockDataSource.m_prepareStatement);
		return new MockPreparedStatement("prepareStatement#string_String[", this.mockDataSource, sql);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		mockDataSource.checkState();
		checkPreException(MockDataSource.m_prepareStatement);
		return new MockPreparedStatement("prepareStatement#string_int_int", this.mockDataSource, sql);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		mockDataSource.checkState();
		checkPreException(MockDataSource.m_prepareStatement);
		return new MockPreparedStatement("prepareStatement#string_int_int_int", this.mockDataSource, sql);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		mockDataSource.checkState();
		throw new NotSupportException("");
	}

	private int rollbackInvotingTimes = 0;

	public void rollback() throws SQLException {
		mockDataSource.checkState();
		checkClose();
        MockDataSource.record(new ExecuteInfo(mockDataSource, "rollback", null, null));
		rollbackInvotingTimes++;
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		mockDataSource.checkState();
		checkClose();
        MockDataSource.record(new ExecuteInfo(mockDataSource,
                "rollback#savepoint", null, new Object[]{ savepoint }));
		rollbackInvotingTimes++;
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		mockDataSource.checkState();
		checkClose();
        MockDataSource.record(new ExecuteInfo(mockDataSource, "setAutoCommit",
                null, new Object[]{ autoCommit }));
		this.autoCommit = autoCommit;
	}

	public void setCatalog(String catalog) throws SQLException {
		throw new NotSupportException("");
	}

	public void setHoldability(int holdability) throws SQLException {
		throw new NotSupportException("");
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		mockDataSource.checkState();
		//TODO:
	}

	public Savepoint setSavepoint() throws SQLException {
		throw new NotSupportException("");
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		throw new NotSupportException("");
	}

	public void setTransactionIsolation(int level) throws SQLException {
		mockDataSource.checkState();
		this.transactionIsolation = level;
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		throw new NotSupportException("");
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

	public Clob createClob() throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Blob createBlob() throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public NClob createNClob() throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public SQLXML createSQLXML() throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isValid(int timeout) throws SQLException
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setClientInfo(String name, String value)
			throws SQLClientInfoException
	{
		// TODO Auto-generated method stub
		
	}

	public void setClientInfo(Properties properties)
			throws SQLClientInfoException
	{
		// TODO Auto-generated method stub
		
	}

	public String getClientInfo(String name) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getClientInfo() throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
