/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom;

import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.datasource.LocalTxDataSourceDO;
import com.taobao.datasource.TaobaoDataSourceFactory;
import com.taobao.datasource.resource.adapter.jdbc.local.LocalTxDataSource;
import com.taobao.tddl.jdbc.atom.config.object.TAtomDsConfDO;
import com.taobao.tddl.jdbc.atom.exception.AtomAlreadyInitException;

/**
 * 静态剥离的jboss数据源，不支持动态改参数
 * 主要用来方便测试
 * @author qihao
 *
 */
public class StaticTAtomDataSource extends AbstractTAtomDataSource {

	private static Log logger = LogFactory.getLog(StaticTAtomDataSource.class);
	/**
	 * 数据源配置信息
	 */
	private TAtomDsConfDO confDO = new TAtomDsConfDO();

	/**
	 * Jboss数据源通过init初始化
	 */
	private LocalTxDataSource jbossDataSource;

	private volatile boolean init;

	public void init() throws Exception {
		if (init) {
			throw new AtomAlreadyInitException("[AlreadyInit] double call Init !");
		}
		LocalTxDataSourceDO localTxDataSourceDO = TAtomDsConfHandle.convertTAtomDsConf2JbossConf(confDO, confDO
				.getDbName());
		boolean checkPram = TAtomDsConfHandle.checkLocalTxDataSourceDO(localTxDataSourceDO);
		if (checkPram) {
			jbossDataSource = TaobaoDataSourceFactory.createLocalTxDataSource(localTxDataSourceDO);
			init = true;
		} else {
			throw new Exception("Init DataSource Error Pleace Check!");
		}
	}

	public void destroyDataSource() throws Exception {
		if (null != this.jbossDataSource) {
			logger.warn("[DataSource Stop] Start!");
			this.jbossDataSource.destroy();
			logger.warn("[DataSource Stop] End!");
		}
	}

	public void flushDataSource() {
		if (null != this.jbossDataSource) {
			logger.warn("[DataSource Flush] Start!");
			this.jbossDataSource.flush();
			logger.warn("[DataSource Flush] End!");
		}
	}

	protected DataSource getDataSource() throws SQLException {
		return jbossDataSource.getDatasource();
	}

	public String getIp() {
		return confDO.getIp();
	}

	public void setIp(String ip) {
		this.confDO.setIp(ip);
	}

	public String getPort() {
		return this.confDO.getPort();
	}

	public void setPort(String port) {
		this.confDO.setPort(port);
	}

	public String getDbName() {
		return this.confDO.getDbName();
	}

	public void setDbName(String dbName) {
		this.confDO.setDbName(dbName);
	}

	public String getUserName() {
		return this.confDO.getUserName();
	}

	public void setUserName(String userName) {
		this.confDO.setUserName(userName);
	}

	public String getPasswd() {
		return this.confDO.getPasswd();
	}

	public void setPasswd(String passwd) {
		this.confDO.setPasswd(passwd);
	}

	public String getDriverClass() {
		return this.confDO.getDriverClass();
	}

	public void setDriverClass(String driverClass) {
		this.confDO.setDriverClass(driverClass);
	}

	public String getSorterClass() {
		return this.confDO.getSorterClass();
	}

	public void setSorterClass(String sorterClass) {
		this.confDO.setSorterClass(sorterClass);
	}

	public int getPreparedStatementCacheSize() {
		return this.confDO.getPreparedStatementCacheSize();
	}

	public void setPreparedStatementCacheSize(int preparedStatementCacheSize) {
		this.confDO.setPreparedStatementCacheSize(preparedStatementCacheSize);
	}

	public int getMinPoolSize() {
		return this.confDO.getMinPoolSize();
	}

	public void setMinPoolSize(int minPoolSize) {
		this.confDO.setMinPoolSize(minPoolSize);
	}

	public int getMaxPoolSize() {
		return this.confDO.getMaxPoolSize();
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.confDO.setMaxPoolSize(maxPoolSize);
	}

	public int getBlockingTimeout() {
		return this.confDO.getBlockingTimeout();
	}

	public void setBlockingTimeout(int blockingTimeout) {
		this.confDO.setBlockingTimeout(blockingTimeout);
	}

	public long getIdleTimeout() {
		return this.confDO.getIdleTimeout();
	}

	public void setIdleTimeout(long idleTimeout) {
		this.confDO.setIdleTimeout(idleTimeout);
	}

	public String getDbType() {
		return this.confDO.getDbType();
	}

	public void setDbType(String dbType) {
		this.confDO.setDbType(dbType);
	}

	public String getOracleConType() {
		return this.confDO.getOracleConType();
	}

	public void setOracleConType(String oracleConType) {
		this.confDO.setOracleConType(oracleConType);
	}

	public Map<String, String> getConnectionProperties() {
		return this.confDO.getConnectionProperties();
	}

	public void setConnectionProperties(Map<String, String> connectionProperties) {
		this.confDO.setConnectionProperties(connectionProperties);
	}

	
}
