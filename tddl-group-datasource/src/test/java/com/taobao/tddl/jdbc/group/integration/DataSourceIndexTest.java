/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.integration;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.tddl.jdbc.group.TGroupDataSource;
import com.taobao.tddl.jdbc.group.testutil.DBHelper;

import com.taobao.tddl.client.util.ThreadLocalMap;
import com.taobao.tddl.client.ThreadLocalString;
import com.taobao.tddl.common.GroupDataSourceRouteHelper;

/**
 * 
 * @author yangzhu
 *
 */
public class DataSourceIndexTest {
	public static final String appName = "unitTest";
	public static final String dbGroupKey = "myDbGroupIndex";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//DBHelper.deleteAll(); //删除三个库中crud表的所有记录
	}
	
	@Before
	public void setUp() throws Exception {
		DBHelper.deleteAll();
	}

	@Test
	public void testThreadLocalDataSourceIndex() throws Exception {
		try {
			TGroupDataSource ds = new TGroupDataSource(dbGroupKey, appName);
			ds.init();
			
			ThreadLocalMap.put(ThreadLocalString.DATASOURCE_INDEX, 0);
			Connection conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			assertEquals(stmt.executeUpdate("insert into crud(f1,f2) values(100,'str')"), 1);
			ResultSet rs = stmt.executeQuery("select f1,f2 from crud where f1=100");
			assertTrue(rs.next());

			ThreadLocalMap.put(ThreadLocalString.DATASOURCE_INDEX, 1);
			assertEquals(stmt.executeUpdate("insert into crud(f1,f2) values(100,'str')"), 1);
			rs = stmt.executeQuery("select count(*) from crud where f1=100");
			assertTrue(rs.next());
			assertEquals(rs.getInt(1), 1);

			stmt.close();
			conn.close();
		} finally {
			ThreadLocalMap.put(ThreadLocalString.DATASOURCE_INDEX, null);
		}
	}
	
	@Test
	public void testGroupDataSourceRouteHelper() throws Exception{
		try {
			TGroupDataSource ds = new TGroupDataSource(dbGroupKey, appName);
			ds.init();
			
			GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
			Connection conn = ds.getConnection();
			Statement stmt = conn.createStatement();
			assertEquals(stmt.executeUpdate("insert into crud(f1,f2) values(100,'str')"), 1);
			ResultSet rs = stmt.executeQuery("select f1,f2 from crud where f1=100");
			assertTrue(rs.next());

			GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(1);
			assertEquals(stmt.executeUpdate("insert into crud(f1,f2) values(100,'str')"), 1);
			rs = stmt.executeQuery("select count(*) from crud where f1=100");
			assertTrue(rs.next());
			assertEquals(rs.getInt(1), 1);

			stmt.close();
			conn.close();
		} finally {
			ThreadLocalMap.put(ThreadLocalString.DATASOURCE_INDEX, null);
		}
	}
}
