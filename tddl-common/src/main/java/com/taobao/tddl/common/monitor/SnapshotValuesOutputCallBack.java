/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.taobao.tddl.common.monitor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * 一些静态值的处理，静态值不符合累加模型，因此在输出的时候回调这个接口加入到old里面输出
 * 
 * @author shenxun
 * @author junyu
 */
public interface SnapshotValuesOutputCallBack {
	public static class Values {
		public final AtomicLong value2 = new AtomicLong(0L);
		public final AtomicLong value1 = new AtomicLong(0L);

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			long v1 = value1.get();
			long v2 = value2.get();

			sb.append("[").append(v1).append(":").append(v2).append("]");
			return sb.toString();
		}
	}

	public static class Key {
		public static final String replicationQueueSize = "_replicationQueueSize";
		public static final String replicationPoolSize = "_replicationPoolSize";
		public static final String parserCacheSize = "_parserCacheSize";

		public static final String THREAD_COUNT = "THREAD_COUNT";
		public static final String THREAD_COUNT_REJECT_COUNT = "THREAD_COUNT_REJECT_COUNT";
		public static final String READ_WRITE_TIMES = "READ_WRITE_TIMES";
		public static final String READ_WRITE_TIMES_REJECT_COUNT = "READ_WRITE_TIMES_REJECT_COUNT";
		public static final String READ_WRITE_CONCURRENT = "READ_WRITE_CONCURRENT";
	}

	/**
	 * 当前的统计内容汇总：
	 * 
	 * @see TDataSourceState TDataSourceWrapper
	 */
	ConcurrentHashMap<String, Values> getValues();
}
