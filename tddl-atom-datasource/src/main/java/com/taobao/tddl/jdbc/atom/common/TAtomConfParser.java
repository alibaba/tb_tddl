/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.common;

import java.io.ByteArrayInputStream;import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.datasource.resource.security.SecureIdentityLoginModule;
import com.taobao.tddl.common.util.TStringUtil;import com.taobao.tddl.jdbc.atom.config.object.TAtomDsConfDO;

/**
 * TAtom数据源的推送配置解析类
 * 
 * @author qihao
 *
 */
public class TAtomConfParser {
	private static Log logger = LogFactory.getLog(TAtomConfParser.class);

	public static final String GLOBA_IP_KEY = "ip";
	public static final String GLOBA_PORT_KEY = "port";
	public static final String GLOBA_DB_NAME_KEY = "dbName";
	public static final String GLOBA_DB_TYPE_KEY = "dbType";
	public static final String GLOBA_DB_STATUS_KEY = "dbStatus";
	public static final String APP_USER_NAME_KEY = "userName";
	public static final String APP_MIN_POOL_SIZE_KEY = "minPoolSize";
	public static final String APP_MAX_POOL_SIZE_KEY = "maxPoolSize";
	public static final String APP_IDLE_TIMEOUT_KEY = "idleTimeout";
	public static final String APP_BLOCKING_TIMEOUT_KEY = "blockingTimeout";
	public static final String APP_PREPARED_STATEMENT_CACHE_SIZE_KEY = "preparedStatementCacheSize";
	public static final String APP_ORACLE_CON_TYPE_KEY = "oracleConType";
	public static final String APP_CON_PROP_KEY = "connectionProperties";
	public static final String PASSWD_ENC_PASSWD_KEY = "encPasswd";
	public static final String PASSWD_ENC_KEY_KEY = "encKey";
	/**
	 * 写，次数限制
	 */
	public static final String APP_WRITE_RESTRICT_TIMES = "writeRestrictTimes";
	/**
	 * 读，次数限制
	 */
	public static final String APP_READ_RESTRICT_TIMES = "readRestrictTimes";
	/**
	 * thread count 次数限制
	 */
	public static final String APP_THREAD_COUNT_RESTRICT = "threadCountRestrict";
	
	public static final String APP_TIME_SLICE_IN_MILLS = "timeSliceInMillis";
	
	public static TAtomDsConfDO parserTAtomDsConfDO(String globaConfStr, String appConfStr) {
		TAtomDsConfDO pasObj = new TAtomDsConfDO();
		if (TStringUtil.isNotBlank(globaConfStr)) {
			Properties globaProp = TAtomConfParser.parserConfStr2Properties(globaConfStr);
			if (!globaProp.isEmpty()) {
				String ip = TStringUtil.trim(globaProp.getProperty(TAtomConfParser.GLOBA_IP_KEY));
				if (TStringUtil.isNotBlank(ip)) {
					pasObj.setIp(ip);
				}
				String port = TStringUtil.trim(globaProp.getProperty(TAtomConfParser.GLOBA_PORT_KEY));
				if (TStringUtil.isNotBlank(port)) {
					pasObj.setPort(port);
				}
				String dbName = TStringUtil.trim(globaProp.getProperty(TAtomConfParser.GLOBA_DB_NAME_KEY));
				if (TStringUtil.isNotBlank(dbName)) {
					pasObj.setDbName(dbName);
				}
				String dbType = TStringUtil.trim(globaProp.getProperty(TAtomConfParser.GLOBA_DB_TYPE_KEY));
				if (TStringUtil.isNotBlank(dbType)) {
					pasObj.setDbType(dbType);
				}
				String dbStatus = TStringUtil.trim(globaProp.getProperty(TAtomConfParser.GLOBA_DB_STATUS_KEY));
				if (TStringUtil.isNotBlank(dbStatus)) {
					pasObj.setDbStatus(dbStatus);
				}
			}
		}
		if (TStringUtil.isNotBlank(appConfStr)) {
			Properties appProp = TAtomConfParser.parserConfStr2Properties(appConfStr);
			if (!appProp.isEmpty()) {
				String userName = TStringUtil.trim(appProp.getProperty(TAtomConfParser.APP_USER_NAME_KEY));
				if (TStringUtil.isNotBlank(userName)) {
					pasObj.setUserName(userName);
				}
				String oracleConType = TStringUtil.trim(appProp.getProperty(TAtomConfParser.APP_ORACLE_CON_TYPE_KEY));
				if (TStringUtil.isNotBlank(oracleConType)) {
					pasObj.setOracleConType(oracleConType);
				}
				String minPoolSize = TStringUtil.trim(appProp.getProperty(TAtomConfParser.APP_MIN_POOL_SIZE_KEY));
				if (TStringUtil.isNotBlank(minPoolSize)&&TStringUtil.isNumeric(minPoolSize)) {
					pasObj.setMinPoolSize(Integer.valueOf(minPoolSize));
				}
				String maxPoolSize = TStringUtil.trim(appProp.getProperty(TAtomConfParser.APP_MAX_POOL_SIZE_KEY));
				if (TStringUtil.isNotBlank(maxPoolSize)&&TStringUtil.isNumeric(maxPoolSize)) {
					pasObj.setMaxPoolSize(Integer.valueOf(maxPoolSize));
				}
				String idleTimeout = TStringUtil.trim(appProp.getProperty(TAtomConfParser.APP_IDLE_TIMEOUT_KEY));
				if (TStringUtil.isNotBlank(idleTimeout)&&TStringUtil.isNumeric(idleTimeout)) {
					pasObj.setIdleTimeout(Long.valueOf(idleTimeout));
				}
				String blockingTimeout = TStringUtil.trim(appProp.getProperty(TAtomConfParser.APP_BLOCKING_TIMEOUT_KEY));
				if (TStringUtil.isNotBlank(blockingTimeout)&&TStringUtil.isNumeric(blockingTimeout)) {
					pasObj.setBlockingTimeout(Integer.valueOf(blockingTimeout));
				}
				String preparedStatementCacheSize = TStringUtil.trim(appProp
						.getProperty(TAtomConfParser.APP_PREPARED_STATEMENT_CACHE_SIZE_KEY));
				if (TStringUtil.isNotBlank(preparedStatementCacheSize)&&TStringUtil.isNumeric(preparedStatementCacheSize)) {
					pasObj.setPreparedStatementCacheSize(Integer.valueOf(preparedStatementCacheSize));
				}
				
				String writeRestrictTimes = TStringUtil.trim(appProp.getProperty(TAtomConfParser.APP_WRITE_RESTRICT_TIMES));
				if(TStringUtil.isNotBlank(writeRestrictTimes)&&TStringUtil.isNumeric(writeRestrictTimes)){
					pasObj.setWriteRestrictTimes(Integer.valueOf(writeRestrictTimes));
				}
				
				String readRestrictTimes = TStringUtil.trim(appProp.getProperty(TAtomConfParser.APP_READ_RESTRICT_TIMES));
				if(TStringUtil.isNotBlank(readRestrictTimes)&&TStringUtil.isNumeric(readRestrictTimes)){
					pasObj.setReadRestrictTimes(Integer.valueOf(readRestrictTimes));
				}
				String threadCountRestrict = TStringUtil.trim(appProp.getProperty(TAtomConfParser.APP_THREAD_COUNT_RESTRICT));
				if(TStringUtil.isNotBlank(threadCountRestrict)&&TStringUtil.isNumeric(threadCountRestrict)){
					pasObj.setThreadCountRestrict(Integer.valueOf(threadCountRestrict));
				}
				String timeSliceInMillis = TStringUtil.trim(appProp.getProperty(TAtomConfParser.APP_TIME_SLICE_IN_MILLS));
				if(TStringUtil.isNotBlank(timeSliceInMillis)&&TStringUtil.isNumeric(timeSliceInMillis)){
					pasObj.setTimeSliceInMillis(Integer.valueOf(timeSliceInMillis));
				}
				
				String conPropStr = TStringUtil.trim(appProp.getProperty(TAtomConfParser.APP_CON_PROP_KEY));
				Map<String, String> connectionProperties = parserConPropStr2Map(conPropStr);
				if (null != connectionProperties && !connectionProperties.isEmpty()) {
					pasObj.setConnectionProperties(connectionProperties);
				}
			}
		}
		return pasObj;
	}

	public static Map<String, String> parserConPropStr2Map(String conPropStr) {
		Map<String, String> connectionProperties = null;
		if (TStringUtil.isNotBlank(conPropStr)) {
			String[] keyValues = TStringUtil.splitm(conPropStr, ";");
			if (null != keyValues && keyValues.length > 0) {
				connectionProperties = new HashMap<String, String>(keyValues.length);
				for (String keyValue : keyValues) {
					String key = TStringUtil.substringBefore(keyValue, "=");
					String value = TStringUtil.substringAfter(keyValue, "=");
					if (TStringUtil.isNotBlank(key) && TStringUtil.isNotBlank(value)) {
						connectionProperties.put(key, value);
					}
				}
			}
		}
		return connectionProperties;
	}

	public static String parserPasswd(String passwdStr) {
		String passwd = null;
		Properties passwdProp = TAtomConfParser.parserConfStr2Properties(passwdStr);
		String encPasswd = passwdProp.getProperty(TAtomConfParser.PASSWD_ENC_PASSWD_KEY);
		if (TStringUtil.isNotBlank(encPasswd)) {
			String encKey = passwdProp.getProperty(TAtomConfParser.PASSWD_ENC_KEY_KEY);
			try {
				passwd = SecureIdentityLoginModule.decode(encKey, encPasswd);
			} catch (Exception e) {
				logger.error("[parserPasswd Error] decode dbPasswdError !", e);
			}
		}
		return passwd;
	}	
	private static Properties parserConfStr2Properties(String data) {
		Properties prop = new Properties();
		if (TStringUtil.isNotBlank(data)) {
			ByteArrayInputStream byteArrayInputStream = null;
			try {
				byteArrayInputStream = new ByteArrayInputStream((data).getBytes());
				prop.load(byteArrayInputStream);
			} catch (IOException e) {
				logger.error("parserConfStr2Properties Error", e);
			} finally {
				try {					byteArrayInputStream.close();				} catch (IOException e) {					logger.error("parserConfStr2Properties Error,can not close ByteArrayInputStream", e);				}
			}
		}
		return prop;
	}
}
