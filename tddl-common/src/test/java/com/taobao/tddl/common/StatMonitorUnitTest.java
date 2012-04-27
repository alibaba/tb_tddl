/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 * @author guangxia
 * @since 1.0, 2010-2-12 ÉÏÎç11:10:32
 */
public class StatMonitorUnitTest {
	
	private StatMonitor monitor;
	private long startTime;
	
	@Before
	public void setUp() throws Exception {
		monitor = StatMonitor.getInstance();
		startTime = System.currentTimeMillis();		
	}

	@After
	public void tearDown() throws Exception {
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetStatResultStringStringString() {
		Set<String> blackList = new TreeSet<String>();
		String keyBlack = "keyBlack"; 
		blackList.add(keyBlack);
		System.out.println(blackList);
		monitor.setBlackList(blackList);
		assertTrue(monitor.getBlackList() instanceof HashSet);
		assertEquals(1, monitor.getBlackList().size());
		assertTrue(monitor.getBlackList().contains(keyBlack));
		assertFalse(monitor.addStat(keyBlack, "keyTwo", "keyThree"));
		
		monitor.setLimit(2);
		
		monitor.addStat("keyOne", "keyTwo", "keyThree0", 3);
		monitor.addStat("keyOne", "keyTwo", "keyThree");
		monitor.addStat("keyOne", "keyTwo", "keyThree", 2);
		monitor.addStat("keyOne", "keyTwo", "keyThree", 3);
		monitor.addStat("keyOne", "keyTwo", "keyThree", 5);
		monitor.addStat("keyOne", "keyTwo", "keyThree", 7);
		System.out.println(monitor.getStatResult("keyOne", "keyTwo", "keyThree"));
		assertTrue(monitor.getStatResult("keyOne", "keyTwo", "keyThree").startsWith("count: 5, value: 17, average: 3.4,"));
		System.out.println("duration: "+(System.currentTimeMillis() - startTime));
		
		assertFalse(monitor.addStat("keyOne", "keyTwo", "keyThree1", 8));
		assertTrue(monitor.addStat("keyOne", "keyTwo", "keyThree", 7));
		
		monitor.resetStat();
		System.out.println(monitor.getStatResult("keyOne", "keyTwo", "keyThree"));
		assertTrue(monitor.getStatResult("keyOne", "keyTwo", "keyThree").startsWith("count: 6, value: 24, average: 4.0,"));
		
		assertTrue(monitor.addStat("keyOne", "keyTwo", "keyThree", 11));
		monitor.addStat("keyOne", "keyTwo", "keyThree", 13);
		System.out.println(monitor.getStatResult("keyOne", "keyTwo", "keyThree"));
		assertTrue(monitor.getStatResult("keyOne", "keyTwo", "keyThree").startsWith("count: 6, value: 24, average: 4.0,"));
		assertTrue(monitor.addStat("keyOne", "keyTwo", "keyThree1", 11));
		assertFalse(monitor.addStat("keyOne", "keyTwo", "keyThree2", 11));
		
		monitor.resetStat();
		System.out.println(monitor.getStatResult("keyOne", "keyTwo", "keyThree"));
		assertTrue(monitor.getStatResult("keyOne", "keyTwo", "keyThree").startsWith("count: 2, value: 24, average: 12.0,"));
		assertTrue(monitor.addStat("keyOne", "keyTwo", "keyThree0", 11));
		assertTrue(monitor.addStat("keyOne", "keyTwo", "keyThree1", 11));
		assertFalse(monitor.addStat("keyOne", "keyTwo", "keyThree2", 11));
		
		monitor.setLimit(1000);
		monitor.resetStat();
		monitor.addStat("keyOne0", "keyTwo0", "keyThree0", 1);
		monitor.addStat("keyOne0", "keyTwo0", "keyThree1", 2);
		monitor.addStat("keyOne0", "keyTwo0", "keyThree2", 3);
		monitor.addStat("keyOne0", "keyTwo1", "keyThree0", 4);
		monitor.addStat("keyOne0", "keyTwo2", "keyThree0", 5);
		monitor.addStat("keyOne0", "keyTwo0", "keyThree0", 6);
		monitor.addStat("keyOne1", "keyTwo0", "keyThree0", 7);
		monitor.addStat("keyOne2", "keyTwo0", "keyThree0", 8);
		monitor.resetStat();
		System.out.println(monitor);
		assertEquals(7, monitor.getSortedSetByCount("*", "*", "*").size());
		assertEquals(5, monitor.getSortedSetByCount("*", "*", "keyThree0").size());
		assertEquals(1, monitor.getSortedSetByCount("*", "*", "keyThree1").size());
		assertEquals(5, monitor.getSortedSetByCount("*", "keyTwo0", "*").size());
		assertEquals(1, monitor.getSortedSetByCount("*", "keyTwo1", "*").size());
		assertEquals(5, monitor.getSortedSetByCount("keyOne0", "*", "*").size());
		assertEquals(1, monitor.getSortedSetByCount("keyOne1", "*", "*").size());
		assertEquals(3, monitor.getSortedSetByCount("*", "keyTwo0", "keyThree0").size());
		assertEquals(1, monitor.getSortedSetByCount("*", "keyTwo0", "keyThree1").size());
		assertEquals(1, monitor.getSortedSetByCount("*", "keyTwo1", "keyThree0").size());
		assertEquals(0, monitor.getSortedSetByCount("*", "keyTwo1", "keyThree1").size());
		assertEquals(3, monitor.getSortedSetByCount("keyOne0", "keyTwo0", "*").size());
		assertEquals(1, monitor.getSortedSetByCount("keyOne1", "keyTwo0", "*").size());
		assertEquals(1, monitor.getSortedSetByCount("keyOne0", "keyTwo1", "*").size());
		assertEquals(0, monitor.getSortedSetByCount("keyOne1", "keyTwo1", "*").size());
		assertEquals(3, monitor.getSortedSetByCount("keyOne0", "*", "keyThree0").size());
		assertEquals(1, monitor.getSortedSetByCount("keyOne0", "*", "keyThree1").size());
		assertEquals(1, monitor.getSortedSetByCount("keyOne1", "*", "keyThree0").size());
		assertEquals(0, monitor.getSortedSetByCount("keyOne1", "*", "keyThree1").size());
		assertEquals("[{(keyOne0, keyTwo0, keyThree0), count: 2, value: 7}]", monitor.getSortedSetByCount("keyOne0", "keyTwo0", "keyThree0").toString());
		assertEquals("[{(keyOne0, keyTwo1, keyThree0), count: 1, value: 4}]", monitor.getSortedSetByCount("keyOne0", "keyTwo1", "keyThree0").toString());
		assertEquals("[]", monitor.getSortedSetByCount("keyOne0", "keyTwo1", "keyThree1").toString());
		assertEquals("[{(keyOne1, keyTwo0, keyThree0), count: 1, value: 7}]", monitor.getSortedSetByCount("keyOne1", "keyTwo0", "keyThree0").toString());
		assertEquals("[]", monitor.getSortedSetByCount("keyOne1", "keyTwo0", "keyThree1").toString());
		assertEquals("[]", monitor.getSortedSetByCount("keyOne1", "keyTwo1", "keyThree1").toString());
		System.out.println(monitor.getSortedSetByCount("*", "*", "*"));
		System.out.println(monitor.getSortedSetByValue("*", "*", "*"));
	}
	
}
