/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.Test;

import com.taobao.tddl.interact.rule.bean.DBType;
import com.taobao.tddl.jdbc.group.testutil.DBHelper;
import com.taobao.tddl.jdbc.group.testutil.DataSourceFactory;

/**
 *
 * @author yangzhu
 *
 */
public class TGroupStatementUnitTest {
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		DBHelper.deleteAll();
	}

	@Test
	public void java_sql_Statement_api_support() throws Exception {
		TGroupDataSource ds = new TGroupDataSource();
		ds.init(new DataSourceWrapper("dbKey1", "rw", DataSourceFactory.getMySQLDataSource(), DBType.MYSQL));

		Connection conn = ds.getConnection();
		Statement stmt = conn.createStatement();

		String insertSQL = "insert into crud(f1,f2) values(10,'str')";
		String updateSQL = "update crud set f2='str2'";
		String selectSQL = "select * from crud";
		String showSQL = "show create table crud";

		//Statement.execute如果第一个结果为 ResultSet 对象，则返回 true；如果其为更新计数或者不存在任何结果，则返回 false
		assertFalse(stmt.execute(insertSQL));
		assertTrue(stmt.execute(selectSQL));
		assertTrue(stmt.execute(showSQL));

		assertFalse(stmt.execute(insertSQL, Statement.RETURN_GENERATED_KEYS));
		assertTrue(stmt.getGeneratedKeys().next());

		assertFalse(stmt.execute(insertSQL, new int[] { 1 }));
		assertTrue(stmt.getGeneratedKeys().next());

		assertFalse(stmt.execute(insertSQL, new String[] { "col" }));
		assertTrue(stmt.getGeneratedKeys().next());

		stmt.addBatch(insertSQL);
		stmt.addBatch(updateSQL);

		int[] updateCounts = stmt.executeBatch();

		assertEquals(updateCounts.length, 2);

		assertTrue(stmt.executeQuery(selectSQL).next());

		assertEquals(stmt.executeUpdate(insertSQL), 1);

		assertEquals(stmt.executeUpdate(insertSQL, Statement.RETURN_GENERATED_KEYS), 1);
		assertTrue(stmt.getGeneratedKeys().next());

		assertEquals(stmt.executeUpdate(insertSQL, new int[] { 1 }), 1);
		assertTrue(stmt.getGeneratedKeys().next());

		assertEquals(stmt.executeUpdate(insertSQL, new String[] { "col" }), 1);
		assertTrue(stmt.getGeneratedKeys().next());

		stmt.close();
		conn.close();
	}
}
