/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.integration;

import java.sql.Connection;
import java.sql.Statement;

import org.junit.Test;

import com.taobao.tddl.interact.rule.bean.DBType;
import com.taobao.tddl.jdbc.group.DataSourceWrapper;
import com.taobao.tddl.jdbc.group.TGroupDataSource;
import com.taobao.tddl.jdbc.group.testutil.DataSourceFactory;

public class EquityDbManagerTryLockTest {
	static TGroupDataSource ds = new TGroupDataSource();

	public static final String appName = "unitTest";
	public static final String dbGroupKey = "EquityDbManagerTryLockTest";

	static TGroupDataSource ds2 = new TGroupDataSource(dbGroupKey, appName);

	public class MyThread extends Thread {
		int count = 10;
		Connection conn;

		MyThread(int index) throws Exception {
			super("MyThread-" + index);
			conn = ds.getConnection();
		}

		public void run() {
			while (count > 0) {
				System.out.println(this + " count=" + count);
				try {
					Statement stmt = conn.createStatement();
					stmt.executeQuery("select f1,f2 from crud");
					stmt.close();
					Thread.sleep(2000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				count--;
			}
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void test() {
	}
	
	//有一个数据库"ds1"在我本机上，不能在hudson上跑，因为我晚上关电脑
	//@Test
	public void tryLockTest() throws Exception {
		DataSourceWrapper dsw1 = new DataSourceWrapper("ds1", "R20W", DataSourceFactory.getLocalMySQLDataSource(),
				DBType.MYSQL);
		DataSourceWrapper dsw2 = new DataSourceWrapper("ds2", "R10", DataSourceFactory.getMySQLDataSource(),
				DBType.MYSQL);

		ds.init(dsw1, dsw2);

		int size = 3;
		MyThread[] myThreads = new MyThread[size];
		for (int i = 1; i <= size; i++) {

			MyThread t = new MyThread(i);
			t.start();
			myThreads[i - 1] = t;
		}

		for (int i = 1; i <= size; i++) {
			myThreads[i - 1].join();
		}
	}
	
	//@Test
	public void tryLockTest2() throws Exception {
		ds = ds2;
		ds.init();

		int size = 200;
		MyThread[] myThreads = new MyThread[size];
		for (int i = 1; i <= size; i++) {

			MyThread t = new MyThread(i);
			t.start();
			myThreads[i - 1] = t;
		}

		for (int i = 1; i <= size; i++) {
			myThreads[i - 1].join();
		}
	}
}
