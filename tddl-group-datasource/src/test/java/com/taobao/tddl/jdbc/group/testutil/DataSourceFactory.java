/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.testutil;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;

/**
 * 
 * @author yangzhu
 *
 */
public class DataSourceFactory {
	public static DataSource getMySQLDataSource() {
		return getMySQLDataSource(1);
	}

	public static DataSource getMySQLDataSource(int num) {
		if (num > 3)
			num = 1;
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUsername("tddl");
		ds.setPassword("tddl");
		ds.setUrl("jdbc:mysql://127.0.0.1:3306/group_test_" + num);
		return ds;

	}

	public static DataSource getLocalMySQLDataSource() {
		return getLocalMySQLDataSource(1);
	}

	public static DataSource getLocalMySQLDataSource(int num) {
		if (num > 3)
			num = 1;
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUsername("root");
		ds.setPassword("zhh");
		ds.setUrl("jdbc:mysql://localhost/group_test_" + num);
		return ds;

	}

	public static DataSource getOracleDataSource() {
		return null;
	}
}
