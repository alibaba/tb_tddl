/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * TaobaoDataSource的抽象类，将javax.sql.DataSource中的方法集中到一起
 * 
 * @author qihao
 * 
 */
public abstract class AbstractTaobaoDataSource implements DataSource {

	protected abstract DataSource getDatasource() throws SQLException;

	public Connection getConnection() throws SQLException {
		return getDatasource().getConnection();
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return getDatasource().getConnection(username, password);
	}

	public PrintWriter getLogWriter() throws SQLException {
		return getDatasource().getLogWriter();

	}

	public int getLoginTimeout() throws SQLException {
		return getDatasource().getLoginTimeout();
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		getDatasource().setLogWriter(out);
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		getDatasource().setLoginTimeout(seconds);
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
		return AbstractTaobaoDataSource.class.isAssignableFrom(iface);
	}
}
