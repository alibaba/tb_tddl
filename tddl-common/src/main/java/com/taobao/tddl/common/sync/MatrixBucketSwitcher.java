/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sync;

import com.taobao.tddl.common.sync.BucketSwitcher.BucketTaker;

/**
 * 
 * 性能优先的非精确实现
 *   
 * @author guangxia
 *
 * @param <T>
 */
public class MatrixBucketSwitcher<T> {
	private final BucketTaker<T> bucketTaker;
	private volatile int bucketSize;
	private static class Buckets<T> {
		final int height;
		final int width;
		final NoStrictBucketSwitcher<T>[][] bucketSwitchers;
		
		@SuppressWarnings("unchecked")
		Buckets(BucketTaker<T> bucketTaker, int bucketSize, int height, int width) {
			this.height = height;
			this.width = width;
			this.bucketSwitchers = new NoStrictBucketSwitcher[height][]; 
			for(int i = 0; i < height; i++) {
				bucketSwitchers[i] = new NoStrictBucketSwitcher[width];
				for(int j = 0; j < width; j++) {
					bucketSwitchers[i][j] = new NoStrictBucketSwitcher<T>(bucketTaker, bucketSize);
				}
			}
		}
		
		Buckets(Buckets<T> oldBuckets, BucketTaker<T> bucketTaker, int bucketSize, int height, int width) {
			this(bucketTaker, bucketSize, height, width);
			NoStrictBucketSwitcher<T>[][] oldBucketSwitchers = oldBuckets.bucketSwitchers;
			for(int i = 0; i < oldBucketSwitchers.length; i++) {
				for(int j = 0; j < oldBucketSwitchers[i].length; j++) {
					oldBucketSwitchers[i][i].getChecker().interrupt();
				}
			}
		}
	}
	private volatile Buckets<T> buckets;
	private volatile long checker_time = 5 * 60 * 1000 / 2;

	public MatrixBucketSwitcher(BucketTaker<T> taker, int bucketSize, int height, int width) {
		this.bucketTaker = taker;
		this.bucketSize = bucketSize;
		this.buckets = new Buckets<T>(bucketTaker, bucketSize, height, width);
	}
		
	public MatrixBucketSwitcher(BucketTaker<T> taker, int bucketSize, int height) {
		this(taker, bucketSize, height, 8);
	}

	public MatrixBucketSwitcher(BucketTaker<T> taker, int bucketSize) {
		this(taker, bucketSize, 4, 8);
	}
	
	public MatrixBucketSwitcher(BucketTaker<T> taker) {
		this(taker, 64, 4, 8);
	}
	
	public void pourin(T task, int m, int n) {
		buckets.bucketSwitchers[m][n].pourin(task);
	}

	/**
	 * 支持动态监控和调整bucketSize
	 */
	public int getBucketSize() {
		return bucketSize;
	}

	public void setBucketSize(int bucketSize) {
		this.bucketSize = bucketSize;
	}

	public void setChecker_time(long checker_time) {
		this.checker_time = checker_time;
	}

	public long getChecker_time() {
		return checker_time;
	}

	public synchronized void setHeight(int newHeight) {
		Buckets<T> local_buckets = buckets;
		if(newHeight == local_buckets.height) {
			return;
		}
		buckets = new Buckets<T>(local_buckets, bucketTaker, bucketSize, newHeight, local_buckets.width);		
	}

	public int getHeight() {
		return buckets.height;
	}

	public synchronized void setWidth(int newWidth) {
		Buckets<T> local_buckets = buckets;
		if(newWidth == local_buckets.width) {
			return;
		}
		buckets = new Buckets<T>(local_buckets, bucketTaker, bucketSize, local_buckets.height, newWidth);		
	}
	
	public synchronized void reSize(int newHeight, int newWidth) {
		Buckets<T> local_buckets = buckets;
		if(newHeight == local_buckets.height && newWidth == local_buckets.width) {
			return;
		}
		buckets = new Buckets<T>(local_buckets, bucketTaker, bucketSize, newHeight, newWidth);
	}

	public int getWidth() {
		return buckets.width;
	}

}
