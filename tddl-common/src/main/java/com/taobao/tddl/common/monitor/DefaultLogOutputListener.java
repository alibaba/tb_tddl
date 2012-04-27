/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//package com.taobao.tddl.common.monitor;
//
//import java.util.Map.Entry;
//import java.util.concurrent.ConcurrentHashMap;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import com.taobao.tddl.common.monitor.TMonitor.Values;
//
//public class DefaultLogOutputListener implements LogOutputListener {
//	private DefaultLogOutputListener(){
//		super();
//	}
//	private static final String ls = System.getProperty("line.separator");
//	private static final Log scheduledThreadLogger = LogFactory.getLog("TDDL_MONITOR_OUTPUT_LOGGER");
//	private static final DefaultLogOutputListener listener = new DefaultLogOutputListener();
//	public static DefaultLogOutputListener getInstance(){
//		
//		return listener;
//	}
//	public void actionPerform(ConcurrentHashMap<String, Values> lastStatMap,
//			long currentTimeMillis) {
//		StringBuilder sb = new StringBuilder();
//		for (Entry<String, Values> entry : lastStatMap.entrySet()) {
//			sb.append(entry.getKey()).append(" ");
//			Values values = entry.getValue();
//			sb.append(
//					values == null ? "0:0" : values.value1 + ":"
//							+ values.value2).append(" ");
//
//			sb.append(ls);
//		}
//		scheduledThreadLogger.warn(sb.toString());
//	}
//}
