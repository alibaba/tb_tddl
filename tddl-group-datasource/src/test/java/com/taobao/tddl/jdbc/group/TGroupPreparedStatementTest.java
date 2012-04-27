/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.tddl.common.mockdatasource.MockDataSource;
import com.taobao.tddl.interact.rule.bean.DBType;

public class TGroupPreparedStatementTest {
	private static TGroupDataSource tgds;
	private static MockDataSource db1 = new MockDataSource("db", "db1");
	private static MockDataSource db2 = new MockDataSource("db", "db2");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tgds = new TGroupDataSource();
		tgds.setDbGroupKey("dbKey0");
		List<DataSourceWrapper> dataSourceWrappers = new ArrayList<DataSourceWrapper>();
		DataSourceWrapper dsw1 = new DataSourceWrapper("db1", "rw", db1, DBType.MYSQL);
		DataSourceWrapper dsw2 = new DataSourceWrapper("db2", "r", db2, DBType.MYSQL);
		dataSourceWrappers.add(dsw1);
		dataSourceWrappers.add(dsw2);
		tgds.init(dataSourceWrappers);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		tgds = null;
	}

	@Test
	public void testAddBatch() throws SQLException {
		MockDataSource.clearTrace();
		TGroupConnection conn = null;
		PreparedStatement stat = null;
		try {
			conn = tgds.getConnection();
			stat = conn.prepareStatement("update test set type=? where id = ?");

			stat.setInt(1, 1);
			stat.setString(2, "2askjfoue33");
			stat.addBatch();

			stat.setInt(1, 2);
			stat.setString(2, "retrtorut48");
			stat.addBatch();

			int[] affectedRow = stat.executeBatch();
			System.out.println(Arrays.toString(affectedRow));
			MockDataSource.showTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
				}
			if (stat != null)
				try {
					stat.close();
				} catch (SQLException e) {
				}
		}
	}

	@Test
	public void testAddBatchSql() throws SQLException {
		MockDataSource.clearTrace();
		TGroupConnection conn = null;
		Statement stat = null;
		try {
			conn = tgds.getConnection();
			stat = conn.createStatement();
			stat.addBatch("update t set name = 'newName' ");
			stat.addBatch("update t set type = 2 ");
			int[] affectedRow = stat.executeBatch();
			System.out.println(Arrays.toString(affectedRow));
			MockDataSource.showTrace();
			Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "executeBatch")
					|| MockDataSource.hasMethod("db", "db2", "executeBatch"));
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
				}
			if (stat != null)
				try {
					stat.close();
				} catch (SQLException e) {
				}
		}
	}

	/**
	 * 同一个Statement先更新后查询
	 */
	@Test
	public void testExecuteSql() throws SQLException {
		MockDataSource.clearTrace();
		TGroupConnection conn = null;
		Statement stat = null;
		try {
			conn = tgds.getConnection();
			stat = conn.createStatement();
			boolean res = stat.execute("update t set name = 'newName'");
			Assert.assertEquals(res, false);
			res = stat.execute("select * from xxx where id=0");
			Assert.assertEquals(res, true);
			MockDataSource.showTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
				}
			if (stat != null)
				try {
					stat.close();
				} catch (SQLException e) {
				}
		}
	}

	/**
	 * 同一个PreparedStatement先更新后查询
	 */
	@Test
	public void testExecute1() throws SQLException {
		MockDataSource.clearTrace();
		TGroupConnection conn = null;
		PreparedStatement stat = null;
		try {
			conn = tgds.getConnection();
			stat = conn.prepareStatement("update t set name = 'newName' where date = ?");
			stat.setDate(1, new java.sql.Date(System.currentTimeMillis()));
			boolean res = stat.execute();
			Assert.assertEquals(res, false);
			res = stat.execute("select * from xxx where id=0");
			Assert.assertEquals(res, true);
			MockDataSource.showTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
				}
			if (stat != null)
				try {
					stat.close();
				} catch (SQLException e) {
				}
		}
	}
	/**
	 * 同一个PreparedStatement先查询后更新
	 */
	@Test
	public void testExecute2() throws SQLException {
		MockDataSource.clearTrace();
		TGroupConnection conn = null;
		PreparedStatement stat = null;
		try {
			conn = tgds.getConnection();
			stat = conn.prepareStatement("select * from xxx where id=?");
			stat.setByte(1, (byte)5);
			boolean res = stat.execute();
			Assert.assertEquals(res, true);
			
			res = stat.execute("update t set name = 'newName'");
			Assert.assertEquals(res, false);
			MockDataSource.showTrace();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
				}
			if (stat != null)
				try {
					stat.close();
				} catch (SQLException e) {
				}
		}
	}

	@Test
	public void testExecuteQuery() {
	}

	@Test
	public void testExecuteUpdate() {
	}

	@Test
	public void testExecuteString() {
	}

	@Test
	public void testExecuteStringInt() {
	}

	@Test
	public void testExecuteStringIntArray() {
	}

	@Test
	public void testExecuteStringStringArray() {
	}

	@Test
	public void testExecuteUpdateString() {
	}

	@Test
	public void testExecuteUpdateStringInt() {
	}

	@Test
	public void testExecuteUpdateStringIntArray() {
	}

	@Test
	public void testExecuteUpdateStringStringArray() {
	}

	@Test
	public void testClearBatch() {
	}

}
