/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//Copyright(c) Taobao.com
package com.taobao.tddl.interact.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a> 
 * @version 1.0
 * @since 1.6
 * @date 2011-8-25œ¬ŒÁ03:12:25
 */
public class TotalStatMonitor {
	private static Log logger=LogFactory.getLog(TotalStatMonitor.class);
	private volatile long statInterval = 30 * 1000;
//  ≤‚ ‘”√	
//	private volatile long statInterval = 100;
	public static final String logFieldSep = "#@#";
	public static final String linesep = System.getProperty("line.separator");
	private static String appName="TDDL";
	
	private static volatile ConcurrentHashMap<String,AtomicLong> dbTabMap=new ConcurrentHashMap<String, AtomicLong>();
	private static ConcurrentHashMap<String,AtomicLong> dbTablastMap;
	
	private static volatile ConcurrentHashMap<String,AtomicLong> virtualSlotMap=new ConcurrentHashMap<String, AtomicLong>();
	private static ConcurrentHashMap<String,AtomicLong> virtualSlotlastMap;
	
	private static TotalStatMonitor instance = new TotalStatMonitor();

	private TotalStatMonitor() {
	}

	public static TotalStatMonitor getInstance() {
		return instance;
	}
	
	private boolean started = false;
	
	public synchronized void start() {
		if (started) {
			return;
		}
		dbTabFlushTask.start();
		virtualSlotFlushTask.start();
		started = true;
		logger.info("Le Log Monitor Start...");
	}
	
	private Thread dbTabFlushTask = new Thread() {
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(statInterval);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				
				ConcurrentHashMap<String,AtomicLong> tempMap=new ConcurrentHashMap<String,AtomicLong>(); 
				
				dbTablastMap=new ConcurrentHashMap<String,AtomicLong>();
				dbTablastMap=dbTabMap;
				
				dbTabMap=tempMap;
				
				flushDbTabLogMapToFile(dbTablastMap);
			}
		}
	};
	
	private Thread virtualSlotFlushTask = new Thread() {
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(statInterval);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				
				ConcurrentHashMap<String,AtomicLong> tempMap=new ConcurrentHashMap<String,AtomicLong>(); 
				
				virtualSlotlastMap=new ConcurrentHashMap<String,AtomicLong>();
				virtualSlotlastMap=virtualSlotMap;
				
				virtualSlotMap=tempMap;
				
				flushVSlotLogMapToFile(virtualSlotlastMap);
			}
		}
	};

	/**
	 * normal db tab access counter
	 * 
	 * @param key
	 */
	public static void dbTabIncrement(String key){
		AtomicLong incre=dbTabMap.putIfAbsent(key, new AtomicLong(0));
	    if(incre!=null){
	    	incre.addAndGet(1);
	    }
	}
	
    /**
     * virtual slot access counter
     * 
     * @param key
     */
	public static void virtualSlotIncrement(String key){
		AtomicLong incre=virtualSlotMap.putIfAbsent(key, new AtomicLong(0));
	    if(incre!=null){
	    	incre.addAndGet(1);
	    }
	}
	
	/**
	 * recieve rule counter
	 * 
	 * @param version
	 */
	public static void recieveRuleLog(String version){
		SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss:SSS");
		
		String time = df.format(new Date());
		
		StringBuilder sb=new StringBuilder().append(appName).append(logFieldSep).append(version).append(
				logFieldSep).append(time).append(logFieldSep).append(1).append(linesep);
		TotalLogInit.DYNAMIC_RULE_LOG.info(sb.toString());
	}
	
	/**
	 * flush db counter to log
	 * 
	 * @param map
	 */
	private static void flushDbTabLogMapToFile(ConcurrentHashMap<String,AtomicLong> map) {
		SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss:SSS");
		
		String time = df.format(new Date());
		for (Entry<String, AtomicLong> entry : map.entrySet()) {
				String key = entry.getKey();
				StringBuilder sb=new StringBuilder().append(appName).append(logFieldSep).append(key).append(
						logFieldSep).append(entry.getValue()).append(logFieldSep)
						.append(time).append(linesep);
				TotalLogInit.DB_TAB_LOG.info(sb.toString());
		}
	}
	
	/**
	 * flush vslot counter to log
	 * 
	 * @param map
	 */
	private static void flushVSlotLogMapToFile(ConcurrentHashMap<String,AtomicLong> map) {
		SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss:SSS");
		
		String time = df.format(new Date());
		for (Entry<String, AtomicLong> entry : map.entrySet()) {
				String key = entry.getKey();
				StringBuilder sb=new StringBuilder().append(appName).append(logFieldSep).append(key).append(
						logFieldSep).append(entry.getValue()).append(logFieldSep)
						.append(time).append(linesep);
				TotalLogInit.VSLOT_LOG.info(sb.toString());
		}
	}

	public void setAppName(String appName) {
		TotalStatMonitor.appName = appName;
	}

	public static void main(String[] args){
		TotalStatMonitor monitor=new TotalStatMonitor();
		monitor.start();
		
		for(int i=0;i<1000000000;i++){
			TotalStatMonitor.virtualSlotIncrement("tab_slot_1");
			TotalStatMonitor.virtualSlotIncrement("tab_slot_2");
		}
		
		TotalStatMonitor.recieveRuleLog("V1,V2");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		TotalStatMonitor.recieveRuleLog("V1");
	}
}
