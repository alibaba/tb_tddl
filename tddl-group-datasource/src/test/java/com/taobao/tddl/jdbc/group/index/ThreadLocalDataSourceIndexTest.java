/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.index;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.taobao.tddl.common.GroupDataSourceRouteHelper;
import com.taobao.tddl.common.mockdatasource.MockDataSource;
import com.taobao.tddl.common.util.DataSourceFetcher;
import com.taobao.tddl.interact.rule.bean.DBType;
import com.taobao.tddl.jdbc.group.TGroupDataSource;

/**
 * 
 * @author linxuan
 *
 */
public class ThreadLocalDataSourceIndexTest {

	@BeforeClass
	public static void beforeClass() {
	}

	private static MockDataSource createMockDataSource(String name) {
		MockDataSource mds = new MockDataSource();
		mds.setName(name);
		mds.setDbIndex("");
		return mds;
	}

	private static TGroupDataSource createGroupDataSource(String weightStr) {
		TGroupDataSource tgds = new TGroupDataSource();
		tgds.setDsKeyAndWeightCommaArray(weightStr);
		tgds.setDbType(DBType.MYSQL);
		tgds.setDataSourceFetcher(new DataSourceFetcher() {
			@Override
			public DataSource getDataSource(String key) {
				return createMockDataSource(key);
			}

			@Override
			public DBType getDataSourceDBType(String key) {
				return null;
			}
		});
		tgds.init();
		return tgds;
	}

	@Test
	public void test_不设i() {
		JdbcTemplate jt = new JdbcTemplate(createGroupDataSource("ds0:rw, ds1:r, ds2:r, ds3:r"));

		MockDataSource.clearTrace();
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(1);
		jt.query("select 1 from dual", new Object[] {}, new ColumnMapRowMapper());
		MockDataSource.showTrace();
		Assert.assertTrue(MockDataSource.hasTrace("", "ds1", "select 1 from dual"));

		MockDataSource.clearTrace();
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(2);
		jt.query("select 1 from dual", new Object[] {}, new ColumnMapRowMapper());
		MockDataSource.showTrace();
		Assert.assertTrue(MockDataSource.hasTrace("", "ds2", "select 1 from dual"));
	}

	@Test
	public void test_设单个i不加数字等同于没设() {
		JdbcTemplate jt = new JdbcTemplate(createGroupDataSource("ds0:rwi, ds1:ri, ds2:ri, ds3:ri"));

		MockDataSource.clearTrace();
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(1);
		jt.query("select 1 from dual", new Object[] {}, new ColumnMapRowMapper());
		MockDataSource.showTrace();
		Assert.assertTrue(MockDataSource.hasTrace("", "ds1", "select 1 from dual"));
	}

	@Test
	public void test_设单个in() {
		JdbcTemplate jt = new JdbcTemplate(createGroupDataSource("ds0:rwi5, ds1:ri6, ds2:ri7, ds3:ri8"));

		MockDataSource.clearTrace();
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(6);
		jt.query("select 1 from dual", new Object[] {}, new ColumnMapRowMapper());
		MockDataSource.showTrace();
		Assert.assertTrue(MockDataSource.hasTrace("", "ds1", "select 1 from dual"));

		MockDataSource.clearTrace();
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(8);
		jt.query("select 1 from dual", new Object[] {}, new ColumnMapRowMapper());
		MockDataSource.showTrace();
		Assert.assertTrue(MockDataSource.hasTrace("", "ds3", "select 1 from dual"));
	}

	@Test
	public void test_设多个i分流() {
		JdbcTemplate jt = new JdbcTemplate(createGroupDataSource("ds0:rwi0, ds1:ri0, ds2:ri1, ds3:ri1"));

		MockDataSource.clearTrace();
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
		jt.query("select 1 from dual", new Object[] {}, new ColumnMapRowMapper());
		MockDataSource.showTrace();
		Assert.assertTrue(MockDataSource.hasTrace("", "ds0", "select") || MockDataSource.hasTrace("", "ds1", "select"));

		MockDataSource.clearTrace();
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(1);
		jt.query("select 1 from dual", new Object[] {}, new ColumnMapRowMapper());
		MockDataSource.showTrace();
		Assert.assertTrue(MockDataSource.hasTrace("", "ds2", "select") || MockDataSource.hasTrace("", "ds3", "select"));
	}

	@Test
	public void test_1个ds设多个i() {
		JdbcTemplate jt = new JdbcTemplate(createGroupDataSource("ds0:rwi0, ds1:ri0i1, ds2:ri1, ds3:r, ds4:ri3"));

		MockDataSource.clearTrace();
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
		jt.query("select 1 from dual", new Object[] {}, new ColumnMapRowMapper());
		MockDataSource.showTrace();
		Assert.assertTrue(MockDataSource.hasTrace("", "ds0", "select") || MockDataSource.hasTrace("", "ds1", "select"));

		MockDataSource.clearTrace();
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(1);
		jt.query("select 1 from dual", new Object[] {}, new ColumnMapRowMapper());
		MockDataSource.showTrace();
		Assert.assertTrue(MockDataSource.hasTrace("", "ds1", "select") || MockDataSource.hasTrace("", "ds2", "select"));

		MockDataSource.clearTrace();
		GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(3);
		jt.query("select 1 from dual", new Object[] {}, new ColumnMapRowMapper());
		MockDataSource.showTrace();
		Assert.assertTrue(MockDataSource.hasTrace("", "ds3", "select") || MockDataSource.hasTrace("", "ds4", "select"));
	}
}
