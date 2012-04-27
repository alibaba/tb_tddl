/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common;

//import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

//import com.taobao.tddl.common.util.BoundedConcurrentHashMap;

public class StatMonitorPerformanceTestAllRead {

	private static final long TIME = 100000L;
	
	public static void main(String[] args) {
		System.out.println("StatMonitor:");
		
//		test(1, 10);
//		test(10, 1);
//		test(3, 3);
//		
//		test(1, 50);
//		test(50, 1);
//		test(5, 10);
//		test(10, 5);
//		
//		test(1, 100);
//		test(100, 1);
//		test(5, 20);
//		test(20, 5);
//		test(9, 9);
		test(1100, 16);
		
//		System.out.println();
//		System.out.println("BoundedConcurrentHashMap:");
//		test1(1000, 16);
//		
//		System.out.println();
//		System.out.println("ConcurrentHashMap:");
		
//		test2(1, 10);
//		test2(10, 1);
//		test2(3, 3);
//		
//		test2(1, 50);
//		test2(50, 1);
//		test2(5, 10);
//		test2(10, 5);
//		
//		test2(1, 100);
//		test2(100, 1);
//		test2(5, 20);
//		test2(20, 5);
//		test2(9, 9);
		
//		test2(1000, 16);

	}
	
	private static void test(final int entities, final int concurrent) {
		long start = System.currentTimeMillis();
		
		final StatMonitor monitor = StatMonitor.getInstance();
		for(int i = 0; i < entities; i++) {
			monitor.addStat("key" + i, "key" + i, "key" + i, i);
		}

		final AtomicBoolean stop = new AtomicBoolean(false);
		final AtomicLong readCount = new AtomicLong(0);
		final AtomicLong errorCount = new AtomicLong(0);
		Thread[] threads = new Thread[concurrent];
		for (int j = 0; j < concurrent; j++) {
			threads[j] = new Thread() {
				@Override
				public void run() {
					while (!stop.get()) {
						for (int i = 0; i < entities; i++) {
							final String key = "key" + i;
							monitor.getStatResult(key, key, key);
							readCount.incrementAndGet();
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
		System.out.println("entities: "+entities+", concurrent: "+concurrent+"; time: "+time+", readCount: "+readCount
				+", ms/per read: "+(double)time / readCount.get()+", error count :" +errorCount);
	}
	/*
	private static void test1(final int entities, final int concurrent) {
		long start = System.currentTimeMillis();
		
		final BoundedConcurrentHashMap<String, String> map = new BoundedConcurrentHashMap<String, String>(1010);
		for(int i = 0; i < entities; i++) {
			map.put("key" + i, "value" + i);
		}

		final AtomicBoolean stop = new AtomicBoolean(false);
		final AtomicLong readCount = new AtomicLong(0);
		Thread[] threads = new Thread[concurrent];
		for (int j = 0; j < concurrent; j++) {
			threads[j] = new Thread() {
				@Override
				public void run() {
					while (!stop.get()) {
						for (int i = 0; i < entities; i++) {
							final String key = "key" + i;
							if(map.get(key) == null){
								System.out.println("error "+i );
							}
							readCount.incrementAndGet();
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
		System.out.println("entities: "+entities+", concurrent: "+concurrent+"; time: "+time+", readCount: "+readCount
				+", ms/per read: "+(double)time / readCount.get());
	}

	
	private static void test2(final int entities, final int concurrent) {
		long start = System.currentTimeMillis();
		
		final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>(1010);
		for(int i = 0; i < entities; i++) {
			map.put("key" + i, "value" + i);
		}

		final AtomicBoolean stop = new AtomicBoolean(false);
		final AtomicLong readCount = new AtomicLong(0);
		Thread[] threads = new Thread[concurrent];
		for (int j = 0; j < concurrent; j++) {
			threads[j] = new Thread() {
				@Override
				public void run() {
					while (!stop.get()) {
						for (int i = 0; i < entities; i++) {
							final String key = "key" + i;
							if(map.get(key) == null){
								System.out.println("error "+i );
							}
							readCount.incrementAndGet();
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
		System.out.println("entities: "+entities+", concurrent: "+concurrent+"; time: "+time+", readCount: "+readCount
				+", ms/per read: "+(double)time / readCount.get());
	}*/


}
