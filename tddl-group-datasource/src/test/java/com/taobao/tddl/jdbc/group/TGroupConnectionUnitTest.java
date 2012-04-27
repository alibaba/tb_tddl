/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.taobao.tddl.common.mockdatasource.MockDataSource;
import com.taobao.tddl.interact.rule.bean.DBType;

/**
 * 
 * @author yangzhu
 *
 */
public class TGroupConnectionUnitTest {

	@Test
	public void java_sql_Connection_api_support() throws Exception {
		TGroupDataSource ds = new TGroupDataSource();

		Connection conn = ds.getConnection();
		assertFalse(conn.isClosed());
		assertTrue(conn.getAutoCommit());
		assertNull(conn.getWarnings());
		assertTrue((conn.getMetaData() instanceof TGroupDatabaseMetaData));

		assertTrue((conn.createStatement() instanceof TGroupStatement));
		assertTrue((conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE) instanceof TGroupStatement));
		assertTrue((conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE,
				ResultSet.HOLD_CURSORS_OVER_COMMIT) instanceof TGroupStatement));

		assertTrue((conn.prepareStatement("sql") instanceof TGroupPreparedStatement));
		assertTrue((conn.prepareStatement("sql", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE) instanceof TGroupPreparedStatement));
		assertTrue((conn.prepareStatement("sql", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE,
				ResultSet.HOLD_CURSORS_OVER_COMMIT) instanceof TGroupPreparedStatement));
		assertTrue((conn.prepareStatement("sql", Statement.RETURN_GENERATED_KEYS) instanceof TGroupPreparedStatement));
		assertTrue((conn.prepareStatement("sql", new int[0]) instanceof TGroupPreparedStatement));
		assertTrue((conn.prepareStatement("sql", new String[0]) instanceof TGroupPreparedStatement));

	}

//	已经支持存储过程
//	@Test(expected = UnsupportedOperationException.class)
//	public void java_sql_Connection_api_not_support() throws Exception {
//		TGroupDataSource ds = new TGroupDataSource();
//
//		Connection conn = ds.getConnection();
//		conn.prepareCall("sql");
//
//	}

	@Test
	public void test_一个连接上创建两个Statement() {
		TGroupDataSource tgds = new TGroupDataSource();
		tgds.setDbGroupKey("dbKey0");
		List<DataSourceWrapper> dataSourceWrappers = new ArrayList<DataSourceWrapper>();
		MockDataSource db1 = new MockDataSource("db", "db1");
		MockDataSource db2 = new MockDataSource("db", "db2");
		DataSourceWrapper dsw1 = new DataSourceWrapper("db1", "rw", db1, DBType.MYSQL);
		DataSourceWrapper dsw2 = new DataSourceWrapper("db2", "r", db2, DBType.MYSQL);
		dataSourceWrappers.add(dsw1);
		dataSourceWrappers.add(dsw2);
		tgds.init(dataSourceWrappers);

		TGroupConnection conn = null;
		Statement stat = null;
		try {
			db1.setClosed(true);
			db2.setClosed(false);
			conn = tgds.getConnection();
			stat = conn.createStatement();
			stat.executeQuery("select 1 from test");
			MockDataSource.showTrace();
			Assert.assertTrue(MockDataSource.hasTrace("db", "db2", "select 1 from test"));

			db1.setClosed(false);
			db2.setClosed(true);
			stat = conn.createStatement();
			stat.executeQuery("select 2 from test");
			//Assert.assertTrue(MockDataSource.hasTrace("db", "db1", "select 1 from test"));
			Assert.fail("没有重用第一个连接");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
	public void test_创建Statement失败重试() {
		TGroupDataSource tgds = new TGroupDataSource();
		tgds.setDbGroupKey("dbKey0");
		List<DataSourceWrapper> dataSourceWrappers = new ArrayList<DataSourceWrapper>();
		MockDataSource db1 = new MockDataSource("db", "db1");
		MockDataSource db2 = new MockDataSource("db", "db2");
		DataSourceWrapper dsw1 = new DataSourceWrapper("db1", "rw", db1, DBType.MYSQL);
		DataSourceWrapper dsw2 = new DataSourceWrapper("db2", "r", db2, DBType.MYSQL);
		dataSourceWrappers.add(dsw1);
		dataSourceWrappers.add(dsw2);
		tgds.init(dataSourceWrappers);

		TGroupConnection conn = null;
		Statement stat = null;
		try {
			conn = tgds.getConnection();
			stat = conn.createStatement();
			
			MockDataSource.addPreException(MockDataSource.m_createStatement, db1.genFatalSQLException());
			stat.executeQuery("select 1 from test");
			MockDataSource.showTrace();
			Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "getConnection"));
			Assert.assertTrue(MockDataSource.hasMethod("db", "db2", "getConnection"));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
    public void test_autocommit() {
        MockDataSource.clearTrace();
        TGroupDataSource tgds = new TGroupDataSource();
        tgds.setDbGroupKey("dbKey0");
        List<DataSourceWrapper> dataSourceWrappers = new ArrayList<DataSourceWrapper>();
        MockDataSource db1 = new MockDataSource("db", "db1");
        MockDataSource db2 = new MockDataSource("db", "db2");
        DataSourceWrapper dsw1 = new DataSourceWrapper("db1", "w", db1, DBType.MYSQL);
        DataSourceWrapper dsw2 = new DataSourceWrapper("db2", "r", db2, DBType.MYSQL);
        dataSourceWrappers.add(dsw1);
        dataSourceWrappers.add(dsw2);
        tgds.init(dataSourceWrappers);

        TGroupConnection conn = null;
        Statement stat = null;
        try {
            conn = tgds.getConnection();
            stat = conn.createStatement();
            stat.executeQuery("select 1 from test");
            conn.setAutoCommit(false);
            stat.executeUpdate("update t set name='newName'");
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
        MockDataSource.showTrace();
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "getConnection"));
        Assert.assertTrue(MockDataSource.hasMethod("db", "db2", "getConnection"));
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "setAutoCommit"));
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "commit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db1", "rollback"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "setAutoCommit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "commit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "rollback"));
    }

	@Test
	public void test_no_trans() {
        MockDataSource.clearTrace();
		TGroupDataSource tgds = new TGroupDataSource();
		tgds.setDbGroupKey("dbKey0");
		List<DataSourceWrapper> dataSourceWrappers = new ArrayList<DataSourceWrapper>();
		MockDataSource db1 = new MockDataSource("db", "db1");
		MockDataSource db2 = new MockDataSource("db", "db2");
		DataSourceWrapper dsw1 = new DataSourceWrapper("db1", "w", db1, DBType.MYSQL);
		DataSourceWrapper dsw2 = new DataSourceWrapper("db2", "r", db2, DBType.MYSQL);
		dataSourceWrappers.add(dsw1);
		dataSourceWrappers.add(dsw2);
		tgds.init(dataSourceWrappers);

		TGroupConnection conn = null;
		Statement stat = null;
		try {
			conn = tgds.getConnection();
			stat = conn.createStatement();
			stat.executeQuery("select 1 from test");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
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
        MockDataSource.showTrace();
        Assert.assertFalse(MockDataSource.hasMethod("db", "db1", "getConnection"));
        Assert.assertTrue(MockDataSource.hasMethod("db", "db2", "getConnection"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db1", "setAutoCommit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db1", "commit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db1", "rollback"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "setAutoCommit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "commit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "rollback"));
	}

    @Test
    public void test_write_trans() {
        MockDataSource.clearTrace();
        TGroupDataSource tgds = new TGroupDataSource();
        tgds.setDbGroupKey("dbKey0");
        List<DataSourceWrapper> dataSourceWrappers = new ArrayList<DataSourceWrapper>();
        MockDataSource db1 = new MockDataSource("db", "db1");
        MockDataSource db2 = new MockDataSource("db", "db2");
        DataSourceWrapper dsw1 = new DataSourceWrapper("db1", "w", db1, DBType.MYSQL);
        DataSourceWrapper dsw2 = new DataSourceWrapper("db2", "r", db2, DBType.MYSQL);
        dataSourceWrappers.add(dsw1);
        dataSourceWrappers.add(dsw2);
        tgds.init(dataSourceWrappers);

        TGroupConnection conn = null;
        Statement stat = null;
        try {
            conn = tgds.getConnection();
            stat = conn.createStatement();
            conn.setAutoCommit(false);
            stat.executeUpdate("update t set name='newName'");
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
        MockDataSource.showTrace();
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "getConnection"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "getConnection"));
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "setAutoCommit"));
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "commit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db1", "rollback"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "setAutoCommit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "commit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "rollback"));
    }
    
    @Test
    public void test_read_trans() {
        MockDataSource.clearTrace();
        TGroupDataSource tgds = new TGroupDataSource();
        tgds.setDbGroupKey("dbKey0");
        List<DataSourceWrapper> dataSourceWrappers = new ArrayList<DataSourceWrapper>();
        MockDataSource db1 = new MockDataSource("db", "db1");
        MockDataSource db2 = new MockDataSource("db", "db2");
        DataSourceWrapper dsw1 = new DataSourceWrapper("db1", "w", db1, DBType.MYSQL);
        DataSourceWrapper dsw2 = new DataSourceWrapper("db2", "r", db2, DBType.MYSQL);
        dataSourceWrappers.add(dsw1);
        dataSourceWrappers.add(dsw2);
        tgds.init(dataSourceWrappers);

        TGroupConnection conn = null;
        Statement stat = null;
        try {
            conn = tgds.getConnection();
            stat = conn.createStatement();
            conn.setAutoCommit(false);
            stat.executeQuery("select 1 from test");
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
        MockDataSource.showTrace();
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "getConnection"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "getConnection"));
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "setAutoCommit"));
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "commit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db1", "rollback"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "setAutoCommit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "commit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "rollback"));
    }
    
    @Test
    public void test_write_and_read_trans() {
        MockDataSource.clearTrace();
        TGroupDataSource tgds = new TGroupDataSource();
        tgds.setDbGroupKey("dbKey0");
        List<DataSourceWrapper> dataSourceWrappers = new ArrayList<DataSourceWrapper>();
        MockDataSource db1 = new MockDataSource("db", "db1");
        MockDataSource db2 = new MockDataSource("db", "db2");
        DataSourceWrapper dsw1 = new DataSourceWrapper("db1", "w", db1, DBType.MYSQL);
        DataSourceWrapper dsw2 = new DataSourceWrapper("db2", "r", db2, DBType.MYSQL);
        dataSourceWrappers.add(dsw1);
        dataSourceWrappers.add(dsw2);
        tgds.init(dataSourceWrappers);

        TGroupConnection conn = null;
        Statement stat = null;
        try {
            conn = tgds.getConnection();
            stat = conn.createStatement();
            conn.setAutoCommit(false);
            stat.executeQuery("update t set name='newName'");
            stat.executeQuery("select 1 from test");
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
        MockDataSource.showTrace();
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "getConnection"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "getConnection"));
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "setAutoCommit"));
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "commit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db1", "rollback"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "setAutoCommit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "commit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "rollback"));
    }
    @Test
    public void test_read_and_write_trans() {
        MockDataSource.clearTrace();
        TGroupDataSource tgds = new TGroupDataSource();
        tgds.setDbGroupKey("dbKey0");
        List<DataSourceWrapper> dataSourceWrappers = new ArrayList<DataSourceWrapper>();
        MockDataSource db1 = new MockDataSource("db", "db1");
        MockDataSource db2 = new MockDataSource("db", "db2");
        DataSourceWrapper dsw1 = new DataSourceWrapper("db1", "w", db1, DBType.MYSQL);
        DataSourceWrapper dsw2 = new DataSourceWrapper("db2", "r", db2, DBType.MYSQL);
        dataSourceWrappers.add(dsw1);
        dataSourceWrappers.add(dsw2);
        tgds.init(dataSourceWrappers);

        TGroupConnection conn = null;
        Statement stat = null;
        try {
            conn = tgds.getConnection();
            stat = conn.createStatement();
            conn.setAutoCommit(false);
            stat.executeQuery("select 1 from test");
            stat.executeQuery("update t set name='newName'");
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
        MockDataSource.showTrace();
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "getConnection"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "getConnection"));
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "setAutoCommit"));
        Assert.assertTrue(MockDataSource.hasMethod("db", "db1", "commit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db1", "rollback"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "setAutoCommit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "commit"));
        Assert.assertFalse(MockDataSource.hasMethod("db", "db2", "rollback"));
    }
}
