/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.taobao.tddl.common.util.TDDLMBeanServer;

/**抽象的TAtomDataSource 定义，实现了DataSource接口
 * 并且定义了自己的接口抽象方法
 * @author qihao
 *
 */
public abstract class AbstractTAtomDataSource implements DataSource {

	protected abstract DataSource getDataSource() throws SQLException;

	public abstract void init() throws Exception;

	public abstract void flushDataSource();

	public abstract void destroyDataSource() throws Exception;
	
	public Connection getConnection() throws SQLException {
		return getDataSource().getConnection();
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return getDataSource().getConnection(username, password);
	}

	public PrintWriter getLogWriter() throws SQLException {
		return getDataSource().getLogWriter();
	}

	public int getLoginTimeout() throws SQLException {
		return getDataSource().getLoginTimeout();
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		getDataSource().setLogWriter(out);

	}

	public static void setShutDownMBean(boolean shutDownMBean) {
		TDDLMBeanServer.shutDownMBean=shutDownMBean;
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		getDataSource().setLoginTimeout(seconds);
	}
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		if(isWrapperFor(iface)){
			return (T) this;
		}else{
			throw new SQLException("not a wrapper for "+ iface);
		}
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		return AbstractTAtomDataSource.class.isAssignableFrom(iface);
	}
	
	public static void main(String[] args) throws SQLException
	{
		StaticTAtomDataSource s = new StaticTAtomDataSource();
		System.out.println(s.isWrapperFor(StaticTAtomDataSource.class));
	}
}
