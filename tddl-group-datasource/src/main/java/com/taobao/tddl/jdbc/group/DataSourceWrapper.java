/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.taobao.tddl.interact.rule.bean.DBType;
import com.taobao.tddl.jdbc.group.config.Weight;

/**
 * 一个线程安全的DataSource包装类
 * 
 * DataSource包装类，因为一个GroupDataSource由多个AtomDataSource组成，且每个AtomDataSource都有对应的读写权重等信息，所以将每一个AtomDataSource封装起来。---add by mazhidan.pt
 * 
 * @author yangzhu
 * @author linxuan refactor as immutable class; dataSourceIndex extends
 *
 */
public class DataSourceWrapper implements DataSource {
	private final String dataSourceKey;  //这个DataSource对应的dbKey
	private final String weightStr;//权重信息字符串
	private final Weight weight;  //权重信息
	private final DataSource wrappedDataSource; //被封装的目标DataSource
	private final DBType dbType;//数据库类型
	private final int dataSourceIndex;//DataSourceIndex是指这个DataSource在Group中的位置

	public DataSourceWrapper(String dataSourceKey, String weightStr, DataSource wrappedDataSource, DBType dbType,
			int dataSourceIndex) {
		this.dataSourceKey = dataSourceKey;
		this.weight = new Weight(weightStr);
		this.weightStr = weightStr;
		this.wrappedDataSource = wrappedDataSource;
		this.dbType = dbType;

		this.dataSourceIndex = dataSourceIndex;
	}

	public DataSourceWrapper(String dataSourceKey, String weightStr, DataSource wrappedDataSource, DBType dbType) {
		this(dataSourceKey, weightStr, wrappedDataSource, dbType, -1);
	}

	/**
	 * 验证此DataSource的路由index信息中，是否包含指定的index--add by mazhidan.pt
	 */
	public boolean isMatchDataSourceIndex(int specifiedIndex) {
		if (weight.indexes != null && !weight.indexes.isEmpty()) {
			return weight.indexes.contains(specifiedIndex);
		} else {
			return this.dataSourceIndex == specifiedIndex;
		}
	}

	/**
	 * 是否有读权重。r0则放回false
	 */
	public boolean hasReadWeight() {
		return weight.r != 0;
	}

	/**
	 * 是否有写权重。w0则放回false
	 */
	public boolean hasWriteWeight() {
		return weight.w != 0;
	}

	public String toString() {
		return new StringBuilder("DataSourceWrapper{dataSourceKey=").append(dataSourceKey).append(", dataSourceIndex=")
				.append(dataSourceIndex).append(",weight=").append(weight).append("}").toString();
	}

	public String getDataSourceKey() {
		return dataSourceKey;
	}

	public String getWeightStr() {
		return weightStr;
	}

	/*public synchronized void setWeightStr(String weightStr) {
		if ((this.weightStr == weightStr) || (this.weightStr != null && this.weightStr.equals(weightStr)))
			return;
		this.weight = new Weight(weightStr);
		this.weightStr = weightStr;
	}*/

	public Weight getWeight() {
		return weight;
	}

	/*public int getDataSourceIndex() {
		return dataSourceIndex;
	}*/

	/*public void setDataSourceIndex(int dataSourceIndex) {
		this.dataSourceIndex = dataSourceIndex;
	}*/

	public DBType getDBType() {
		return dbType;
	}

	public DataSource getWrappedDataSource() {
		return wrappedDataSource;
	}

	//以下是javax.sql.DataSource的API实现
	////////////////////////////////////////////////////////////////////////////
	public Connection getConnection() throws SQLException {
		return wrappedDataSource.getConnection();
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return wrappedDataSource.getConnection(username, password);
	}

	public PrintWriter getLogWriter() throws SQLException {
		return wrappedDataSource.getLogWriter();
	}

	public int getLoginTimeout() throws SQLException {
		return wrappedDataSource.getLoginTimeout();
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		wrappedDataSource.setLogWriter(out);
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		wrappedDataSource.setLoginTimeout(seconds);
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

	/*
	//Since: 1.6

	//java.sql.Wrapper
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}
	*/
}
