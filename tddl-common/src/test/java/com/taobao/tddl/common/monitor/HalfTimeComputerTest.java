/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.monitor;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;



public class HalfTimeComputerTest {
	@Test
	public void test_getMostNearTime_29分(){
		HalfTimeComputer halfTimeComputer = new HalfTimeComputer();
		Calendar cal = Calendar.getInstance();
		cal.set(2010, 1, 2,12,29,0);
		Date  date = halfTimeComputer.getMostNearTime(cal);
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date);
		Assert.assertEquals(30, cal1.get(Calendar.MINUTE));
		Assert.assertEquals(12, cal1.get(Calendar.HOUR_OF_DAY));
	}
	@Test
	public void test_getMostNearTime_31分(){
		HalfTimeComputer halfTimeComputer = new HalfTimeComputer();
		Calendar cal = Calendar.getInstance();
		cal.set(2010, 1, 2,12,31,0);
		Date  date = halfTimeComputer.getMostNearTime(cal);
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date);

		Assert.assertEquals(13, cal1.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(0, cal1.get(Calendar.MINUTE));
	}
	@Test
	public void test_getMostNearTimeInterval_29分(){
		HalfTimeComputer halfTimeComputer = new HalfTimeComputer();
		Calendar cal = Calendar.getInstance();
		cal.set(2010, 1, 2,12,59,0);
		long  date = halfTimeComputer.getMostNearTimeInterval(cal);
		Assert.assertEquals(60000,date);
	}
	@Test
	public void test_getMostNearTimeInterval_31分(){
		HalfTimeComputer halfTimeComputer = new HalfTimeComputer();
		Calendar cal = Calendar.getInstance();
		cal.set(2010, 1, 2,23,31,0);
		long  date = halfTimeComputer.getMostNearTimeInterval(cal);
		Assert.assertEquals(1740000,date);
	}
	
	@Test
	public void test_getMostNearTimeInterval_30分(){
		HalfTimeComputer halfTimeComputer = new HalfTimeComputer();
		Calendar cal = Calendar.getInstance();
		cal.set(2010, 1, 2,23,30,0);
		long  date = halfTimeComputer.getMostNearTimeInterval(cal);
		Assert.assertEquals(1800000,date);
	}
	
	@Test
	public void test_getMostNearTimeInterval_0分(){
		HalfTimeComputer halfTimeComputer = new HalfTimeComputer();
		Calendar cal = Calendar.getInstance();
		cal.set(2010, 1, 2,0,0,0);
		long  date = halfTimeComputer.getMostNearTimeInterval(cal);
		Assert.assertEquals(1800000,date);
	}
}
