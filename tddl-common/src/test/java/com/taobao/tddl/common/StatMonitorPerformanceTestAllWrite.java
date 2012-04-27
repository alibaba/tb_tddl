/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class StatMonitorPerformanceTestAllWrite {

	private static final long TIME = 10000L;
	
	public static void main(String[] args) {
		System.out.println("StatMonitor:");
		
		test(1, 10);
		test(10, 1);
		test(3, 3);
		
		test(1, 50);
		test(50, 1);
		test(5, 10);
		test(10, 5);
		
		test(1, 100);
		test(100, 1);
		test(5, 20);
		test(20, 5);
		test(9, 9);
		
		System.out.println();
		System.out.println("ConcurrentHashMap:");
		
		test2(1, 10);
		test2(10, 1);
		test2(3, 3);
		
		test2(1, 50);
		test2(50, 1);
		test2(5, 10);
		test2(10, 5);
		
		test2(1, 100);
		test2(100, 1);
		test2(5, 20);
		test2(20, 5);
		test2(9, 9);
	}
	
	private static void test(final int entities, final int concurrent) {
		long start = System.currentTimeMillis();
		
		final StatMonitor map = StatMonitor.getInstance();

		final AtomicBoolean stop = new AtomicBoolean(false);
		final AtomicLong writeCount = new AtomicLong(0);
		Thread[] threads = new Thread[concurrent];
		for (int j = 0; j < concurrent; j++) {
			threads[j] = new Thread() {
				@Override
				public void run() {
					while (!stop.get()) {
						for (int i = 0; i < entities; i++) {
							final String key = "key" + i+"_";
							final long value = i;
							map.addStat(key+"1", key+"2", key+"3", value);
							writeCount.incrementAndGet();
						}
					}
				}
			};
			threads[j].start();
		}

		try {
			Thread.sleep(TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		stop.set(true);
		
		long time = System.currentTimeMillis() - start;
		System.out.println("entities: "+entities+", concurrent: "+concurrent+"; time: "+time+", writeCount: "+writeCount
				+", ms/per write: "+(double)time / writeCount.get());
	}
	
	private static void test2(final int entities, final int concurrent) {
		long start = System.currentTimeMillis();

		final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();

		final AtomicBoolean stop = new AtomicBoolean(false);
		final AtomicLong writeCount = new AtomicLong(0);
		Thread[] threads = new Thread[concurrent];
		for (int j = 0; j < concurrent; j++) {
			threads[j] = new Thread() {
				@Override
				public void run() {
					while (!stop.get()) {
						for (int i = 0; i < entities; i++) {
							final String value = "value" + i;
							final String key = "key" + i;
							map.put(key, value);
							writeCount.incrementAndGet();
						}
					}
				}
			};
			threads[j].start();
		}

		try {
			Thread.sleep(TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		stop.set(true);
		
		long time = System.currentTimeMillis() - start;
		System.out.println("entities: "+entities+", concurrent: "+concurrent+"; time: "+time+", writeCount: "+writeCount
				+", ms/per write: "+(double)time / writeCount.get());
	}


}
