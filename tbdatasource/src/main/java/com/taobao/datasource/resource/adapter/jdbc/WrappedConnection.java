/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.taobao.datasource.resource.adapter.jdbc;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.jboss.logging.Logger;
import org.jboss.util.NestedSQLException;

/**
 * A wrapper for a connection.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57189 $
 */
public class WrappedConnection implements Connection {

	private static final Logger log = Logger.getLogger(WrappedConnection.class);

	private BaseWrapperManagedConnection mc;

	private WrapperDataSource dataSource;

	private HashMap statements;

	private boolean closed = false;

	private int trackStatements;

	public WrappedConnection(final BaseWrapperManagedConnection mc) {
		this.mc = mc;
		if (mc != null)
			trackStatements = mc.getTrackStatements();
	}

	void setManagedConnection(final BaseWrapperManagedConnection mc) {
		this.mc = mc;
		if (mc != null)
			trackStatements = mc.getTrackStatements();
	}

	public WrapperDataSource getDataSource() {
		return dataSource;
	}

	protected void setDataSource(WrapperDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		checkStatus();
		mc.setJdbcReadOnly(readOnly);
	}

	public boolean isReadOnly() throws SQLException {
		checkStatus();
		return mc.isJdbcReadOnly();
	}

	public void close() throws SQLException {
		closed = true;
		if (mc != null) {
			if (trackStatements != BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT) {
				synchronized (this) {
					if (statements != null) {
						for (Iterator i = statements.entrySet().iterator(); i.hasNext();) {
							Map.Entry entry = (Map.Entry) i.next();
							WrappedStatement ws = (WrappedStatement) entry.getKey();
							if (trackStatements == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_TRUE_INT) {
								Throwable stackTrace = (Throwable) entry.getValue();
								log.warn("Closing a statement you left open, please do your own housekeeping",
										stackTrace);
							}
							try {
								ws.internalClose();
							} catch (Throwable t) {
								log.warn("Exception trying to close statement:", t);
							}
						}
					}
				}
			}
			mc.closeHandle(this);
		}
		mc = null;
		dataSource = null;
	}

	public boolean isClosed() throws SQLException {
		return closed;
	}

	public Statement createStatement() throws SQLException {
		checkTransaction();
		try {
			return new WrappedStatement(this, mc.getConnection().createStatement());
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		checkTransaction();
		try {
			return new WrappedStatement(this, mc.getConnection().createStatement(resultSetType, resultSetConcurrency));
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {

		checkTransaction();
		try {
			return new WrappedStatement(this, mc.getConnection().createStatement(resultSetType, resultSetConcurrency,
					resultSetHoldability));
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		checkTransaction();
		try {
			return new WrappedPreparedStatement(this, mc.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY));
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		checkTransaction();
		try {
			return new WrappedPreparedStatement(this, mc.prepareStatement(sql, resultSetType, resultSetConcurrency));
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		checkTransaction();
		try {
			return new WrappedPreparedStatement(this, mc.getConnection().prepareStatement(sql, resultSetType,
					resultSetConcurrency, resultSetHoldability));
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		checkTransaction();
		try {
			return new WrappedPreparedStatement(this, mc.getConnection().prepareStatement(sql, autoGeneratedKeys));
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		checkTransaction();
		try {
			return new WrappedPreparedStatement(this, mc.getConnection().prepareStatement(sql, columnIndexes));
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {

		checkTransaction();
		try {
			return new WrappedPreparedStatement(this, mc.getConnection().prepareStatement(sql, columnNames));
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public CallableStatement prepareCall(String sql) throws SQLException {
		checkTransaction();
		try {
			return new WrappedCallableStatement(this, mc.prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY));
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		checkTransaction();
		try {
			return new WrappedCallableStatement(this, mc.prepareCall(sql, resultSetType, resultSetConcurrency));
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {

		checkTransaction();
		try {
			return new WrappedCallableStatement(this, mc.getConnection().prepareCall(sql, resultSetType,
					resultSetConcurrency, resultSetHoldability));
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public String nativeSQL(String sql) throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().nativeSQL(sql);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setAutoCommit(boolean autocommit) throws SQLException {
		checkStatus();
		mc.setJdbcAutoCommit(autocommit);
	}

	public boolean getAutoCommit() throws SQLException {
		checkStatus();
		return mc.isJdbcAutoCommit();
	}

	public void commit() throws SQLException {
		checkTransaction();
		mc.jdbcCommit();
	}

	public void rollback() throws SQLException {
		checkTransaction();
		mc.jdbcRollback();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		checkTransaction();
		mc.jdbcRollback(savepoint);
	}

	public DatabaseMetaData getMetaData() throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().getMetaData();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setCatalog(String catalog) throws SQLException {
		checkTransaction();
		try {
			mc.getConnection().setCatalog(catalog);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public String getCatalog() throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().getCatalog();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setTransactionIsolation(int isolationLevel) throws SQLException {
		checkStatus();
		mc.setJdbcTransactionIsolation(isolationLevel);
	}

	public int getTransactionIsolation() throws SQLException {
		checkStatus();
		return mc.getJdbcTransactionIsolation();
	}

	public SQLWarning getWarnings() throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().getWarnings();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void clearWarnings() throws SQLException {
		checkTransaction();
		try {
			mc.getConnection().clearWarnings();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public Map getTypeMap() throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().getTypeMap();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setTypeMap(Map<String, Class<?>> typeMap) throws SQLException {
		checkTransaction();
		try {
			mc.getConnection().setTypeMap(typeMap);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setHoldability(int holdability) throws SQLException {
		checkTransaction();
		try {
			mc.getConnection().setHoldability(holdability);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public int getHoldability() throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().getHoldability();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public Savepoint setSavepoint() throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().setSavepoint();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().setSavepoint(name);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		checkTransaction();
		try {
			mc.getConnection().releaseSavepoint(savepoint);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public Connection getUnderlyingConnection() throws SQLException {
		checkTransaction();
		return mc.getConnection();
	}

	void checkTransaction() throws SQLException {
		checkStatus();
		mc.checkTransaction();
	}

	/**
	 * The checkStatus method checks that the handle has not been closed and
	 * that it is associated with a managed connection.
	 *
	 * @exception SQLException if an error occurs
	 */
	protected void checkStatus() throws SQLException {
		if (closed)
			throw new SQLException("Connection handle has been closed and is unusable");
		if (mc == null)
			throw new SQLException("Connection handle is not currently associated with a ManagedConnection");
	}

	/**
	 * The base checkException method rethrows the supplied exception, informing
	 * the ManagedConnection of the error. Subclasses may override this to
	 * filter exceptions based on their severity.
	 *
	 * @param e a <code>SQLException</code> value
	 * @exception Exception if an error occurs
	 */
	protected SQLException checkException(Throwable t) throws SQLException {
		if (mc != null)
			mc.connectionError(t);
		if (t instanceof SQLException)
			throw (SQLException) t;
		else
			throw new NestedSQLException("Error", t);
	}

	int getTrackStatements() {
		return trackStatements;
	}

	void registerStatement(WrappedStatement ws) {
		if (trackStatements == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
			return;

		synchronized (this) {
			if (statements == null)
				statements = new HashMap();

			if (trackStatements == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_TRUE_INT)
				statements.put(ws, new Throwable("STACKTRACE"));
			else
				statements.put(ws, null);
		}
	}

	void unregisterStatement(WrappedStatement ws) {
		if (trackStatements == BaseWrapperManagedConnectionFactory.TRACK_STATEMENTS_FALSE_INT)
			return;
		synchronized (this) {
			if (statements != null)
				statements.remove(ws);
		}
	}

	void checkConfiguredQueryTimeout(WrappedStatement ws) throws SQLException {
		if (mc == null || dataSource == null)
			return;

		int timeout = 0;

		// Use the transaction timeout
		if (mc.isTransactionQueryTimeout())
			timeout = dataSource.getTimeLeftBeforeTransactionTimeout();

		// Look for a configured value
		if (timeout <= 0)
			timeout = mc.getQueryTimeout();

		if (timeout > 0)
			ws.setQueryTimeout(timeout);
	}

	Logger getLogger() {
		return log;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(mc.getConnection());
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return (T) (iface.isInstance(mc.getConnection()) ? mc.getConnection() : null);
	}

	public Clob createClob() throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().createClob();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public Blob createBlob() throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().createBlob();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public NClob createNClob() throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().createNClob();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public SQLXML createSQLXML() throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().createSQLXML();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public boolean isValid(int timeout) throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().isValid(timeout);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		try {
			checkTransaction();
			try {
				mc.getConnection().setClientInfo(name, value);
			} catch (Throwable t) {
				throw checkException(t);
			}
		} catch (SQLClientInfoException e) {
			throw e;
		} catch (SQLException e) {
			SQLClientInfoException t = new SQLClientInfoException();
			t.initCause(e);
			throw t;
		}
	}

	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		try {
			checkTransaction();
			try {
				mc.getConnection().setClientInfo(properties);
			} catch (Throwable t) {
				throw checkException(t);
			}
		} catch (SQLClientInfoException e) {
			throw e;
		} catch (SQLException e) {
			SQLClientInfoException t = new SQLClientInfoException();
			t.initCause(e);
			throw t;
		}
	}

	public String getClientInfo(String name) throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().getClientInfo(name);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public Properties getClientInfo() throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().getClientInfo();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().createArrayOf(typeName, elements);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		checkTransaction();
		try {
			return mc.getConnection().createStruct(typeName, attributes);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}
}
