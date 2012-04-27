/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.jdbc;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.taobao.tddl.common.jdbc.QueryForMapHandler.TableMetaData;
import com.taobao.tddl.common.mockdatasource.MockDataSource;

public class MetaDataQueryForMapHandlerTest {
    private MetaDataQueryForMapHandler handler;
    private MockDataSource mds;
    private JdbcTemplate jt;

    @Before
	public void setUp() throws Exception {
		mds = new MockDataSource();
		mds.setDbIndex("dbindex0");
		jt = new JdbcTemplate(mds);
		handler = new MetaDataQueryForMapHandler();
	}

	@Test
	public void testQueryForMap() {
		MockDataSource.addPreData("sku_id:0,item_id:65,seller_id:63,name:'³ßÂë'");
		Object obj = handler.queryForMap(jt, "sku", null, "where item_id=?", new Object[]{5});
		MockDataSource.showTrace();
		Assert.assertTrue(MockDataSource.hasMethod("dbindex0", "ResultSet.getMetaData"));
		System.out.println(obj);
		
		MockDataSource.clearTrace();
		MockDataSource.addPreData("sku_id:0,item_id:65,seller_id:63,name:'³ßÂë'");
		obj = handler.queryForMap(jt, "sku", null, "where item_id=?", new Object[]{5});
		Assert.assertFalse(MockDataSource.hasMethod("dbindex0", "ResultSet.getMetaData"));
		MockDataSource.showTrace();
		System.out.println(obj);
	}

	@Test
	public void testGetTableMetaData() {
		MockDataSource.addPreData("sku_id:0,item_id:65,seller_id:63,name:'³ßÂë'");
		Object obj = handler.queryForMap(jt, "sku", null, "where item_id=?", new Object[]{5});
		MockDataSource.showTrace();
		Assert.assertTrue(MockDataSource.hasMethod("dbindex0", "ResultSet.getMetaData"));

		TableMetaData tmd = handler.getTableMetaData("sku");
		Assert.assertEquals(tmd.commaColumnNames, "sku_id,item_id,seller_id,name");
		//Assert.assertEquals(tmd.columns[0].sqlType, java.sql.Types.INTEGER);
		System.out.println(tmd.columns[0].sqlType);
		System.out.println(tmd.columns[3].sqlType);
	}

}
