/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//package com.taobao.tddl.common.monitor;
//
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.Map.Entry;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.concurrent.locks.ReentrantLock;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
///**
// * guangxia 的monitor不是很能够满足需求，因此这里添加一个新的monitor
// * 
// * 两个key的，会回调接口的
// * 
// * @author shenxun
// * @author junyu
// * 
// */
//public class TMonitor {
//	private static final Log logger = LogFactory.getLog(TMonitor.class);
//
//	/**
//	 * 单位是毫秒
//	 */
//	private static volatile long statInterval = 2 * 60 * 1000;
//
//	/**
//	 * 不要修改
//	 */
//	private static final long cleanInterval = 30 * 60 * 1000;
//	private static int limit = 500;
//
//	private static List<LogOutputListener> monitorLogListeners = new LinkedList<LogOutputListener>();
//	private static List<SnapshotValuesOutputCallBack> snapshotValueCallBack = new LinkedList<SnapshotValuesOutputCallBack>();
//
//	private static volatile boolean started = false;
//
//	private static volatile ConcurrentHashMap<String, Values> currentStatMapNeedLimit = new ConcurrentHashMap<String, Values>();
//	private static volatile ConcurrentHashMap<String, Values> currentStatMapWithOutLimit = new ConcurrentHashMap<String, Values>();
//	private static final ReentrantLock lockLimit = new ReentrantLock();
//	private static CycleExecuteTimer outPutTimer;
//	private static CycleExecuteTimer cleanTimer;
//
//	static {
//		start();
//	}
//
//	private static void start() {
//		if (started) {
//			return;
//		}
//
//		/**
//		 * 启动输出到日志的任务
//		 * 
//		 * 1.CallBack的日志。 2.限制key的日志。 3.非限定key的日志。
//		 * 
//		 * 并且同时清空非限定key的value值。
//		 * 
//		 * 每隔2分钟执行一次。
//		 */
//		outPutTimer = new CycleExecuteTimer("LogOutPutTask", new Runnable() {
//			public void run() {
//				writeCallBackLog();
//				ConcurrentHashMap<String, Values> newMap = copyNewWithOutLimitMap();
//				ConcurrentHashMap<String, Values> oldMap = currentStatMapWithOutLimit;
//				currentStatMapWithOutLimit = newMap;
//				writeLogMapToFile(currentStatMapNeedLimit);
//				writeLogMapToFile(oldMap);
//				oldMap.clear();
//			}
//		}, statInterval, TimeUnit.MILLISECONDS, null);
//
//		outPutTimer.start();
//
//		/**
//		 * 启动清空限制key数量Map的任务 默认在00:00:00,00:30:00,01:00:00,...,23:30:00运行
//		 */
//		cleanTimer = new CycleExecuteTimer(
//				"LimitLogCleanTask", new Runnable() {
//					public void run() {
//						resetLimitMap();
//					}
//				}, cleanInterval, TimeUnit.MILLISECONDS, new HalfTimeComputer());
//
//		cleanTimer.start();
//		addOutputListener(DefaultLogOutputListener.getInstance());
//		started = true;
//		logger.warn("tddl monitor start...");
//
//	}
//
//	/**
//	 * 重置整个受限制map
//	 */
//	private static void resetLimitMap() {
//		lockLimit.lock();
//		try {
//			// 原子的清空size个数和锁
//			size = 0;
//			ConcurrentHashMap<String, Values> oldMap = currentStatMapNeedLimit;
//
//			currentStatMapNeedLimit = new ConcurrentHashMap<String, Values>(
//					limit);
//			// help gc
//			oldMap.clear();
//			logger.warn("清空key数量限制Map");
//		} finally {
//			lockLimit.unlock();
//		}
//	}
//
//	/**
//	 * 只清空value,保留key
//	 */
//	private static ConcurrentHashMap<String, Values> copyNewWithOutLimitMap() {
//		Set<String> keySet = currentStatMapWithOutLimit.keySet();
//		ConcurrentHashMap<String, Values> keepKeysMap = new ConcurrentHashMap<String, Values>();
//		for (String key : keySet) {
//			keepKeysMap.put(key, new Values());
//		}
//		return keepKeysMap;
//	}
//
//	/**
//	 * 拉取自定义日志内容并打印(单线程，无需锁)
//	 */
//	private static void writeCallBackLog() {
//		ConcurrentHashMap<String, Values> tempMap = new ConcurrentHashMap<String, Values>();
//		for (SnapshotValuesOutputCallBack callBack : snapshotValueCallBack) {
//			ConcurrentHashMap<String, Values> values = callBack.getValues();
//			Map<String, Values> copiedMap = new HashMap<String, TMonitor.Values>(
//					values);
//			for (Entry<String, Values> entry : copiedMap.entrySet()) {
//				Values value = tempMap.get(entry.getKey());
//				if (null == value) {
//					value = new Values();
//					tempMap.putIfAbsent(entry.getKey(), value);
//				}
//				value.value1.addAndGet(entry.getValue().value1.get());
//				value.value2.addAndGet(entry.getValue().value2.get());
//			}
//		}
//
//		writeLogMapToFile(tempMap);
//	}
//
//	/**
//	 * 将内存数据输出到日志中
//	 * 
//	 * @param oldMap
//	 */
//	private static void writeLogMapToFile(ConcurrentHashMap<String, Values> map) {
//		for (LogOutputListener listener : monitorLogListeners) {
//			listener.actionPerform(map, System.currentTimeMillis());
//		}
//	}
//
//	/**
//	 * 添加一条日志信息到非限制key数量的map中，定时刷出到文件日志。 刷出日志时保留key，清空value。
//	 * 
//	 * @param key
//	 * @param value1
//	 * @param value2
//	 */
//	private static void add(String key, long value1, long value2) {
//		Values values = currentStatMapWithOutLimit.get(key);
//
//		if (null == values) {
//			Values newValues = new Values();
//			Values alreadyValues = currentStatMapWithOutLimit.putIfAbsent(key,
//					newValues);
//			if (null == alreadyValues) {
//				// 表示原子put成功
//				values = newValues;
//			} else {
//				// 表示原子put时已经有值了。
//				values = alreadyValues;
//			}
//		}
//
//		values.value1.addAndGet(value1);
//		values.value2.addAndGet(value2);
//	}
//
//	/**
//	 * 添加一条日志信息到限制key数量的map中，定时输出到文件日志中. 1.如果key不存在,并且当前map key数量小于限定值，那么生成
//	 * 一个新的<key,value>结构插入到map中。 2.如果key不存在,并且当前map key数量等于限定值, 那么抛弃 这条日志信息。
//	 * 3.如果key存在,那么根据key取得value并且加上新的值。
//	 * 
//	 * 刷出日志时不清空key,value,但半小时重置日志map
//	 * 
//	 * @param key
//	 * @param value1
//	 * @param value2
//	 */
//	private static void addWithLimit(String key, long value1, long value2) {
//		if (key == null || key.length() == 0) {
//			return;
//		}
//		key = "limit_" + key;
//
//		Values values = currentStatMapNeedLimit.get(key);
//		/**
//		 * 1. 判断value是否为null 2. 判断是否允许添加 加锁 判断value是否为null 判断是否允许添加 添加 自增 解锁
//		 */
//		if (null == values) {
//			if (size + 1 > limit) {
//				// 如果size 不允许再添加了，那么直接返回；
//				logger.debug("size 超过范围，丢弃");
//				return;
//			} else {
//				// 那么这里是size还允许增加， 并且没有这个key所对应的value
//
//				lockLimit.lock();
//				try {
//					// 双检查
//					values = currentStatMapNeedLimit.get(key);
//					if (null == values) {
//
//						if (size + 1 > limit) {
//							return;
//						}
//						// 保证size++ 和put是一个原子操作
//						size++;
//						values = new Values();
//						currentStatMapNeedLimit.put(key, values);
//					}
//				} finally {
//					lockLimit.unlock();
//				}
//			}
//		}
//
//		values.value1.addAndGet(value1);
//		values.value2.addAndGet(value2);
//
//	}
//
//	private static volatile int size = 0; // 似乎是可以不用volatile的，保险起见。
//
//	public static List<LogOutputListener> getOutputListener() {
//		return monitorLogListeners;
//	}
//	public static synchronized void removeSnapshotValuesCallback(SnapshotValuesOutputCallBack callbackList){
//		snapshotValueCallBack.remove(callbackList);
//	}
//	public static synchronized void addSnapshotValuesCallbask(
//			SnapshotValuesOutputCallBack callbackList) {
//		if (snapshotValueCallBack.contains(callbackList)) {
//			// only one instance is allowed
//			return;
//		}
//		snapshotValueCallBack.add(callbackList);
//	}
//
//	public static synchronized void addOutputListener(LogOutputListener listener) {
//		if (monitorLogListeners.contains(listener)) {
//			// only one instance is allowed
//			return;
//		}
//		monitorLogListeners.add(listener);
//	}
//
//	public static long getStatInterval() {
//		return statInterval;
//	}
//
//	public static synchronized void setStatInterval(long statIntervals) {
//		statInterval = statIntervals;
//	}
//
//	public static void reStartMonitor(){
//		outPutTimer.stop();
//		cleanTimer.stop();
//		started=false;
//		start();
//	}
//
//	public static class Values {
//		public final AtomicLong value2 = new AtomicLong(0L);
//		public final AtomicLong value1 = new AtomicLong(0L);
//
//		@Override
//		public String toString() {
//			StringBuilder sb = new StringBuilder();
//			long v1 = value1.get();
//			long v2 = value2.get();
//
//			sb.append("[").append(v1).append(":").append(v2).append("]");
//			return sb.toString();
//		}
//	}
//}
