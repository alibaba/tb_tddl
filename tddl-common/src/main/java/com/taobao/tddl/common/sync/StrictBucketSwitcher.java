/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BucketSwitcher实现
 * 
 * 精确实现：
 * 由于用ArrayBlockingQueue实现，高并发下pourin不会丢失数据
 *   
 * @author guangxia
 *
 * @param <T>
 */
public class StrictBucketSwitcher<T> implements BucketSwitcher<T> {

	private volatile int bucketSize;
	private volatile LinkedBlockingQueue<T> bucket;
	private final Lock full = new ReentrantLock();
	private final BucketTaker<T> bucketTaker;
	private volatile boolean taked;
	private Thread checker;
	private volatile long checkerTime = 5 * 60 * 1000 / 2;

	public StrictBucketSwitcher(BucketTaker<T> taker) {
		this(taker, 64);
	}

	public StrictBucketSwitcher(BucketTaker<T> taker, int bucketSize) {
		this.bucketTaker = taker;
		this.bucketSize = bucketSize;
		bucket = new LinkedBlockingQueue<T>();
		
		checker = new Thread() {
			@Override
			public void run() {
				while(true) {
					if(!taked) {
						Collection<T> fulledBucket = null;
						full.lock();
						try {
							if(bucket.size() != 0) {
								fulledBucket = takeAway();
							}
						} finally {
							full.unlock();
						}
						if (fulledBucket != null) {
							bucketTaker.takeAway(fulledBucket);
						}
					}
					taked = false;
					try {
						sleep(checkerTime);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		};
		checker.start();
	}

	public void pourin(T task) {
		try {
			bucket.put(task);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		if (bucket.size() >= this.bucketSize) {
			Collection<T> fulledBucket = null;
			this.full.lock();
			try {
				if (bucket.size() >= this.bucketSize) {
					fulledBucket = takeAway();
				}
			} finally {
				this.full.unlock();
			}
			if (fulledBucket != null) {
				bucketTaker.takeAway(fulledBucket);
			}
		}
	}
	
	/**如果当前队列里有任务，则抢占并放到自己队列里
	 * 
	 * @return
	 */
	protected Collection<T> takeAway() {
		int size = bucketSize;
		LinkedBlockingQueue<T> local_bucket = bucket;
		Collection<T> fulledBucket = new ArrayList<T>(size);
		for(int i = 0; i < size; i++) {
			T t = local_bucket.poll();
			if(t != null) {
				fulledBucket.add(t);
			}
		}
		taked = true;
		return fulledBucket;
	}

	/**
	 * 支持动态监控和调整bucketSize
	 */
	public int getBucketSize() {
		return bucketSize;
	}

	public synchronized void setBucketSize(int bucketSize) {
		this.bucketSize = bucketSize;
	}

	public synchronized void setCheckerTime(long checkerTime) {
		this.checkerTime = checkerTime;
	}

	public long getCheckerTime() {
		return checkerTime;
	}
}
