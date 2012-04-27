/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TConnectionWrapper implements Connection {
	private static Log log = LogFactory.getLog(TConnectionWrapper.class);
	private final Connection targetConnection;
	private final TDataSourceWrapper dataSourceWrapper;

	private Set<TStatementWrapper> statements = new HashSet<TStatementWrapper>(1);

	public TConnectionWrapper(Connection targetConnection, TDataSourceWrapper dataSourceWrapper) {
		this.targetConnection = targetConnection;
		this.dataSourceWrapper = dataSourceWrapper;
	}

	public void clearWarnings() throws SQLException {
		this.targetConnection.clearWarnings();
	}

	public void close() throws SQLException {
		try {
			this.targetConnection.close();
		} finally {
			if (log.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				sb.append("appname : ").append(dataSourceWrapper.connectionProperties.datasourceName).append(" ");
				sb.append("threadcount : ").append(dataSourceWrapper.threadCount);
			}
			dataSourceWrapper.threadCount.decrementAndGet();
		}

		for (TStatementWrapper statementWrapper : this.statements) {
			try {
				statementWrapper.close();
			} catch (SQLException e) {
				log.error("", e);
			}
		}
	}

	public void commit() throws SQLException {
		this.targetConnection.commit();
	}

	public Statement createStatement() throws SQLException {
		Statement targetStatement = this.targetConnection.createStatement();
		TStatementWrapper statementWrapper = new TStatementWrapper(targetStatement, this, this.dataSourceWrapper);
		this.statements.add(statementWrapper);
		return statementWrapper;
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		Statement targetStatement = this.targetConnection.createStatement(resultSetType, resultSetConcurrency);
		TStatementWrapper statementWrapper = new TStatementWrapper(targetStatement, this, this.dataSourceWrapper);
		this.statements.add(statementWrapper);
		return statementWrapper;
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		Statement s = this.targetConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
		TStatementWrapper statementWrapper = new TStatementWrapper(s, this, this.dataSourceWrapper);
		this.statements.add(statementWrapper);
		return statementWrapper;
	}

	public boolean getAutoCommit() throws SQLException {
		return this.targetConnection.getAutoCommit();
	}

	public String getCatalog() throws SQLException {
		return this.targetConnection.getCatalog();
	}

	public int getHoldability() throws SQLException {
		return this.targetConnection.getHoldability();
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		DatabaseMetaData targetMetaData = this.targetConnection.getMetaData();
		return new DatabaseMetaDataWrapper(targetMetaData, this);
	}

	public int getTransactionIsolation() throws SQLException {
		return this.targetConnection.getTransactionIsolation();
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return this.targetConnection.getTypeMap();
	}

	public SQLWarning getWarnings() throws SQLException {
		return this.targetConnection.getWarnings();
	}

	public boolean isClosed() throws SQLException {
		return this.targetConnection.isClosed();
	}

	public boolean isReadOnly() throws SQLException {
		return this.targetConnection.isReadOnly();
	}

	public String nativeSQL(String sql) throws SQLException {
		return this.targetConnection.nativeSQL(sql);
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		CallableStatement cs = targetConnection.prepareCall(sql);
		CallableStatementWrapper csw = new CallableStatementWrapper(cs, this, this.dataSourceWrapper, sql);
		statements.add(csw);
		return csw;
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		CallableStatement cs = this.targetConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
		CallableStatementWrapper csw = new CallableStatementWrapper(cs, this, this.dataSourceWrapper, sql);
		statements.add(csw);
		return csw;
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		CallableStatement cs = this.targetConnection.prepareCall(sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
		CallableStatementWrapper csw = new CallableStatementWrapper(cs, this, this.dataSourceWrapper, sql);
		statements.add(csw);
		return csw;
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		PreparedStatement ps = this.targetConnection.prepareStatement(sql);
		TPreparedStatementWrapper psw = new TPreparedStatementWrapper(ps, this, this.dataSourceWrapper, sql);
		statements.add(psw);
		return psw;
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		PreparedStatement ps = this.targetConnection.prepareStatement(sql, autoGeneratedKeys);
		TPreparedStatementWrapper psw = new TPreparedStatementWrapper(ps, this, this.dataSourceWrapper, sql);
		statements.add(psw);
		return psw;
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		PreparedStatement ps = this.targetConnection.prepareStatement(sql, columnIndexes);
		TPreparedStatementWrapper psw = new TPreparedStatementWrapper(ps, this, this.dataSourceWrapper, sql);
		statements.add(psw);
		return psw;
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		PreparedStatement ps = this.targetConnection.prepareStatement(sql, columnNames);
		TPreparedStatementWrapper psw = new TPreparedStatementWrapper(ps, this, this.dataSourceWrapper, sql);
		statements.add(psw);
		return psw;
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		PreparedStatement ps = this.targetConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
		TPreparedStatementWrapper psw = new TPreparedStatementWrapper(ps, this, this.dataSourceWrapper, sql);
		statements.add(psw);
		return psw;
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		PreparedStatement ps = this.targetConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
		TPreparedStatementWrapper psw = new TPreparedStatementWrapper(ps, this, this.dataSourceWrapper, sql);
		statements.add(psw);
		return psw;
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		this.targetConnection.releaseSavepoint(savepoint);
	}

	public void rollback() throws SQLException {
		this.targetConnection.rollback();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		this.targetConnection.rollback(savepoint);
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		this.targetConnection.setAutoCommit(autoCommit);
	}

	public void setCatalog(String catalog) throws SQLException {
		this.targetConnection.setCatalog(catalog);
	}

	public void setHoldability(int holdability) throws SQLException {
		this.targetConnection.setHoldability(holdability);
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		this.targetConnection.setReadOnly(readOnly);
	}

	public Savepoint setSavepoint() throws SQLException {
		return this.targetConnection.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return this.targetConnection.setSavepoint(name);
	}

	public void setTransactionIsolation(int level) throws SQLException {
		this.targetConnection.setTransactionIsolation(level);

	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		this.targetConnection.setTypeMap(map);
	}

	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (isWrapperFor(iface)) {
			return (T) this;
		} else {
			throw new SQLException("not a wrapper for " + iface);
		}
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.getClass().isAssignableFrom(iface);
	}

	public Clob createClob() throws SQLException {
		return this.targetConnection.createClob();
	}

	public Blob createBlob() throws SQLException {
		return this.targetConnection.createBlob();
	}

	public NClob createNClob() throws SQLException {
		return this.targetConnection.createNClob();
	}

	public SQLXML createSQLXML() throws SQLException {
		return this.targetConnection.createSQLXML();
	}

	public boolean isValid(int timeout) throws SQLException {
		return this.targetConnection.isValid(timeout);
	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		this.targetConnection.setClientInfo(name, value);
	}

	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		this.targetConnection.setClientInfo(properties);
	}

	public String getClientInfo(String name) throws SQLException {
		return this.targetConnection.getClientInfo(name);
	}

	public Properties getClientInfo() throws SQLException {
		return this.targetConnection.getClientInfo();
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return this.targetConnection.createArrayOf(typeName, elements);
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return this.targetConnection.createStruct(typeName, attributes);
	}
}
