/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.common;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * TAtom数据源的常量设置类
 *
 * @author qihao
 *
 */
public class TAtomConstants {

	public final static String DEFAULT_DIAMOND_GROUP = null;

	public final static String DEFAULT_MYSQL_CHAR_SET = "gbk";

	//public final static String ORACLE_DBTYPE_STR = "ORACLE";

	//public final static String MYSQL_DBTYPE_STR = "MYSQL";

	public final static String DEFAULT_ORACLE_CON_TYPE = "oci";

	public final static String DB_STATUS_R = "R";

	public final static String DB_STATUS_W = "W";

	public final static String DB_STATUS_RW = "RW";

	public final static String DB_STATUS_NA = "NA";

	public static Map<String, String> DEFAULT_ORACLE_CONNECTION_PROPERTIES = new HashMap<String, String>(
			2);
	static {
		TAtomConstants.DEFAULT_ORACLE_CONNECTION_PROPERTIES.put(
				"SetBigStringTryClob", "true");
		TAtomConstants.DEFAULT_ORACLE_CONNECTION_PROPERTIES.put(
				"defaultRowPrefetch", "50");
	}

	public static Map<String, String> DEFAULT_MYSQL_CONNECTION_PROPERTIES = new HashMap<String, String>(
			1);
	static {
		TAtomConstants.DEFAULT_MYSQL_CONNECTION_PROPERTIES.put(
				"characterEncoding", "gbk");
	}

	public final static String DEFAULT_ORACLE_DRIVER_CLASS = "oracle.jdbc.driver.OracleDriver";

	public final static String DEFAULT_MYSQL_DRIVER_CLASS = "com.mysql.jdbc.Driver";

	public final static String DEFAULT_ORACLE_SORTER_CLASS = "com.taobao.datasource.resource.adapter.jdbc.vendor.OracleExceptionSorter";

	public final static String DEFAULT_MYSQL_SORTER_CLASS = "com.taobao.datasource.resource.adapter.jdbc.vendor.MySQLExceptionSorter";


	public final static String MYSQL_INTEGRATION_SORTER_CLASS = "com.mysql.jdbc.integration.jboss.ExtendedMysqlExceptionSorter";

	public final static String DEFAULT_MYSQL_VALID_CONNECTION_CHECKERCLASS = "com.mysql.jdbc.integration.jboss.MysqlValidConnectionChecker";

	/**
	 * 全局配置dataId模板
	 */
	private static MessageFormat GLOBAL_FORMAT = new MessageFormat(
			"com.taobao.tddl.atom.global.{0}");

	/**
	 * 应用配置dataId模板
	 */
	private static MessageFormat APP_FORMAT = new MessageFormat(
			"com.taobao.tddl.atom.app.{0}.{1}");

	private static MessageFormat PASSWD_FORMAT = new MessageFormat(
			"com.taobao.tddl.atom.passwd.{0}.{1}.{2}");

	/**
	 * dbName模板
	 */
	private static MessageFormat DB_NAME_FORMAT = new MessageFormat(
			"atom.dbkey.{0}^{1}");

	/**
	 * 根据dbKey获取全局配置dataId
	 *
	 * @param dbKey
	 *            数据库名KEY
	 * @return
	 */
	public static String getGlobalDataId(String dbKey) {
		return GLOBAL_FORMAT.format(new Object[] { dbKey });
	}

	/**
	 * 根据应用名和dbKey获取指定的应用配置dataId
	 *
	 * @param appName
	 * @param dbKey
	 * @return
	 */
	public static String getAppDataId(String appName, String dbKey) {
		return APP_FORMAT.format(new Object[] { appName, dbKey });
	}

	/**
	 * 根据dbKey和userName获得对应的passwd的dataId
	 *
	 * @param dbKey
	 * @param userName
	 * @return
	 */
	public static String getPasswdDataId(String dbName, String dbType,
			String userName) {
		return PASSWD_FORMAT.format(new Object[] { dbName, dbType, userName });
	}

	/**
	 * @param appName
	 * @param dbkey
	 * @return
	 */
	public static String getDbNameStr(String appName, String dbkey) {
		return DB_NAME_FORMAT.format(new Object[] { appName, dbkey });
	}
}
