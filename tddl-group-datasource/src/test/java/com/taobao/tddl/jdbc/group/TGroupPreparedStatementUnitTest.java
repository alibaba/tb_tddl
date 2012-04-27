/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
public class TGroupPreparedStatementUnitTest {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		DBHelper.deleteAll();
	}

	@Test
	public void java_sql_Statement_api_support() throws Exception {
		TGroupDataSource ds = new TGroupDataSource();
		ds.init(new DataSourceWrapper("dbKey1", "rw", DataSourceFactory.getMySQLDataSource(), DBType.MYSQL));

		String insertSQL = "insert into crud(f1,f2) values(10,'str')";
		String updateSQL = "update crud set f2='str2'";
		String selectSQL = "select * from crud";

		Connection conn = ds.getConnection();
		PreparedStatement stmt = conn.prepareStatement(insertSQL);
		//Statement.execute如果第一个结果为 ResultSet 对象，则返回 true；如果其为更新计数或者不存在任何结果，则返回 false
		assertFalse(stmt.execute());
		stmt.close();
		
		stmt = conn.prepareStatement(selectSQL);
		assertTrue(stmt.execute());
		stmt.close();
		
		stmt = conn.prepareStatement(selectSQL);
		assertTrue(stmt.executeQuery().next());
		stmt.close();

		stmt = conn.prepareStatement(updateSQL);
		assertFalse(stmt.execute());
		stmt.close();

		stmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
		assertFalse(stmt.execute());
		stmt.close();

		stmt = conn.prepareStatement(insertSQL, new int[] { 1 });
		assertFalse(stmt.execute());
		stmt.close();

		stmt = conn.prepareStatement(insertSQL, new String[] { "col" });
		assertFalse(stmt.execute());

		assertEquals(stmt.executeUpdate(), 1);
		stmt.close();

		//测试批量更新
		stmt = conn.prepareStatement("insert into crud(f1,f2) values(?,?)");
		stmt.setInt(1, 10);
		stmt.setString(2, "str1");
		stmt.addBatch();

		stmt.setInt(1, 20);
		stmt.setString(2, "str2");
		stmt.addBatch();

		int[] updateCounts = stmt.executeBatch();
		assertEquals(updateCounts.length, 2);
		stmt.close();

		conn.close();
	}
}
