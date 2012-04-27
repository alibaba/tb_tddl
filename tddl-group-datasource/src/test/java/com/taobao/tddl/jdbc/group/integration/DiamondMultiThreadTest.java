/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.integration;

import org.junit.Test;

import com.taobao.diamond.manager.DiamondManager;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.manager.impl.DefaultDiamondManager;
import com.taobao.tddl.jdbc.group.TGroupDataSource;

public class DiamondMultiThreadTest {
	public static final String appName = "unitTest";

	public static class MyThread extends Thread {
		DiamondManager diamondManager;

		MyThread(String dataId, int index) {
			super("MyThread-" + index);
			diamondManager = new DefaultDiamondManager(dataId, (ManagerListener) null);
			//System.out.println(this + " diamondManager = "+diamondManager);
		}

		public void run() {
			if(ds!=null) {
				ds.init();
				return;
			}
			//System.out.println(this + " is running");

			String configInfo = diamondManager.getConfigureInfomation(0);
			System.out.println(this + " get configInfo = " + configInfo);

			//System.out.println(this + " ended");
		}
		
		TGroupDataSource ds;

		MyThread(TGroupDataSource ds) {
			this.ds = ds;
		}
	}

	@Test
	public void 并发执行多个线程_不涉及TAtomDataSource() throws Exception {
		int size = 6;
		MyThread[] myThreads = new MyThread[size];
		for (int i = 1; i <= size; i++) {
			MyThread t = new MyThread("DiamondMultiThreadTest" + i, i);
			t.start();
			
			myThreads[i-1] = t;
		}
		
		for (int i = 0; i < size; i++) {
			myThreads[i].join();
		}
	}
	
	@Test
	public void 串行执行多个线程_不涉及TAtomDataSource() throws Exception {
		int size = 6;
		for (int i = 1; i <= size; i++) {
			MyThread t = new MyThread("DiamondMultiThreadTest" + i, i);
			t.start();
			t.join();
		}
	}
	
	@Test
	public void 并发执行多个线程_涉及TAtomDataSource() throws Exception {
		int size = 3;
		MyThread[] myThreads = new MyThread[size];
		for (int i = 1; i <= size; i++) {

			MyThread t = new MyThread(new TGroupDataSource("myDbGroupKey" + i, appName));
			t.start();
			myThreads[i-1] = t;
		}
		
		for (int i = 1; i <= size; i++) {
			myThreads[i-1].join();
		}
	}
	
	@Test
	public void 串行执行多个线程_涉及TAtomDataSource() throws Exception {
		int size = 3;
		for (int i = 1; i <= size; i++) {
			MyThread t = new MyThread(new TGroupDataSource("myDbGroupKey" + i, appName));
			t.start();
			t.join();
		}
	}
}
