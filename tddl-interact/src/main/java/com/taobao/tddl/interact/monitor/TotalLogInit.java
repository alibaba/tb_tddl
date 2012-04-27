/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//Copyright(c) Taobao.com
package com.taobao.tddl.interact.monitor;

import java.io.File;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a> 
 * @version 1.0
 * @since 1.6
 * @date 2011-8-25下午02:48:05
 */
public class TotalLogInit {
	public static final Logger DB_TAB_LOG = Logger.getLogger("DB_TAB_LOG");
	public static final Logger VSLOT_LOG = Logger.getLogger("VSLOT_LOG");
	public static final Logger DYNAMIC_RULE_LOG=Logger.getLogger("DYNAMIC_RULE_LOG");
	
	static private volatile boolean initOK = false;
	
	private static String getLogPath() {
		String userHome = System.getProperty("user.home");
		if (!userHome.endsWith(File.separator)) {
			userHome += File.separator;
		}
		String path = userHome + "logs" + File.separator + "tddl" + File.separator;
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return path;
	}
	
	static {
		initTddlLog();
	}
	
	private static Appender buildAppender(String name, String fileName, String pattern) {
		DailyRollingFileAppender appender = new DailyRollingFileAppender();
		appender.setName(name);
		appender.setAppend(true);
		appender.setEncoding("GBK");
		appender.setLayout(new PatternLayout(pattern));
		appender.setFile(new File(getLogPath(), fileName).getAbsolutePath());
		appender.activateOptions();// 很重要，否则原有日志内容会被清空
		return appender;
	}
	
	static public void initTddlLog() {
		if (initOK)
			return;
		Appender dbTabAppender = buildAppender("TDDL_Vtab_Appender", "tddl-db-tab.log", "%m");
		Appender vSlotAppender = buildAppender("TDDL_Vtab_Appender", "tddl-vslot.log", "%m");
		Appender dynamicRuleAppender = buildAppender("TDDL_DynamicRule_Appender", "tddl-dynamic-rule.log", "%m");

		DB_TAB_LOG.setAdditivity(false);
		DB_TAB_LOG.removeAllAppenders();
		DB_TAB_LOG.addAppender(dbTabAppender);
		DB_TAB_LOG.setLevel(Level.INFO);
		
		VSLOT_LOG.setAdditivity(false);
		VSLOT_LOG.removeAllAppenders();
		VSLOT_LOG.addAppender(vSlotAppender);
		VSLOT_LOG.setLevel(Level.INFO);
		
		DYNAMIC_RULE_LOG.setAdditivity(false);
		DYNAMIC_RULE_LOG.removeAllAppenders();
		DYNAMIC_RULE_LOG.addAppender(dynamicRuleAppender);
		DYNAMIC_RULE_LOG.setLevel(Level.INFO);
	}
}
