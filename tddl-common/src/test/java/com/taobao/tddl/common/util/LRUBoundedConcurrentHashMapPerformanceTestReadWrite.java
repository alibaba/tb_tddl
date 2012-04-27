/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//package com.taobao.tddl.common.util;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicLong;
//
//import org.junit.Test;
//
//public class LRUBoundedConcurrentHashMapPerformanceTestReadWrite {
//
//	private static final long TIME = 10000L;
//	
//    @Test
//    public void test0(){
//    }
//
//	public static void main(String[] args) {
//		System.out.println("LRUConcurrentHashMap:");
//		
//		test(1, 1, 10);
//		test(1, 2, 5);
//		test(1, 3, 3);
//		test(1, 5, 2);
//		test(1, 10, 1);
//		
//		test(10, 1, 10);
//		test(10, 2, 5);
//		test(10, 3, 3);
//		test(10, 5, 2);
//		test(10, 10, 1);
//		
//		test(20, 1, 10);
//		test(20, 2, 5);
//		test(20, 3, 3);
//		test(20, 5, 2);
//		test(20, 10, 1);
//		
//		System.out.println();
//		System.out.println("ConcurrentHashMap:");
//		
//		test2(1, 1, 10);
//		test2(1, 2, 5);
//		test2(1, 3, 3);
//		test2(1, 5, 2);
//		test2(1, 10, 1);
//		
//		test2(10, 1, 10);
//		test2(10, 2, 5);
//		test2(10, 3, 3);
//		test2(10, 5, 2);
//		test2(10, 10, 1);
//		
//		test2(20, 1, 10);
//		test2(20, 2, 5);
//		test2(20, 3, 3);
//		test2(20, 5, 2);
//		test2(20, 10, 1);
//	}
//	
//	private static void test(final int entities, final int reads, final int writes) {
//		long start = System.currentTimeMillis();
//		
//		final LRUConcurrentHashMap<String, String> map = new LRUConcurrentHashMap<String, String>();
//		for(int i = 0; i < entities; i++) {
//			map.putIfAbsent("key" + i, "value" + i);
//		}
//
//		final AtomicBoolean stop = new AtomicBoolean(false);
//		final AtomicLong accessCount = new AtomicLong(0);
//		Thread[][] readThreads = new Thread[entities][reads];
//		Thread[][] writeThreads = new Thread[entities][writes];
//		
//		
//		for (int i = 0; i < entities; i++) {
//			final String key = "key" + i;
//			
//			for (int j = 0; j < reads; j++) {
//				readThreads[i][j] = new Thread() {
//					@Override
//					public void run() {
//						while (!stop.get()) {
//							map.get(key);
//							accessCount.incrementAndGet();
//						}
//					}
//				};
//				readThreads[i][j].start();
//			}
//			
//			for (int j = 0; j < writes; j++) {
//				final String value = "value"+i+j;
//				writeThreads[i][j] = new Thread() {
//					@Override
//					public void run() {
//						while (!stop.get()) {
//							map.putIfAbsent(key, value);
//							accessCount.incrementAndGet();
//						}
//					}
//				};
//				writeThreads[i][j].start();
//			}
//		}
//
//		try {
//			Thread.sleep(TIME);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		stop.set(true);
//		
//		long time = System.currentTimeMillis() - start;
//		System.out.println("entities: "+entities+", reads: "+reads+", writes: "+writes+"; time: "+time+", accessCount: "+accessCount
//				+", ms/per access: "+(double)time / accessCount.get());
//	}
//	
//	private static void test2(final int entities, final int reads, final int writes) {
//		long start = System.currentTimeMillis();
//		
//		final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();
//		for(int i = 0; i < entities; i++) {
//			map.put("key" + i, "value" + i);
//		}
//
//		final AtomicBoolean stop = new AtomicBoolean(false);
//		final AtomicLong accessCount = new AtomicLong(0);
//		Thread[][] readThreads = new Thread[entities][reads];
//		Thread[][] writeThreads = new Thread[entities][writes];
//		
//		
//		for (int i = 0; i < entities; i++) {
//			final String key = "key" + i;
//			
//			for (int j = 0; j < reads; j++) {
//				readThreads[i][j] = new Thread() {
//					@Override
//					public void run() {
//						while (!stop.get()) {
//							map.get(key);
//							accessCount.incrementAndGet();
//						}
//					}
//				};
//				readThreads[i][j].start();
//			}
//			
//			for (int j = 0; j < writes; j++) {
//				final String value = "value"+i+j;
//				writeThreads[i][j] = new Thread() {
//					@Override
//					public void run() {
//						while (!stop.get()) {
//							map.put(key, value);
//							accessCount.incrementAndGet();
//						}
//					}
//				};
//				writeThreads[i][j].start();
//			}
//		}
//
//		try {
//			Thread.sleep(TIME);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		stop.set(true);
//		
//		long time = System.currentTimeMillis() - start;
//		System.out.println("entities: "+entities+", reads: "+reads+", writes: "+writes+"; time: "+time+", accessCount: "+accessCount
//				+", ms/per access: "+(double)time / accessCount.get());
//	}
//	
//}
