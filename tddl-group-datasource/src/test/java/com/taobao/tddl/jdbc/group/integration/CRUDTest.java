/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.tddl.interact.rule.bean.DBType;
import com.taobao.tddl.jdbc.group.DataSourceWrapper;
import com.taobao.tddl.jdbc.group.TGroupDataSource;
import com.taobao.tddl.jdbc.group.testutil.DBHelper;
import com.taobao.tddl.jdbc.group.testutil.DataSourceFactory;

/**
 * 不使用TAtomDataSource，基于org.apache.commons.dbcp.BasicDataSource测试crud
 * 
 * @author yangzhu
 *
 */
public class CRUDTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//DBHelper.deleteAll(); //删除三个库中crud表的所有记录
	}

	@AfterClass
	public static void tearDownAfterClass() {

	}
	
	@Before
	public void setUp() throws Exception {
		DBHelper.deleteAll();
	}

	@Test
	public void 单个数据库() throws Exception {
		TGroupDataSource ds = new TGroupDataSource();
		DataSourceWrapper dsw = new DataSourceWrapper("db1", "rw", DataSourceFactory.getMySQLDataSource(), DBType.MYSQL);
		ds.init(dsw);
		
		Connection conn = ds.getConnection();

		//测试Statement的crud
		Statement stmt = conn.createStatement();
		assertEquals(stmt.executeUpdate("insert into crud(f1,f2) values(10,'str')"), 1);
		assertEquals(stmt.executeUpdate("update crud set f2='str2'"), 1);
		ResultSet rs = stmt.executeQuery("select f1,f2 from crud");
		rs.next();
		assertEquals(rs.getInt(1), 10);
		assertEquals(rs.getString(2), "str2");
		assertEquals(stmt.executeUpdate("delete from crud"), 1);
		rs.close();
		stmt.close();

		//测试PreparedStatement的crud
		String sql = "insert into crud(f1,f2) values(?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, 10);
		ps.setString(2, "str");
		assertEquals(ps.executeUpdate(), 1);
		ps.close();

		sql = "update crud set f2=?";
		ps = conn.prepareStatement(sql);
		ps.setString(1, "str2");
		assertEquals(ps.executeUpdate(), 1);
		ps.close();

		sql = "select f1,f2 from crud";
		ps = conn.prepareStatement(sql);
		rs = ps.executeQuery();
		rs.next();
		assertEquals(rs.getInt(1), 10);
		assertEquals(rs.getString(2), "str2");
		rs.close();
		ps.close();

		sql = "delete from crud";
		ps = conn.prepareStatement(sql);
		assertEquals(ps.executeUpdate(), 1);
		ps.close();

		conn.close();
	}
	
	@Test
	public void 测试DataSourceWrapper() throws Exception {
		List<DataSourceWrapper> dataSourceWrappers = new ArrayList<DataSourceWrapper>();
		dataSourceWrappers.add(new DataSourceWrapper("dbKey1","rw", DataSourceFactory.getMySQLDataSource(1), DBType.MYSQL));
		dataSourceWrappers.add(new DataSourceWrapper("dbKey2","r", DataSourceFactory.getMySQLDataSource(2), DBType.MYSQL));

		TGroupDataSource ds = new TGroupDataSource();
		ds.setDbGroupKey("myDbGroupKey");
		ds.init(dataSourceWrappers);

		Connection conn = ds.getConnection();

		//测试Statement的crud
		Statement stmt = conn.createStatement();
		assertEquals(stmt.executeUpdate("insert into crud(f1,f2) values(10,'str')"), 1);
		assertEquals(stmt.executeUpdate("update crud set f2='str2'"), 1);
		ResultSet rs = stmt.executeQuery("select f1,f2 from crud");
		rs.next();
		assertEquals(rs.getInt(1), 10);
		assertEquals(rs.getString(2), "str2");
		assertEquals(stmt.executeUpdate("delete from crud"), 1);
		rs.close();
		stmt.close();

		//测试PreparedStatement的crud
		String sql = "insert into crud(f1,f2) values(?,?)";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setInt(1, 10);
		ps.setString(2, "str");
		assertEquals(ps.executeUpdate(), 1);
		ps.close();

		sql = "update crud set f2=?";
		ps = conn.prepareStatement(sql);
		ps.setString(1, "str2");
		assertEquals(ps.executeUpdate(), 1);
		ps.close();

		sql = "select f1,f2 from crud";
		ps = conn.prepareStatement(sql);
		rs = ps.executeQuery();
		rs.next();
		assertEquals(rs.getInt(1), 10);
		assertEquals(rs.getString(2), "str2");
		rs.close();
		ps.close();

		sql = "delete from crud";
		ps = conn.prepareStatement(sql);
		assertEquals(ps.executeUpdate(), 1);
		ps.close();

		conn.close();
	}

	//dbGroup: db1:r10w, db2:r20, db3:r30
	@Test
	public void 三个数据库_测试db1可读写_db2与db3只能读() throws Exception {
		DataSource ds1 = DataSourceFactory.getMySQLDataSource(1);
		DataSource ds2 = DataSourceFactory.getMySQLDataSource(2);
		DataSource ds3 = DataSourceFactory.getMySQLDataSource(3);
		
		
		//读库时最有可能从db3读，然后是db2，db1的权重最小
		TGroupDataSource ds = new TGroupDataSource();
		DataSourceWrapper dsw1 = new DataSourceWrapper("db1", "r10w", ds1, DBType.MYSQL);
		DataSourceWrapper dsw2 = new DataSourceWrapper("db2", "r20", ds2, DBType.MYSQL);
		DataSourceWrapper dsw3 = new DataSourceWrapper("db3", "r30", ds3, DBType.MYSQL);
		ds.init(dsw1, dsw2, dsw3);

		Connection conn = ds.getConnection();

		//测试Statement的crud
		Statement stmt = conn.createStatement();
		assertEquals(stmt.executeUpdate("insert into crud(f1,f2) values(10,'str')"), 1);
		assertEquals(stmt.executeUpdate("update crud set f2='str2'"), 1);
		ResultSet rs = stmt.executeQuery("select f1,f2 from crud");
		rs.next();
		assertEquals(rs.getInt(1), 10);
		assertEquals(rs.getString(2), "str2");
		assertEquals(stmt.executeUpdate("delete from crud"), 1);
		rs.close();
		stmt.close();

		//测试PreparedStatement的crud
		String sql = "insert into crud(f1,f2) values(10,'str')";
		PreparedStatement ps = conn.prepareStatement(sql);
		assertEquals(ps.executeUpdate(), 1);
		ps.close();

		sql = "update crud set f2='str2'";
		ps = conn.prepareStatement(sql);
		assertEquals(ps.executeUpdate(), 1);
		ps.close();

		sql = "select f1,f2 from crud";
		ps = conn.prepareStatement(sql);
		rs = ps.executeQuery();
		rs.next();
		assertEquals(rs.getInt(1), 10);
		assertEquals(rs.getString(2), "str2");
		rs.close();
		ps.close();

		sql = "delete from crud";
		ps = conn.prepareStatement(sql);
		assertEquals(ps.executeUpdate(), 1);
		ps.close();

		conn.close();
	}

	//dbGroup: db1:w, db2:r20, db3:r30
	@Test
	public void 在只写库上更新后再查询会重用写库上的连接_即使它是一个只写库也不管() throws Exception { //不支持这种只能写的情况
		DataSource ds1 = DataSourceFactory.getMySQLDataSource(1);
		DataSource ds2 = DataSourceFactory.getMySQLDataSource(2);
		DataSource ds3 = DataSourceFactory.getMySQLDataSource(3);
		
		//读库时最有可能从db3读，然后是db2，db1的权重最小
		TGroupDataSource ds = new TGroupDataSource();
		DataSourceWrapper dsw1 = new DataSourceWrapper("db1", "w", ds1, DBType.MYSQL);
		DataSourceWrapper dsw2 = new DataSourceWrapper("db2", "r20", ds2, DBType.MYSQL);
		DataSourceWrapper dsw3 = new DataSourceWrapper("db3", "r30", ds3, DBType.MYSQL);
		ds.init(dsw1, dsw2, dsw3);
		Connection conn = ds.getConnection();

		Statement stmt = conn.createStatement();
		assertEquals(stmt.executeUpdate("insert into crud(f1,f2) values(100,'str')"), 1);
		
		//在只写库上更新后，会保留写连接，
		//但是因为写连接对应的数据源被配置成只写，所以接下来的读操作不允许在写连接上进行
		//因为db2,db3都没有数据，所以rs.next()返回false
		ResultSet rs = stmt.executeQuery("select f1,f2 from crud where f1=100");
		assertFalse(rs.next());
		rs.close();

		assertEquals(stmt.executeUpdate("delete from crud where f1=100"), 1);
		stmt.close();

		conn.close();
	}
}
