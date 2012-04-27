/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sync;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

/**
 * 生产者消费者模式之：注水接水模式，零存整取模式，积累批量模式
 * 水管里不断有水流出，连续或时断时续。
 * 接水者等一桶水满了后，才取走使用
 * 
 * @author linxuan
 *
 * @param <T>
 */
public interface BucketSwitcher<T> {
	/**
	 * 注水者注入水。
	 * 通过该方法不断地加入任务、对象。。。
	 * 就像水管里不断有水流到水桶里。
	 * 水桶满了，自动切换。注水者(用户的一个或多个线程)不必关心水管流到的是那个桶
	 */
	void pourin(T task);

	/**
	 * 设置接水者。
	 * 如果设置了接水者，一通水满后会自动移走，并拿给接水者。
	 */
	abstract class BucketTaker<T> {
		private final ExecutorService executor;
		
		public BucketTaker(ExecutorService executor) {
			this.executor = executor;
		}
		
		public abstract Runnable createTakeAwayTask(Collection<T> list);
		
		public void takeAway(Collection<T> list) {
			executor.execute(createTakeAwayTask(list));
		}
	}
}
