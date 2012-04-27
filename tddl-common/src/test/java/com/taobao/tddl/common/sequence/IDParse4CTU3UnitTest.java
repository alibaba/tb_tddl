/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sequence;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.tddl.common.sequence.IDParse.DetachID;

public class IDParse4CTU3UnitTest {
	
	private Config.Factory configFactory;
	private IDParse<Long, Integer, Integer> idParse;
	private DetachID<Long, Integer, Integer> detachID;

	@Before
	public void setUp() throws Exception {
		configFactory = new Config.Factory("/generators4ctu3.xml");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParse() {
		Long id = 1234567L;
		//Mon Aug 10 00:00:00 CST 2009
		long time = 1249833600000L - IDParse4CTU3.Year2000Time;
		long rawId = time * 10000000 + id;
		
		idParse = new IDParse4CTU3(configFactory.newInstance("gid1"));
		detachID = idParse.parse(rawId);
		assertEquals((Integer)7, detachID.getTableArg());
		assertEquals(id, detachID.getId());
		
		idParse = new IDParse4CTU3(configFactory.newInstance("gid2"));
		detachID = idParse.parse(rawId);
		assertEquals((Integer)2, detachID.getTableArg());
		assertEquals(id, detachID.getId());
	}

}
