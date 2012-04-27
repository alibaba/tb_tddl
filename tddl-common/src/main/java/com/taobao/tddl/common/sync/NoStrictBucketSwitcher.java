/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BucketSwitcher实现
 * 
 * 性能优先的非精确实现
 * 
 * @author guangxia
 *
 * @param <T>
 */
public class NoStrictBucketSwitcher<T> implements BucketSwitcher<T> {
	private volatile int bucketSize;
	private final Lock full = new ReentrantLock();
	private final BucketTaker<T> bucketTaker;
	private volatile boolean taked;
	private final Thread checker;
	private volatile long checker_time = 5 * 60 * 1000 / 2;
	
	private static class Bucket<T> {
		final T[] array;
		final AtomicInteger index;
		final AtomicInteger size;
		@SuppressWarnings("unchecked")
		Bucket(int size) {
			this.array = (T[])new Object[size];
			this.index = new AtomicInteger(0);
			this.size = new AtomicInteger(0);
		}
	}
	private volatile Bucket<T> bucket;

	public NoStrictBucketSwitcher(BucketTaker<T> taker) {
		this(taker, 64);
	}

	public NoStrictBucketSwitcher(BucketTaker<T> taker, int bucketSize) {
		this.bucketTaker = taker;
		this.bucketSize = bucketSize;
		bucket = new Bucket<T>(bucketSize);
		
		checker = new Thread() {
			@Override
			public void run() {
				while(true) {
					if(!taked) {
						Collection<T> fulledBucket = null;
						full.lock();
						try {
							Bucket<T> local_bucket = bucket;
							if(local_bucket.index.get() != 0) {
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
						sleep(checker_time);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		};
		checker.start();
	}

	public void pourin(T task) {
		while(true) {
			Bucket<T> local_bucket = bucket;
			int next = local_bucket.index.getAndIncrement();
			if(next < local_bucket.array.length) {
				//这里的间隙将导致检查线程丢数据和porin线程在满的情况下自旋
				local_bucket.array[next] = task;
				local_bucket.size.incrementAndGet();
				return;
			}
			//处理满掉的情况
			Collection<T> fulledBucket = null;
			this.full.lock();
			try {
				local_bucket = bucket;
				if (local_bucket.size.get() == local_bucket.array.length) {
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
	
	@SuppressWarnings("unchecked")
	protected Collection<T> takeAway() {
		Bucket<T> oldBucket = bucket;
		int size = oldBucket.index.get();
		//失败的情况下，依然会导致indexInBucket自增
		if(size > oldBucket.array.length) {
			size = oldBucket.array.length;
		}
		ArrayList<T> fulledBucket = new ArrayList<T>(size);
		for(int i = 0; i < size; i++) {
			T tmp_Item = oldBucket.array[i];
			if(tmp_Item != null) {
				fulledBucket.add(tmp_Item);
			}
		}
		bucket = new Bucket(bucketSize);
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

	public synchronized void setChecker_time(long checker_time) {
		this.checker_time = checker_time;
	}

	public long getChecker_time() {
		return checker_time;
	}

	public Thread getChecker() {
		return checker;
	}
}
