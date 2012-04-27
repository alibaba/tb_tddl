/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.util;

import org.apache.log4j.Logger;

import com.taobao.tddl.common.LoggerInit;

public class NagiosUtils {
	private static final Logger nagiosLog = LoggerInit.TDDL_Nagios_LOG;

	public static final String KEY_DB_NOT_AVAILABLE = "DB_NOT_AVAILABLE"; //数据库不可用,KEY前缀+dbindex
	public static final String KEY_SQL_PARSE_FAIL = "SQL_PARSE_FAIL"; //业务执行了特殊的SQL造成解析失败
	public static final String KEY_REPLICATION_FAIL_RATE = "REPLICATION_FAIL_RATE"; //行复制失败率
	public static final String KEY_REPLICATION_TIME_AVG = "REPLICATION_TIME_AVG"; //一段时间内的行复制平均响应时间
	public static final String KEY_INSERT_LOGDB_FAIL_RATE = "INSERT_LOGDB_FAIL_RATE"; //插日志库失败率
	public static final String KEY_INSERT_LOGDB_TIME_AVG = "INSERT_LOGDB_TIME_AVG"; //一段时间内的插日志库平均响应时间
	

	public static void addNagiosLog(String key, String value) {
		key = key.replaceAll(":", "_");
		key = key.replaceAll(",", "|");
		value = value.replaceAll(":", "_");
		value = value.replaceAll(",", "|");
		innerAddNagiosLog(key, value);
	}

	public static void addNagiosLog(String key, int value) {
		key = key.replaceAll(":", "_");
		key = key.replaceAll(",", "|");
		innerAddNagiosLog(key, Integer.toString(value));
	}

	public static void addNagiosLog(String key, long value) {
		key = key.replaceAll(":", "_");
		key = key.replaceAll(",", "|");
		innerAddNagiosLog(key, Long.toString(value));
	}

	public static void addNagiosLog(String host, String key, String value) {
		host = host.replaceAll(":", "_");
		host = host.replaceAll(",", "|");
		key = key.replaceAll(":", "_");
		key = key.replaceAll(",", "|");
		value = value.replaceAll(":", "_");
		value = value.replaceAll(",", "|");
		innerAddNagiosLog(host, key, value);
	}

	public static void addNagiosLog(String host, String key, int value) {
		host = host.replaceAll(":", "_");
		host = host.replaceAll(",", "|");
		key = key.replaceAll(":", "_");
		key = key.replaceAll(",", "|");
		innerAddNagiosLog(host, key, Integer.toString(value));
	}

	public static void addNagiosLog(String host, String key, long value) {
		host = host.replaceAll(":", "_");
		host = host.replaceAll(",", "|");
		key = key.replaceAll(":", "_");
		key = key.replaceAll(",", "|");
		innerAddNagiosLog(host, key, Long.toString(value));
	}

	private static void innerAddNagiosLog(String key, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(":");
		sb.append(value);
		nagiosLog.info(sb.toString());
	}

	private static void innerAddNagiosLog(String host, String key, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append(host);
		sb.append("_");
		sb.append(key);
		sb.append(":");
		sb.append(value);
		nagiosLog.info(sb.toString());
	}
}
