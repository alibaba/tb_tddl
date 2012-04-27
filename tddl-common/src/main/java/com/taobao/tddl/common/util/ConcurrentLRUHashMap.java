/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.util;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.taobao.tddl.common.exception.lru.LRUHashMapException;



/**
 * 并发最近最少使用HashMap 注意：要添加一个，一定要会删除一个，所以，总的容量是不会改变的。
 * 
 * @author xudanhui.pt Jan 19, 2011,2:59:42 PM
 */
public class ConcurrentLRUHashMap<K, V> extends AbstractMap<K, V> implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1089799193141551978L;

	static final int DEFAULT_INITIAL_CAPACITY = 16;

	static final float DEFAULT_LOAD_FACTOR = 0.75f;

	static final int DEFAULT_CONCURRENCY_LEVEL = 16;

	static final int MAXIMUM_CAPACITY = 1 << 30;

	static final int MAX_SEGMENTS = 1 << 16;

	final int segmentMask;

	final int segmentShift;

	final Segment<K, V>[] segments;

	static final int RETRIES_BEFORE_LOCK = 2;

	private static int hash(int h) {
		// Spread bits to regularize both segment and index locations,
		// using variant of single-word Wang/Jenkins hash.
		h += (h << 15) ^ 0xffffcd7d;
		h ^= (h >>> 10);
		h += (h << 3);
		h ^= (h >>> 6);
		h += (h << 2) + (h << 14);
		return h ^ (h >>> 16);
	}

	final Segment<K, V> segmentFor(int hash) {
		return segments[(hash >>> segmentShift) & segmentMask];
	}

	public ConcurrentLRUHashMap(int initialCapacity, float loadFactor,
			int concurrencyLevel) {

		if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
			throw new IllegalArgumentException();

		if (concurrencyLevel > MAX_SEGMENTS)
			concurrencyLevel = MAX_SEGMENTS;

		// Find power-of-two sizes best matching arguments
		int sshift = 0;
		int ssize = 1;
		while (ssize < concurrencyLevel) {
			++sshift;
			ssize <<= 1;
		}
		segmentShift = 32 - sshift;
		segmentMask = ssize - 1;
		this.segments = Segment.newArray(ssize);

		if (initialCapacity > MAXIMUM_CAPACITY)
			initialCapacity = MAXIMUM_CAPACITY;
		int c = initialCapacity / ssize;
		if (c * ssize < initialCapacity)
			++c;
		int cap = 1;
		while (cap < c)
			cap <<= 1;
		for (int i = 0; i < this.segments.length; ++i)
			this.segments[i] = new Segment<K, V>(cap, loadFactor);
	}

	/**
	 * Hash锁分离段
	 * 
	 * @author xudanhui.pt Jan 19, 2011,3:08:09 PM
	 * @param <K>
	 * @param <V>
	 */
	static final class Segment<K, V> extends ReentrantLock implements
			Serializable {
		private static final long serialVersionUID = 1L;
		/**
		 * 阈值
		 */
		transient int threshold;
		/**
		 * 计数
		 */
		transient volatile int count;
		/**
		 * 修改标记
		 */
		transient int modCount;
		/**
		 * hash表
		 */
		transient volatile HashEntry<K, V>[] table;
		/**
		 * 段内头节点，哨兵节点。
		 */
		transient final HashEntry<K, V> header;

		Segment(int initialCapacity, float lf) {
			// 创建hash表
			table = HashEntry.<K, V> newArray(initialCapacity);
			// 计算阈值
			threshold = (int) (initialCapacity * lf);
			// 段内计数初值为0
			count = 0;
			// 构建双向链表的头节点
			header = new HashEntry(null, -1, null, null, null, null,
					new AtomicBoolean(false));
			header.linknext = header;
			header.linkpref = header;
		}

		@SuppressWarnings("unchecked")
		static final <K, V> Segment<K, V>[] newArray(int i) {
			return new Segment[i];
		}

		// 取得hash表中第一个值
		HashEntry<K, V> getFirst(int hash) {
			HashEntry<K, V>[] tab = table;
			return tab[hash & (tab.length - 1)];
		}

		// 在有锁的情况下读值
		V readValueUnderLock(HashEntry<K, V> e) {
			lock();
			try {
				return e.value;
			} finally {
				unlock();
			}
		}

		// 把节点移动到双向链表的头部
		void moveNodeToHeader(HashEntry<K, V> e) {
			lock();
			try {

				// TODO 这里需要修改一下空操作的验证
				if (!e.dead.get()) {

					// 从退化连表中断开连接
					e.linkpref.linknext = e.linknext;
					e.linknext.linkpref = e.linkpref;

					// 添加到退化链表头部
					header.linknext.linkpref = e;
					e.linknext = header.linknext;
					e.linkpref = header;
					header.linknext = e;
				}
			} finally {
				unlock();
			}

		}

		// get操作
		V get(Object key, int hash) {
			HashEntry<K, V> e = getFirst(hash);
			// 遍历查找
			while (e != null) {
				if (e.hash == hash && key.equals(e.key)) {
					V v = e.value;
					// 把节点移动到头部。
					moveNodeToHeader(e);
					if (v != null)
						return v;
					// 在锁的情况读，必定能读到。
					// tab[index] = new HashEntry<K,V>(key, hash, first, value)，
					// value赋值和tab[index]赋值可能会重新排序，重新排序之后，可能会读空值
					// 读到空值的话，在有锁的情况在再读一遍，一定能读！
					return readValueUnderLock(e); // recheck
				}
				e = e.next;
			}
			return null;
		}

		// 删除老节点
		private void removeOldNode() throws LRUHashMapException {
			lock();
			try {
				int c = count;
				// 执行删除操作。
				HashEntry<K, V> node = header.linkpref;
				if (node == null) {
					throw new LRUHashMapException("segment中的退化链表崩溃！");
				}
				if (node == header) {
					throw new LRUHashMapException("segment中退化链表已经为空！");
				}
				// 已经标记为死亡，理论上来说，不会有这样的情况。
				if (node.dead.get()) {
					throw new LRUHashMapException("node非正常死亡");
				}
				remove(node.key, node.hash, node.value);//删除节点
			} finally {
				unlock();
			}
		}

		// 插入操作
		V put(K key, int hash, V value, boolean onlyIfAbsent)
				throws LRUHashMapException {
			lock();
			try {
				int c = count;
				// 如果当前节点数量已经大于等于阈值
				if (c >= threshold) {
					removeOldNode();// 删不掉就会抛出错误，抛出错误就完了！
					c=count;
				}

				// 执行put操作！
				HashEntry<K, V>[] tab = table;
				int index = hash & (tab.length - 1);
				HashEntry<K, V> first = tab[index];
				HashEntry<K, V> e = first;
				while (e != null && (e.hash != hash || !key.equals(e.key)))
					e = e.next;
				V oldValue;

				if (e != null) {
					oldValue = e.value;
					if (!onlyIfAbsent) {
						e.value = value;
						moveNodeToHeader(e);// 移动到头部
					}
				} else {
					oldValue = null;

					++modCount;
					HashEntry<K, V> newNode = new HashEntry<K, V>(key, hash,
							first, value, header.linknext, header,
							new AtomicBoolean(false));

					header.linknext.linkpref = newNode;
					header.linknext = newNode;
					tab[index] = newNode;
					c++;
					count = c;
				}

			} finally {
				unlock();
			}
			return null;
		}

		// 删除程序
		V remove(Object key, int hash, Object value) {
			lock();
			try {
				int c = count - 1;
				HashEntry<K, V>[] tab = table;
				int index = hash & (tab.length - 1);
				HashEntry<K, V> first = tab[index];
				HashEntry<K, V> e = first;
				while (e != null && (e.hash != hash || !key.equals(e.key)))
					e = e.next;

				V oldValue = null;
				if (e != null) {
					// 把节点从Hash链表中脱离出来
					e.dead.set(true);
					// 把尾部节点从链表中删除！
					e.linkpref.linknext = e.linknext;
					e.linknext.linkpref = e.linkpref;

					// 把节点从hash槽中删除
					V v = e.value;
					if (value == null || value.equals(v)) {
						oldValue = v;
						++modCount;
						HashEntry<K, V> newFirst = e.next;
						// 循环删除
						for (HashEntry<K, V> p = first; p != e; p = p.next) {
							HashEntry<K, V> newNode = new HashEntry<K, V>(
									p.key, p.hash, newFirst, p.value,
									p.linknext, p.linkpref, new AtomicBoolean(
											false));
							// 调整链表
							p.linknext.linkpref = newNode;
							p.linkpref.linknext = newNode;
							p.dead.set(true);// 标记死亡，这个很重要！
							// 赋值
							newFirst = newNode;
						}
						tab[index] = newFirst;
						count = c; // write-volatile
					}
				}
				return oldValue;
			} finally {
				unlock();
			}
		}

	}

	/**
	 * Hash槽内链表的节点
	 * 
	 * @author xudanhui.pt Jan 19, 2011,3:04:07 PM
	 * @param <K>
	 * @param <V>
	 */
	static class HashEntry<K, V> {
		/**
		 * 键
		 */
		final K key;
		/**
		 * hash值
		 */
		final int hash;
		/**
		 * 值
		 */
		volatile V value;
		/**
		 * hash链指针
		 */
		final HashEntry<K, V> next;
		/**
		 * 双向链表的下一个节点
		 */
		HashEntry<K, V> linknext;
		/**
		 * 双向链表的下一个节点
		 */
		HashEntry<K, V> linkpref;
		/**
		 * 死亡标记
		 */
		AtomicBoolean dead;

		HashEntry(K key, int hash, HashEntry<K, V> next, V value,
				HashEntry<K, V> linknext, HashEntry<K, V> linkpref,
				AtomicBoolean dead) {
			this.key = key;
			this.hash = hash;
			this.next = next;
			this.value = value;
			this.linknext = linknext;
			this.linkpref = linkpref;
			this.dead = dead;
		}

		static final <K, V> HashEntry<K, V>[] newArray(int i) {
			return new HashEntry[i];
		}
	}

	public V putIfAbsent(K key, V value) {
		if (value == null)
			throw new NullPointerException();
		int hash = hash(key.hashCode());
		try {
			return segmentFor(hash).put(key, hash, value, true);
		} catch (LRUHashMapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

	@Override
	public V put(K key, V value) {
		if (value == null)
			throw new NullPointerException();
		int hash = hash(key.hashCode());
		try {
			return segmentFor(hash).put(key, hash, value, false);
		} catch (LRUHashMapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}

	@Override
	public V get(Object key) {
		int hash = hash(key.hashCode());
		return segmentFor(hash).get(key, hash);
	}

	@Override
	public int size() {
		final Segment<K, V>[] segments = this.segments;
		long sum = 0;
		long check = 0;
		int[] mc = new int[segments.length];
		// Try a few times to get accurate count. On failure due to
		// continuous async changes in table, resort to locking.
		for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k) {
			check = 0;
			sum = 0;
			int mcsum = 0;
			for (int i = 0; i < segments.length; ++i) {
				sum += segments[i].count;
				mcsum += mc[i] = segments[i].modCount;
			}
			if (mcsum != 0) {
				for (int i = 0; i < segments.length; ++i) {
					check += segments[i].count;
					if (mc[i] != segments[i].modCount) {
						check = -1; // force retry
						break;
					}
				}
			}
			if (check == sum)
				break;
		}
		if (check != sum) { // Resort to locking all segments
			sum = 0;
			for (int i = 0; i < segments.length; ++i)
				segments[i].lock();
			for (int i = 0; i < segments.length; ++i)
				sum += segments[i].count;
			for (int i = 0; i < segments.length; ++i)
				segments[i].unlock();
		}
		if (sum > Integer.MAX_VALUE)
			return Integer.MAX_VALUE;
		else
			return (int) sum;
	}

	public V remove(Object key) {
		int hash = hash(key.hashCode());
		return segmentFor(hash).remove(key, hash, null);
	}

	public boolean remove(Object key, Object value) {
		int hash = hash(key.hashCode());
		if (value == null)
			return false;
		return segmentFor(hash).remove(key, hash, value) != null;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}
}
