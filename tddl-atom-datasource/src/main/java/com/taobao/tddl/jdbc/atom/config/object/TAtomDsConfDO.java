/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.config.object;

import java.util.HashMap;import java.util.Map;import com.taobao.tddl.jdbc.atom.common.TAtomConstants;
/**
 * TAtom数据源全局和应用配置的DO
 * 
 * @author qihao
 * @author shenxun
 *
 */
public class TAtomDsConfDO implements Cloneable {

	private String ip;

	private String port;

	private String dbName;

	private String userName;

	private String passwd;

	private String driverClass;

	private String sorterClass;

	private int preparedStatementCacheSize;

	private int minPoolSize;

	private int maxPoolSize;

	private int blockingTimeout;

	private long idleTimeout;

	//private String dbType;

	private String oracleConType = TAtomConstants.DEFAULT_ORACLE_CON_TYPE;

	private AtomDbTypeEnum dbTypeEnum;

	private AtomDbStatusEnum dbStautsEnum;

	private String dbStatus;

	private Map<String, String> connectionProperties = new HashMap<String, String>();

	/**
	 * 写 次数限制
	 */
	private int writeRestrictTimes;

	/**
	 * 读 次数限制
	 */
	private int readRestrictTimes;

	/**
	 * 统计时间片 
	 */
	private int timeSliceInMillis;

	/**
	 * 线程技术count限制
	 */
	private int threadCountRestrict;

	/**
	 * 允许并发读的最大个数，0为不限制
	 */
	private int maxConcurrentReadRestrict;

	/**
	 * 允许并发写的最大个数，0为不限制
	 */
	private int maxConcurrentWriteRestrict;
	
	private volatile boolean isSingleInGroup;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getDriverClass() {
		if ((driverClass==null||"".equals(driverClass.trim())) && null != this.dbTypeEnum) {
			return this.dbTypeEnum.getDriverClass();
		}
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getSorterClass() {
		if ((sorterClass==null||"".equals(sorterClass.trim())) && null != this.dbTypeEnum) {
			return this.dbTypeEnum.getSorterClass();
		}
		return sorterClass;
	}

	public void setSorterClass(String sorterClass) {
		this.sorterClass = sorterClass;
	}

	public int getPreparedStatementCacheSize() {
		return preparedStatementCacheSize;
	}

	public void setPreparedStatementCacheSize(int preparedStatementCacheSize) {
		this.preparedStatementCacheSize = preparedStatementCacheSize;
	}

	public int getMinPoolSize() {
		return minPoolSize;
	}

	public void setMinPoolSize(int minPoolSize) {
		this.minPoolSize = minPoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getBlockingTimeout() {
		return blockingTimeout;
	}

	public void setBlockingTimeout(int blockingTimeout) {
		this.blockingTimeout = blockingTimeout;
	}

	public long getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(long idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public Map<String, String> getConnectionProperties() {
		return connectionProperties;
	}

	public String getDbType() {
		return dbTypeEnum.name().toLowerCase();
	}

	public void setDbType(String dbType) {
		this.dbTypeEnum = AtomDbTypeEnum.getAtomDbTypeEnumByType(dbType);
	}

	public String getDbStatus() {
		return dbStatus;
	}

	public void setDbStatus(String dbStatus) {
		this.dbStatus = dbStatus;
		if (dbStatus!=null&&!"".equals(dbStatus.trim())){
			this.dbStautsEnum = AtomDbStatusEnum.getAtomDbStatusEnumByType(dbStatus);
		}
	}

	public AtomDbStatusEnum getDbStautsEnum() {
		return dbStautsEnum;
	}

	public AtomDbTypeEnum getDbTypeEnum() {
		return dbTypeEnum;
	}

	public void setConnectionProperties(Map<String, String> connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	public String getOracleConType() {
		return oracleConType;
	}

	public void setOracleConType(String oracleConType) {
		this.oracleConType = oracleConType;
	}

	public int getWriteRestrictTimes() {
		return writeRestrictTimes;
	}

	public void setWriteRestrictTimes(int writeRestrictTimes) {
		this.writeRestrictTimes = writeRestrictTimes;
	}

	public int getReadRestrictTimes() {
		return readRestrictTimes;
	}

	public void setReadRestrictTimes(int readRestrictTimes) {
		this.readRestrictTimes = readRestrictTimes;
	}

	public int getThreadCountRestrict() {
		return threadCountRestrict;
	}

	public void setThreadCountRestrict(int threadCountRestrict) {
		this.threadCountRestrict = threadCountRestrict;
	}

	public int getTimeSliceInMillis() {
		return timeSliceInMillis;
	}

	public void setTimeSliceInMillis(int timeSliceInMillis) {
		this.timeSliceInMillis = timeSliceInMillis;
	}

	public int getMaxConcurrentReadRestrict() {
		return maxConcurrentReadRestrict;
	}

	public void setMaxConcurrentReadRestrict(int maxConcurrentReadRestrict) {
		this.maxConcurrentReadRestrict = maxConcurrentReadRestrict;
	}

	public int getMaxConcurrentWriteRestrict() {
		return maxConcurrentWriteRestrict;
	}

	public void setMaxConcurrentWriteRestrict(int maxConcurrentWriteRestrict) {
		this.maxConcurrentWriteRestrict = maxConcurrentWriteRestrict;
	}

	public TAtomDsConfDO clone() {
		TAtomDsConfDO tAtomDsConfDO = null;
		try {
			tAtomDsConfDO = (TAtomDsConfDO) super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return tAtomDsConfDO;
	}

	public boolean isSingleInGroup() {
		return isSingleInGroup;
	}

	public void setSingleInGroup(boolean isSingleInGroup) {
		this.isSingleInGroup = isSingleInGroup;
	}
}
