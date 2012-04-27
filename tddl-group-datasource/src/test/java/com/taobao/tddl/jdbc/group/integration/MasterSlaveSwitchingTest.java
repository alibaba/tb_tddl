/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.integration;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

import com.taobao.tddl.jdbc.group.TGroupDataSource;
import com.taobao.tddl.jdbc.group.testutil.DBHelper;

/**
 * 
 * @author yangzhu
 *
 */
public class MasterSlaveSwitchingTest {
	public static final String appName = "unitTest";
	public static final String dbGroupKey = "myDbGroupKey2";

	@Before
	public void setUp() throws Exception {
		DBHelper.deleteAll();
	}

	@Test
	public void switching() throws Exception {
		if (true)
			return; //要手工测试注释掉这一行

		TGroupDataSource ds = new TGroupDataSource(dbGroupKey, appName);
		ds.setAutoSelectWriteDataSource(true);
		ds.init();

		Connection conn = ds.getConnection();
		Statement stmt = conn.createStatement();

		//先往主库插入一条记录
		assertEquals(stmt.executeUpdate("insert into crud(f1,f2) values(10,'str')"), 1);

		System.out.println("已往主库插入一条记录...");

		System.out.println("把主库的状态改成NA，休息一分半钟..."); //同时请关闭MySQL数据库服务器
		Thread.sleep(90 * 1000);
		try {
			stmt.executeUpdate("update crud set f2='str2'");
			fail("主库处于NA状态不能进行更新操作");
		} catch (Exception e) {
			System.out.println("主库当前处于NA状态，无法进行更新操作，Exception: " + e);

			System.out.println("重建Connection"); //不能基于原有Connection重建Statement
			conn = ds.getConnection();
			System.out.println("重建Statement");
			stmt = conn.createStatement();
		}

		System.out.println("把备1的状态改成RW，休息半分钟...");
		Thread.sleep(30 * 1000);

		try {
			assertEquals(stmt.executeUpdate("insert into crud(f1,f2) values(10,'str')"), 1);
			System.out.println("已往备1库中插入一条记录...");
		} catch (Exception e) {
			e.printStackTrace();
			fail("备1的状态已改成RW，但是不能进行更新操作");
		}

		stmt.close();
		conn.close();
	}
}
