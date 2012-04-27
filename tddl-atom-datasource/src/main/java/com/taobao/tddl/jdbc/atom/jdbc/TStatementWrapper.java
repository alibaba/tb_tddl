/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.tddl.common.Monitor;
import com.taobao.tddl.jdbc.atom.config.object.AtomDbStatusEnum;

/**
 * Statement 包装类
 * @author shenxun
 *
 */
public class TStatementWrapper implements Statement {
	private static Log log = LogFactory.getLog(TStatementWrapper.class);
	private static final Pattern SELECT_FOR_UPDATE_PATTERN = Pattern.compile("^select\\s+.*\\s+for\\s+update.*$",
			Pattern.CASE_INSENSITIVE);
	protected static final String UPDATE = "UPDATE";
	protected static final String QUERY = "QUERY";

	protected final Statement targetStatement;
	protected final TConnectionWrapper connectionWrapper;
	protected final TDataSourceWrapper datasourceWrapper;

	/**
	 * 经过计算后的结果集，允许使用 getResult函数调用.
	 *
	 * 一个statement只允许有一个结果集
	 */
	protected TResultSetWrapper currentResultSet;

	public TStatementWrapper(Statement targetStatement, TConnectionWrapper connectionWrapper,
			TDataSourceWrapper tdsWrapper) {
		this.targetStatement = targetStatement;
		this.connectionWrapper = connectionWrapper;
		this.datasourceWrapper = tdsWrapper;
	}

	public void addBatch(String sql) throws SQLException {
		this.targetStatement.addBatch(sql);
	}

	public void cancel() throws SQLException {
		this.targetStatement.cancel();
	}

	public void clearBatch() throws SQLException {
		this.targetStatement.clearBatch();
	}

	public void clearWarnings() throws SQLException {
		this.targetStatement.clearWarnings();
	}

	public void close() throws SQLException {
		if (currentResultSet != null) {
			try {
				this.currentResultSet.close();
			} catch (SQLException e) {
				log.error("", e);
			}
		}
		this.targetStatement.close();
	}

	protected void recordReadTimes() throws SQLException {
		AtomDbStatusEnum status = datasourceWrapper.connectionProperties.dbStatus;
		if (status != AtomDbStatusEnum.R_STAUTS && status != AtomDbStatusEnum.RW_STATUS) {
			throw new SQLException("db do not allow to execute read ! dbStatus is " + status);
		}
		/*
		int readRestrictionTimes = datasourceWrapper.connectionProperties.readRestrictionTimes;
		int currentReadTimes = datasourceWrapper.readTimes.incrementAndGet();
		if (readRestrictionTimes != 0) {
			if (currentReadTimes > readRestrictionTimes) {
				datasourceWrapper.readTimesReject.incrementAndGet();
				throw new SQLException("max read times ," + currentReadTimes);
			}
		}
		*/
		if (!datasourceWrapper.readFlowControl.allow()) {
			throw new SQLException(datasourceWrapper.readFlowControl.reportExceed());
		}
	}

	protected void recordWriteTimes() throws SQLException {
		AtomDbStatusEnum status = datasourceWrapper.connectionProperties.dbStatus;
		if (status != AtomDbStatusEnum.W_STATUS && status != AtomDbStatusEnum.RW_STATUS) {
			throw new SQLException("db do not allow to execute write ! dbStatus is " + status);
		}
		/*
		int writeRestrictionTimes = datasourceWrapper.connectionProperties.writeRestrictionTimes;
		int currentWriteTimes = datasourceWrapper.writeTimes.incrementAndGet();
		if (writeRestrictionTimes != 0) {
			if (currentWriteTimes > writeRestrictionTimes) {
				datasourceWrapper.writeTimesReject.incrementAndGet();
				throw new SQLException("max write times , " + currentWriteTimes);
			}
		}
		*/
		if (!datasourceWrapper.writeFlowControl.allow()) {
			throw new SQLException(datasourceWrapper.writeFlowControl.reportExceed());
		}
	}

	// 增加并发读计数并判断阀值
	protected void increaseConcurrentRead() throws SQLException {
		int maxConcurrentReadRestrict = datasourceWrapper.connectionProperties.maxConcurrentReadRestrict;
		int concurrentReadCount = datasourceWrapper.concurrentReadCount.incrementAndGet();
		if (maxConcurrentReadRestrict != 0) {
			if (concurrentReadCount > maxConcurrentReadRestrict) {
				datasourceWrapper.readTimesReject.incrementAndGet();
				throw new SQLException("maxConcurrentReadRestrict reached , " + maxConcurrentReadRestrict);
			}
		}
	}

	// 增加并发写计数并判断阀值
	protected void increaseConcurrentWrite() throws SQLException {
		int maxConcurrentWriteRestrict = datasourceWrapper.connectionProperties.maxConcurrentWriteRestrict;
		int concurrentWriteCount = datasourceWrapper.concurrentWriteCount.incrementAndGet();
		if (maxConcurrentWriteRestrict != 0) {
			if (concurrentWriteCount > maxConcurrentWriteRestrict) {
				datasourceWrapper.writeTimesReject.incrementAndGet();
				throw new SQLException("maxConcurrentWriteRestrict reached , " + maxConcurrentWriteRestrict);
			}
		}
	}

	// 减少并发读计数
	protected void decreaseConcurrentRead() throws SQLException {
		datasourceWrapper.concurrentReadCount.decrementAndGet();
	}

	// 减少并发写计数
	protected void decreaseConcurrentWrite() throws SQLException {
		datasourceWrapper.concurrentWriteCount.decrementAndGet();
	}

	public boolean execute(String sql) throws SQLException {
		return executeInternal(sql, -1, null, null);
	}

	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		return executeInternal(sql, autoGeneratedKeys, null, null);
	}

	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		return executeInternal(sql, -1, columnIndexes, null);
	}

	public boolean execute(String sql, String[] columnNames) throws SQLException {
		return executeInternal(sql, -1, null, columnNames);
	}

	private boolean executeInternal(String sql, int autoGeneratedKeys, int[] columnIndexes, String[] columnNames)
			throws SQLException {
		SqlType sqlType = getSqlType(sql);
		if (sqlType == SqlType.SELECT || sqlType == SqlType.SELECT_FOR_UPDATE || sqlType == SqlType.SHOW) {
			executeQuery(sql);
			return true;
		} else if (sqlType == SqlType.INSERT || sqlType == SqlType.UPDATE || sqlType == SqlType.DELETE
				|| sqlType == SqlType.REPLACE|| sqlType== SqlType.TRUNCATE|| sqlType == SqlType.CREATE
				|| sqlType== SqlType.DROP|| sqlType == SqlType.LOAD|| sqlType== SqlType.MERGE) {
			executeUpdateInternal(sql, autoGeneratedKeys, columnIndexes, columnNames);
			return false;
		} else {
			throw new SQLException("only select, insert, update, delete,replace,truncate,create,drop,load,merge sql is supported");
		}
	}

	private int executeUpdateInternal(String sql, int autoGeneratedKeys, int[] columnIndexes, String[] columnNames)
			throws SQLException {
		ensureResultSetIsEmpty();
		recordWriteTimes();
		increaseConcurrentWrite();
		long time0 = System.currentTimeMillis();
		Exception e0 = null;

		try {
			if (autoGeneratedKeys == -1 && columnIndexes == null && columnNames == null) {
				return this.targetStatement.executeUpdate(sql);
			} else if (autoGeneratedKeys != -1) {
				return this.targetStatement.executeUpdate(sql, autoGeneratedKeys);
			} else if (columnIndexes != null) {
				return this.targetStatement.executeUpdate(sql, columnIndexes);
			} else if (columnNames != null) {
				return this.targetStatement.executeUpdate(sql, columnNames);
			} else {
				return this.targetStatement.executeUpdate(sql);
			}
		} catch (SQLException e) {
			e0 = e;
			throw e;
		} finally {
			decreaseConcurrentWrite();
			recordSql(sql, System.currentTimeMillis() - time0, e0);
		}
	}

	public int[] executeBatch() throws SQLException {
		ensureResultSetIsEmpty();
		recordWriteTimes();
		increaseConcurrentWrite();
		try {
			return this.targetStatement.executeBatch();
		} finally {
			decreaseConcurrentWrite();
		}
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		ensureResultSetIsEmpty();
		recordReadTimes();
		increaseConcurrentRead();
		long time0 = System.currentTimeMillis();
		Exception e0 = null;

		try {
			currentResultSet = new TResultSetWrapper(this, this.targetStatement.executeQuery(sql));
			return currentResultSet;
		} catch (SQLException e) {
			decreaseConcurrentRead();
			e0 = e;
			throw e;
		} finally {
			recordSql(sql, System.currentTimeMillis() - time0, e0);
		}
	}

	//TODO 200ms的超时要可动态配置
	protected void recordSql(String sql, long elapsedTime, Exception e) {
		//只有允许记录atom的sql以及在采样频率下才能记录		if (!Monitor.isStatAtomSql||!Monitor.isSamplingRecord()) {			return;		}		if (!Monitor.isInclude(sql)) {			return; // 不在白名单中，不输出日志，以减少日志量		}		String dbname = datasourceWrapper.connectionProperties.datasourceName;		String dbIp=datasourceWrapper.connectionProperties.ip;		String dbPort=datasourceWrapper.connectionProperties.port;		String realDbName=datasourceWrapper.connectionProperties.realDbName;				if (e != null) {			//TODO 暂时不使用惩罚超时机制//			if (elapsedTime > 500) {//				this.datasourceWrapper.countTimeOut(); //记录超时//			}			Monitor.atomSqlAdd(dbname, Monitor.buildExecuteSqlKey2(sql), Monitor.KEY3_EXECUTE_A_SQL_EXCEPTION,dbIp,dbPort,realDbName,elapsedTime, 1);		} else if (elapsedTime > Monitor.sqlTimeout) {//			this.datasourceWrapper.countTimeOut(); //记录超时			Monitor.atomSqlAdd(dbname, Monitor.buildExecuteSqlKey2(sql), Monitor.KEY3_EXECUTE_A_SQL_TIMEOUT,dbIp,dbPort,realDbName,elapsedTime, 1);		} else {			Monitor.atomSqlAdd(dbname, Monitor.buildExecuteSqlKey2(sql), Monitor.KEY3_EXECUTE_A_SQL_SUCCESS,dbIp,dbPort,realDbName,elapsedTime, 1);		}
	}

	public int executeUpdate(String sql) throws SQLException {
		return executeUpdateInternal(sql, -1, null, null);
	}

	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		return executeUpdateInternal(sql, autoGeneratedKeys, null, null);
	}

	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		return executeUpdateInternal(sql, -1, columnIndexes, null);
	}

	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		return executeUpdateInternal(sql, -1, null, columnNames);
	}

	public Connection getConnection() throws SQLException {
		return connectionWrapper;
	}

	public int getFetchDirection() throws SQLException {
		return this.targetStatement.getFetchDirection();
	}

	public int getFetchSize() throws SQLException {
		return this.targetStatement.getFetchSize();
	}

	public ResultSet getGeneratedKeys() throws SQLException {
		return new TResultSetWrapper(this, this.targetStatement.getGeneratedKeys());
	}

	public int getMaxFieldSize() throws SQLException {
		return this.targetStatement.getMaxFieldSize();
	}

	public int getMaxRows() throws SQLException {
		return this.targetStatement.getMaxRows();
	}

	public boolean getMoreResults() throws SQLException {
		return this.targetStatement.getMoreResults();
	}

	public boolean getMoreResults(int current) throws SQLException {
		return this.targetStatement.getMoreResults(current);
	}

	public int getQueryTimeout() throws SQLException {
		return this.targetStatement.getQueryTimeout();
	}

	//FIXME
	public ResultSet getResultSet() throws SQLException {
		/*
		ResultSet targetRS = this.targetStatement.getResultSet();
		if (targetRS == null) {
			return null;
		}
		return new TResultSetWrapper(this, targetRS);
		*/
		return currentResultSet;
	}

	public int getResultSetConcurrency() throws SQLException {
		return this.targetStatement.getResultSetConcurrency();
	}

	public int getResultSetHoldability() throws SQLException {
		return this.targetStatement.getResultSetHoldability();
	}

	public int getResultSetType() throws SQLException {
		return this.targetStatement.getResultSetType();
	}

	public int getUpdateCount() throws SQLException {
		return this.targetStatement.getUpdateCount();
	}

	public SQLWarning getWarnings() throws SQLException {
		return this.targetStatement.getWarnings();
	}

	public void setCursorName(String name) throws SQLException {
		this.targetStatement.setCursorName(name);
	}

	public void setEscapeProcessing(boolean enable) throws SQLException {
		this.targetStatement.setEscapeProcessing(enable);
	}

	public void setFetchDirection(int direction) throws SQLException {
		this.targetStatement.setFetchDirection(direction);
	}

	public void setFetchSize(int rows) throws SQLException {
		this.targetStatement.setFetchSize(rows);
	}

	public void setMaxFieldSize(int max) throws SQLException {
		this.targetStatement.setMaxFieldSize(max);
	}

	public void setMaxRows(int max) throws SQLException {
		this.targetStatement.setMaxRows(max);
	}

	public void setQueryTimeout(int seconds) throws SQLException {
		this.targetStatement.setQueryTimeout(seconds);
	}

	protected static SqlType getSqlType(String sql) throws SQLException {
		SqlType sqlType = null;
		String noCommentsSql = StringUtils.stripComments(sql, "'\"", "'\"", true, false, true, true).trim();

		if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "select")) {
			if (SELECT_FOR_UPDATE_PATTERN.matcher(noCommentsSql).matches()) {
				sqlType = SqlType.SELECT_FOR_UPDATE;
			} else {
				sqlType = SqlType.SELECT;
			}
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "show")) {
			sqlType = SqlType.SHOW;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "insert")) {
			sqlType = SqlType.INSERT;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "update")) {
			sqlType = SqlType.UPDATE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "delete")) {
			sqlType = SqlType.DELETE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "replace")) {
			sqlType = SqlType.REPLACE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "truncate")){
		    sqlType = SqlType.TRUNCATE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "create")) {
			sqlType = SqlType.CREATE;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "drop")) {
			sqlType = SqlType.DROP;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "load")) {
			sqlType = SqlType.LOAD;
		} else if (StringUtils.startsWithIgnoreCaseAndWs(noCommentsSql, "merge")){
		    sqlType = SqlType.MERGE;
		} else {
			throw new SQLException("only select, insert, update, delete,replace,truncate sql is supported");
		}
		return sqlType;
	}

	/**
	 * 如果新建了查询，那么上一次查询的结果集应该被显示的关闭掉。这才是符合jdbc规范的
	 *
	 * @throws SQLException
	 */
	protected void ensureResultSetIsEmpty() throws SQLException {
		if (currentResultSet != null) {
			try {
				currentResultSet.close();
			} catch (SQLException e) {
				log.error("exception on close last result set . can do nothing..", e);
			} finally {
				currentResultSet = null;
			}
		}
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

	public boolean isClosed() throws SQLException {
		return this.targetStatement.isClosed();
	}

	public void setPoolable(boolean poolable) throws SQLException {
		this.targetStatement.setPoolable(poolable);
	}

	public boolean isPoolable() throws SQLException {
		return this.targetStatement.isPoolable();
	}
}
