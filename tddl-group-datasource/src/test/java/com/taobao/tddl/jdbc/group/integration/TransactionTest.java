/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.integration;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.taobao.tddl.jdbc.group.SpringTGroupDataSource;
import com.taobao.tddl.jdbc.group.testutil.DBHelper;

/**
 * 不使用TAtomDataSource，基于org.apache.commons.dbcp.BasicDataSource测试crud
 * 
 * @author yangzhu
 * 
 */
public class TransactionTest {
	private static ClassPathXmlApplicationContext ctx;
	private static SpringTGroupDataSource ds;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ctx = new ClassPathXmlApplicationContext(
				new String[] { "conf/springTGroupDataSource.xml" });
		ds = (SpringTGroupDataSource) ctx.getBean("ds");
	}

	@AfterClass
	public static void tearDownAfterClass() {

	}

	@Before
	public void setUp() throws Exception {
		DBHelper.deleteAll();
	}

	// dbGroup: db1:r10w, db2:r20, db3:r30
	@Test
	public void springTest() throws Exception {

		Connection conn = ds.getConnection();
		conn.setAutoCommit(false);

		// 测试Statement的crud
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("select f1,f2 from crud");
		assertEquals(stmt
				.executeUpdate("insert into crud(f1,f2) values(10,'str')"), 1);
		assertEquals(stmt
				.executeUpdate("insert into crud(f1,f2) values(10,'str')"), 1);
		conn.commit();
		conn.setAutoCommit(true);
		rs = stmt.executeQuery("select f1,f2 from crud");
		rs.next();
		assertEquals(rs.getInt(1), 10);
		assertEquals(rs.getString(2), "str");
		rs.close();

		assertEquals(stmt.executeUpdate("delete from crud"), 2);

		stmt.close();
		conn.close();
	}
}
