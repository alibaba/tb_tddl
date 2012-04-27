/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.monitor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.taobao.tddl.common.LoggerInit;

/**
 * 带缓存的写日志工具。解决高tps项，统计日志量太大的问题
 * 对统计日志在内存中作缓冲合并，定时刷出。
 * add(key1(统计目标),key2(group),key3(flag),timeuse)
 * 
 * 内存中及刷出后的结构为：
 * sql(md5) dbname/app   flag     count(sum) time(sum)    min         max         
 * sql      logicDbName  执行成功  执行次数   响应时间   最小响应时间 最大响应时间
 * sql      realDbName1  执行成功  执行次数   响应时间   最小响应时间 最大响应时间
 * sql      realDbName2  执行成功  执行次数   响应时间   最小响应时间 最大响应时间
 * sql      realDbName2  执行失败  执行次数   响应时间   最小响应时间 最大响应时间
 * sql      realDbName2  执行超时  执行次数   响应时间   最小响应时间 最大响应时间
 * sql      null         解析成功  执行次数   响应时间   最小响应时间 最大响应时间
 * sql      null         解析失败  执行次数   响应时间   最小响应时间 最大响应时间
 * sql      null         解析命中  执行次数   命中次数     NA          NA
 * 
 * 最后由日志解析工具生成的报表可能是：
 * sql dbname/app 成功次数  成功平均响应时间 成功最小响应时间 成功最大响应时间  失败次数  失败平均响应时间 失败最小响应时间 失败最大响应时间
 * 
 * key太多的问题：
 * 用定长map，当map满时，刷出执行次数最小的1/3数据。这样的好处是不用每次get/put都排序。不会频繁刷出。
 * 既能拦截绝大部分热点key的流量，又相当于对非热点的key做了批量写入。
 * 
 * 副作用：
 * 因为累加了一段时间内的执行次数和响应时间，可以同时作为时间片方式的实时监控报警。但是报警的间隔时间可能要求更小
 * 
 * @author linxuan
 *
 */
public class BufferedStatLogWriter {

	// private static final Log logger =
	// LogFactory.getLog(BufferedStatLogWriter.class);
	// private static final Logger logger = LoggerInit.TDDL_LOG;
	public static final Logger statlog = LoggerInit.TDDL_Statistic_LOG;
	public static final String logFieldSep = "#@#"; // sql中出现概率小，和正则式不冲突
	public static final String linesep = System.getProperty("line.separator");
	public static volatile int maxkeysize = 1024;
	public static volatile int dumpInterval = 300; // 单位秒。默认5分钟全量刷出一次
	public static final SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss:SSS");

	private static LogWriter logWriter = new LogWriter() {
		private void addLine(StringBuilder sb, Object key, Object group, Object flag, StatCounter sc, String time) {
			sb.append(key).append(logFieldSep).append(group).append(logFieldSep).append(flag).append(logFieldSep)
					.append(sc.getCount()).append(logFieldSep).append(sc.getValue()).append(logFieldSep).append(
							sc.getMin()).append(logFieldSep).append(sc.getMax()).append(logFieldSep).append(time)
					.append(linesep);
		}

		public void writeLog(Map<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> map) {
			statlog.debug(Thread.currentThread().getName() + "[writeLog]map.size()=" + map.size() + linesep);
			StringBuilder sb = new StringBuilder();
			String time = df.format(new Date());
			for (Map.Entry<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> e0 : map
					.entrySet()) {
				for (Map.Entry<Object, ConcurrentHashMap<Object, StatCounter>> e1 : e0.getValue().entrySet()) {
					for (Map.Entry<Object, StatCounter> e2 : e1.getValue().entrySet()) {
						StatCounter sc = e2.getValue();
						addLine(sb, e0.getKey(), e1.getKey(), e2.getKey(), sc, time);
					}
				}
			}
			statlog.warn(sb);
		}
	};

	public static void setLogWriter(LogWriter logWriter) {
		BufferedStatLogWriter.logWriter = logWriter;
	}

	public static interface LogWriter {
		// void write(Object key, Object group, Object flag, StatCounter
		// counter);
		void writeLog(Map<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> map);
	}

	static class StatCounter {
		private final AtomicLong count = new AtomicLong(0L);
		private final AtomicLong value = new AtomicLong(0L);
		private final AtomicLong min = new AtomicLong(Long.MAX_VALUE); // value最小值
		private final AtomicLong max = new AtomicLong(Long.MIN_VALUE); // value最大值

		public void add(long c, long v) {
			// this.count.incrementAndGet();
			this.count.addAndGet(c);
			this.value.addAndGet(v);
			while (true) {
				long vmin = min.get();
				if (v < vmin) {
					if (min.compareAndSet(vmin, v)) {
						break;
					}
					continue; // 有可能已经被其他线程设置了一个次小的，所以继续判断
				}
				break;
			}
			while (true) {
				long vmax = max.get();
				if (v > vmax) {
					if (max.compareAndSet(vmax, v)) {
						break;
					}
					continue; // 有可能已经被其他线程设置了一个次大的，所以继续判断
				}
				break;
			}
		}

		public synchronized void reset() {
			this.count.set(0L);
			this.value.set(0L);
			this.min.set(Long.MAX_VALUE);
			this.max.set(Long.MIN_VALUE);
		}

		public long getCount() {
			return this.count.get();
		}

		public long getValue() {
			return this.value.get();
		}

		public long getMin() {
			return this.min.get();
		}

		public long getMax() {
			return this.max.get();
		}

		public long[] get() {
			return new long[] { this.count.get(), this.value.get(), this.min.get(), this.max.get() };
		}
	}

	private static ConcurrentHashMap<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> keys = new ConcurrentHashMap<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>>(
			maxkeysize, 0.75f, 32);

	public static void add(Object key, Object group, Object flag, long timeuse) {
		add(key, group, flag, 1, timeuse);
	}

	public static void add(Object key, Object group, Object flag, long count, long timeuse) {
		ConcurrentHashMap<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> oldkeys = keys;
		ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>> groups = oldkeys.get(key);
		if (groups == null) {
			ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>> newGroups = new ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>();
			groups = oldkeys.putIfAbsent(key, newGroups);
			if (groups == null) {
				groups = newGroups;
				insureSize();
			}
		}
		ConcurrentHashMap<Object, StatCounter> flags = groups.get(group);
		if (flags == null) {
			ConcurrentHashMap<Object, StatCounter> newFlags = new ConcurrentHashMap<Object, StatCounter>();
			flags = groups.putIfAbsent(group, newFlags);
			if (flags == null) {
				flags = newFlags;
			}
		}
		StatCounter counter = flags.get(flag);
		if (counter == null) {
			StatCounter newCounter = new StatCounter();
			counter = flags.putIfAbsent(flag, newCounter);
			if (counter == null) {
				counter = newCounter;
			}
		}
		counter.add(count, timeuse);
	}

	private static Lock lock = new ReentrantLock();
	private static volatile boolean isInFlushing = false;
	private static ExecutorService flushExecutor = Executors.newSingleThreadExecutor();
	// private static ScheduledExecutorService executor =
	// Executors.newSingleThreadScheduledExecutor();
	private static final Thread fullDumpThread;

	static {
		LoggerInit.initTddlLog();
		/*
		executor.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				submitFlush(true);
			}
		}, dumpInterval, dumpInterval, TimeUnit.SECONDS);
		*/
		fullDumpThread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(dumpInterval * 1000);
					} catch (InterruptedException e) {
					}
					submitFlush(true);
				}
			}
		});
		fullDumpThread.start();
	}

	private static void insureSize() {
		if (keys.size() < maxkeysize) {
			return;
		}
		// logger.info("[insureSize]keys.size()="+keys.size());
		submitFlush(false);
	}

	private static boolean submitFlush(final boolean isFlushAll) {
		if (!isInFlushing && lock.tryLock()) {
			try {
				isInFlushing = true;
				flushExecutor.execute(new Runnable() {
					public void run() {
						try {
							if (isFlushAll) {
								flushAll();
							} else {
								flushLRU();
							}
						} finally {
							isInFlushing = false;
						}
					}
				});
			} finally {
				lock.unlock();
			}
			return true;
		}
		return false;
	}

	/**
	 * 只有一个线程会执行flushAll或flushLRU方法
	 */
	private static void flushAll() {
		Map<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> res = keys;
		keys = new ConcurrentHashMap<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>>(
				maxkeysize);
		try {
			Thread.sleep(5); // 等待已经进入keys子项的线程计算完
		} catch (InterruptedException e) {
		}
		statlog.info("[flushAll]size=" + res.size() + linesep);
		logWriter.writeLog(res);
		res = null;
	}

	private static final Comparator<Object[]> countsComparator = new Comparator<Object[]>() {
		public int compare(Object[] keycount1, Object[] keycount2) {
			Long v1 = (Long) keycount1[1];
			Long v2 = (Long) keycount2[1];
			return v1.compareTo(v2);
		}
	};

	/**
	 * 刷出执行次数最少的key，只保留keysize的2/3
	 */
	private static void flushLRU() {
		List<Object[]> counts = new ArrayList<Object[]>();
		for (Map.Entry<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> e0 : keys.entrySet()) {
			long count = 0;
			for (Map.Entry<Object, ConcurrentHashMap<Object, StatCounter>> e1 : e0.getValue().entrySet()) {
				for (Map.Entry<Object, StatCounter> e2 : e1.getValue().entrySet()) {
					count += e2.getValue().getCount();
				}
			}
			counts.add(new Object[] { e0.getKey(), count });
		}
		statlog.debug("sortedSize=" + counts.size() + ",keys.size=" + keys.size() + linesep);// sortedSize=1135,keys.size=1169
		Collections.sort(counts, countsComparator);
		int i = 0;
		int remain = maxkeysize * 2 / 3; // 保留2/3
		int flush = keys.size() - remain; // 这时size可能已经增长
		Map<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> flushed = new HashMap<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>>();
		for (Object[] keycount : counts) {
			Object key = keycount[0];
			ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>> removed = keys.remove(key);
			if (removed != null) {
				flushed.put(key, removed);
				i++;
			} else {
				statlog.warn("-------------- Should not happen!!! ------------");
			}
			if (i >= flush) {
				if (keys.size() <= remain)
					break;
			}
		}
		statlog.info("[flushLRU]flushedSize=" + flushed.size() + ",keys.size=" + keys.size());
		logWriter.writeLog(flushed);
		flushed = null;
	}

	/*
	private static void writeLog(Map<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> map) {
		logger.info(Thread.currentThread().getName() + "[writeLog]map.size()=" + map.size());
		for (Map.Entry<Object, ConcurrentHashMap<Object, ConcurrentHashMap<Object, StatCounter>>> e0 : map.entrySet()) {
			for (Map.Entry<Object, ConcurrentHashMap<Object, StatCounter>> e1 : e0.getValue().entrySet()) {
				for (Map.Entry<Object, StatCounter> e2 : e1.getValue().entrySet()) {
					StatCounter sc = e2.getValue();
					logWriter.write(e0.getKey(), e1.getKey(), e2.getKey(), sc);
				}
			}
		}
	}
	*/

	public static void main(String[] args) throws InterruptedException {
		LoggerInit.TDDL_Nagios_LOG.fatal("test");
		LoggerInit.TDDL_SQL_LOG.fatal("test");
		LoggerInit.TDDL_MD5_TO_SQL_MAPPING.fatal("test");
		LoggerInit.TDDL_LOG.fatal("test");
		for (int i = 0; i < 5000; i++) {
			BufferedStatLogWriter.add("select 1", "tc", "success", 20L);
			BufferedStatLogWriter.add("select 1", "tc", "faile", 50L);
			BufferedStatLogWriter.add("update 1", "ic", "success", 10L);
			BufferedStatLogWriter.add("insert a" + i, "ic", "success", 11L);
			if (i % 10 == 0) {
				Thread.sleep(1);
			}
		}
		statlog.info("-------------------------------------");
		// BufferedStatLogWriter.flushLRU();
		Thread.sleep(10000);
		statlog.info("end");
	}
}
