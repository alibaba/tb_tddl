/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

public class StaticTAtomDataSourceUnitTest {
	@Test
	public void createStaticTAtomDataSource() throws Exception {
		StaticTAtomDataSource dataSource = new StaticTAtomDataSource();
		dataSource.setIp("127.0.0.1");
		dataSource.setPort("3306");
		dataSource.setDbName("test");
		dataSource.setDbType("mysql");
		dataSource.setUserName("tddl");
		dataSource.setPasswd("tddl");
		dataSource.setMinPoolSize(1);
		dataSource.setMaxPoolSize(2);
		//≥ı ºªØ
		dataSource.init();
		JdbcTemplate jtp = new JdbcTemplate();
		jtp.setDataSource(dataSource);
		int actual = jtp.queryForInt("select 1 from dual");
		Assert.assertEquals(actual, 1);
		dataSource.destroyDataSource();
	}
}
