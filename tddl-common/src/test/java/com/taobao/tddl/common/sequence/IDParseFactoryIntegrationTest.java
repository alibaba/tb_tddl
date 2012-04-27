/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sequence;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.tddl.common.sequence.IDParse.DetachID;

public class IDParseFactoryIntegrationTest {
	
	private IDParseFactory factory;
	private Config.Factory configFactory;

	@Before
	public void setUp() throws Exception {
		factory = IDParseFactory.newInstance();
		configFactory = new Config.Factory("/generators.xml");
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateIDParse() {
		long id = 1234567L;
		//Mon Aug 10 00:00:00 CST 2009
		long time = 1249833600000L - IDParse4CTU3.Year2000Time;
		Long rawId = time * 10000000 + id;

		IDParse idParse = factory.createIDParse(configFactory.newInstance("gid1"));
		DetachID<Long, Integer, Integer> detachID1 = idParse.parse(rawId);
		assertEquals((Integer)7, detachID1.getTableArg());
		assertEquals(id, detachID1.getId().longValue());
		
		idParse = factory.createIDParse(configFactory.newInstance("gid2"));
		DetachID<Long, Integer, Integer> detachID2 = idParse.parse(10000220118L);
		assertEquals((Integer)22, detachID2.getDatabaseArg());
		assertEquals((Integer)118, detachID2.getTableArg());
	}

}
