/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.monitor;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author junyu
 *
 */
public class CycleExecuteTimerTest {
	CycleExecuteTimer cet;
	private int i=1;
	private static int count = 0;
	public void testStart(){
		cet=new CycleExecuteTimer("number", new Runnable() {
			public void run() {
				count++;
			}
		},1000, TimeUnit.MILLISECONDS,null);
		
		cet.start();
		
		System.out.println("说明等待线程没有影响");
	}
	
	public void testStop(){
		cet.stop();
	}
	
	@Test
	public void testCountBiggerThan(){
		CycleExecuteTimerTest cycle=new CycleExecuteTimerTest();
		cycle.testStart();
		try {
			Thread.sleep(10*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cycle.testStop();
		Assert.assertTrue(""+count,count > 9);
	}
}
