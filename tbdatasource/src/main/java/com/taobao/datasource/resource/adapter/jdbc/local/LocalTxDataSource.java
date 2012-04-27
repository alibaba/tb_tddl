/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.taobao.datasource.resource.adapter.jdbc.local;

import java.lang.management.ManagementFactory;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;

import com.taobao.datasource.resource.connectionmanager.CachedConnectionManager;
import com.taobao.datasource.resource.connectionmanager.JBossManagedConnectionPool;
import com.taobao.datasource.resource.connectionmanager.TxConnectionManager;
import com.taobao.datasource.resource.security.SecureIdentityLoginModule;

/**
 * This is a pojo that instantiates a Local tx connection pool. It provides same
 * functionality as ds.xml files
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 57189 $
 */
public class LocalTxDataSource{

    protected static Logger logger = Logger.getLogger(LocalTxDataSource.class);

    private static final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    private static final String POOL_OBJECT_NAME_FORMAT = "jboss.jca:name=%s,service=ManagedConnectionPool";

    public LocalTxDataSource() {
    }

    private CachedConnectionManager cachedConnectionManager;
    private TransactionManager transactionManager;

    private String name;
    
    private boolean useJmx=true;

    private LocalManagedConnectionFactory mcf = new LocalManagedConnectionFactory();
    private JBossManagedConnectionPool pool = new JBossManagedConnectionPool();

    private TxConnectionManager connectionManager;
    private DataSource datasource;

    public class ConnectionManagerDelegate implements ConnectionManager {
        private static final long serialVersionUID = 1L;

        public Object allocateConnection(ManagedConnectionFactory mcf, ConnectionRequestInfo cxRequestInfo)
                throws ResourceException {
            return connectionManager.allocateConnection(mcf, cxRequestInfo);
        }
    }

    public void init() throws Exception {
        initPool();
        initConnectionManager();
        pool.start();
        registerMBean();
        initDataSource();
    }

    private void registerMBean() throws Exception {
    	//如果没有设置JNDI的名字则不注册MBEAN
    	if(null!=name&&useJmx){
            ObjectName objectName = new ObjectName(String.format(POOL_OBJECT_NAME_FORMAT, name));
            server.registerMBean(pool, objectName);
    	}
    }

    private void initDataSource() throws ResourceException {
        datasource = (DataSource) mcf.createConnectionFactory(new ConnectionManagerDelegate());
    }

    private void initConnectionManager() {
        if (transactionManager == null) {
            throw new IllegalStateException("transactionManager is required");
        }
        connectionManager = new TxConnectionManager();
        connectionManager.setCachedConnectionManager(cachedConnectionManager);
        connectionManager.setTransactionManagerInstance(transactionManager);
        connectionManager.setName(name);
        connectionManager.setLocalTransactions(true);
        connectionManager.setTrackConnectionByTx(true);
    }

    private void initPool() {
        pool.setManagedConnectionFactory(mcf);
        pool.setName(name);
        pool.setDataSource(this);
    }

    public void destroy() throws Exception {
        pool.stop();
        unregisterMBean();
    }

    private void unregisterMBean() throws Exception {
    	//如果有设置JNDI的名字则注销MBEAN
    	if(null!=name&&useJmx){
    	      ObjectName objectName = new ObjectName(String.format(POOL_OBJECT_NAME_FORMAT, name));
    	      server.unregisterMBean(objectName);
    	}
    }
    
    public void flush(){
    	pool.flush();
    }
    
    public void setBeanName(String beanName) {
        this.name = beanName;
    }
    
    public String getBeanName() {
    	return this.name;
    }
    
    public void start() throws Exception {
        pool.startService();
        connectionManager.setPoolingStrategy(pool.getPoolingStrategy());
        connectionManager.startService();
        if (logger.isDebugEnabled()) {
            logger.debug("Connection pool " + name + " is started");
        }
    }

    public void stop() throws Exception {
        connectionManager.stopService();
        pool.stopService();

        if (logger.isDebugEnabled()) {
            logger.debug("Connection pool " + name + " is stopped");
        }
    }

    public DataSource getDatasource() {
        return datasource;
    }

    public void setCachedConnectionManager(CachedConnectionManager cachedConnectionManager) {
        this.cachedConnectionManager = cachedConnectionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public int getMinSize() {
        return pool.getMinSize();
    }

    public void setMinSize(int minSize) {
        pool.setMinSize(minSize);
    }

    public int getMaxSize() {
        return pool.getMaxSize();
    }

    public void setMaxSize(int maxSize) {
        pool.setMaxSize(maxSize);
    }

    public int getBlockingTimeoutMillis() {
        return pool.getBlockingTimeoutMillis();
    }

    public void setBlockingTimeoutMillis(int blockingTimeout) {
        pool.setBlockingTimeoutMillis(blockingTimeout);
    }

    public long getIdleTimeoutMinutes() {
        return pool.getIdleTimeoutMinutes();
    }

    public void setIdleTimeoutMinutes(long idleTimeout) {
        pool.setIdleTimeoutMinutes(idleTimeout);
    }

    public boolean getBackgroundValidation() {
        return pool.getBackGroundValidation();
    }

    public void setBackgroundValidation(boolean backgroundValidation) {
        pool.setBackGroundValidation(backgroundValidation);
    }

    public long getBackGroundValidationMinutes() {
        return pool.getBackGroundValidationMinutes();
    }

    public void setBackGroundValidationMinutes(long backgroundInterval) {
        pool.setBackGroundValidationMinutes(backgroundInterval);
    }

    public boolean getPrefill() {
        return pool.getPreFill();
    }

    public void setPrefill(boolean prefill) {
        pool.setPreFill(prefill);
    }

    public boolean getUseFastFail() {
        return pool.getUseFastFail();
    }

    public void setUseFastFail(boolean useFastFail) {
        pool.setUseFastFail(useFastFail);
    }

    public String getDriverClass() {
        return mcf.getDriverClass();
    }

    public void setDriverClass(final String driverClass) {
        mcf.setDriverClass(driverClass);
    }

    public String getConnectionURL() {
        return mcf.getConnectionURL();
    }

    public void setConnectionURL(final String connectionURL) {
        mcf.setConnectionURL(connectionURL);
    }

    public void setUserName(final String userName) {
        mcf.setUserName(userName);
    }
    
    public String getUserName() {
       return  mcf.getUserName();
    }

    public void setPassword(final String password) {
        mcf.setPassword(password);
    }
  
    public String getPassword() {
       return  mcf.getPassword();
    }
    
    public void setSecurityDomain(final SecureIdentityLoginModule securityDomain) throws Exception {
        mcf.setUserName(securityDomain.getUserName());
        mcf.setPassword(securityDomain.getDecodedPassword());
    }

    public void setPreparedStatementCacheSize(int size) {
        mcf.setPreparedStatementCacheSize(size);
    }

    public int getPreparedStatementCacheSize() {
        return mcf.getPreparedStatementCacheSize();
    }

    public boolean getSharePreparedStatements() {
        return mcf.getSharePreparedStatements();
    }

    public void setSharePreparedStatements(boolean sharePS) {
        mcf.setSharePreparedStatements(sharePS);
    }

    public boolean getTxQueryTimeout() {
        return mcf.isTransactionQueryTimeout();
    }

    public void setTxQueryTimeout(boolean qt) {
        mcf.setTransactionQueryTimeout(qt);
    }

    public int getQueryTimeout() {
        return mcf.getQueryTimeout();
    }

    public void setQueryTimeout(int queryTimeout) {
        mcf.setQueryTimeout(queryTimeout);
    }

    public String getTransactionIsolation() {
        return mcf.getTransactionIsolation();
    }

    public void setTransactionIsolation(String transactionIsolation) {
        mcf.setTransactionIsolation(transactionIsolation);
    }

    public String getNewConnectionSQL() {
        return mcf.getNewConnectionSQL();
    }

    public void setNewConnectionSQL(String newConnectionSQL) {
        mcf.setNewConnectionSQL(newConnectionSQL);
    }

    public String getCheckValidConnectionSQL() {
        return mcf.getCheckValidConnectionSQL();
    }

    public void setCheckValidConnectionSQL(String checkValidConnectionSQL) {
        mcf.setCheckValidConnectionSQL(checkValidConnectionSQL);
    }

    public String getTrackStatements() {
        return mcf.getTrackStatements();
    }

    public void setTrackStatements(String value) {
        mcf.setTrackStatements(value);
    }

    public String getExceptionSorterClassName() {
        return mcf.getExceptionSorterClassName();
    }

    public void setExceptionSorterClassName(String exceptionSorterClassName) {
        mcf.setExceptionSorterClassName(exceptionSorterClassName);
    }

    public String getValidConnectionCheckerClassName() {
        return mcf.getValidConnectionCheckerClassName();
    }

    public void setValidConnectionCheckerClassName(String value) {
        mcf.setValidConnectionCheckerClassName(value);
    }

    public boolean getValidateOnMatch() {
        return mcf.getValidateOnMatch();
    }

    public void setValidateOnMatch(boolean validateOnMatch) {
        mcf.setValidateOnMatch(validateOnMatch);
    }

    public String getConnectionPropertiesString() {
        return mcf.getConnectionProperties();
    }

    public void setConnectionPropertiesString(String connectionProperties) {
        mcf.setConnectionProperties(connectionProperties);
    }

    public void setConnectionProperties(Map<String, String> connectionProperties) {
        mcf.setConnectionProperties(connectionProperties);
    }

    public void setCriteria(String criteria) {
        pool.setCriteria(criteria);
    }

    public String getCriteria() {
        return pool.getCriteria();
    }

    public boolean getNoTxSeparatePools() {
    	return pool.getNoTxSeparatePools();
    }
 
    public void setNoTxSeparatePools(boolean noTxSeparatePools) {
        pool.setNoTxSeparatePools(noTxSeparatePools);
    }

    public boolean isNoTxSeparatePools() {
        return pool.getNoTxSeparatePools();
    }
    
    public boolean isUseJmx() {
		return useJmx;
	}

	public void setUseJmx(boolean useJmx) {
		this.useJmx = useJmx;
	}

	public String toString() {
        final String TAB = ", ";
        StringBuilder sb=new StringBuilder(); 
        sb.append("LocalTxDataSource(").append(super.toString()).append(TAB)
        .append("beanName = ").append(this.getBeanName()).append(TAB)
        .append("connectionURL = ").append(this.getConnectionURL()).append(TAB)
        .append("driverClass = " ).append(this.getDriverClass()).append(TAB)
        .append("userName = " ).append(this.getUserName()).append(TAB)
        .append("password = ").append(this.getPassword()).append(TAB)
        .append("maxSize = ").append(this.getMaxSize()).append(TAB)
        .append("minSize = ").append(this.getMinSize()).append(TAB)
        .append("preparedStatementCacheSize = ").append(this.getPreparedStatementCacheSize()).append(TAB)
        .append("connectionProperties = ").append(this.getConnectionPropertiesString()).append(TAB)
        .append("exceptionSorterClassName = " ).append(this.getExceptionSorterClassName()).append(TAB)
        .append("txQueryTimeout = ").append(this.getTxQueryTimeout()).append(TAB)
        .append("queryTimeout = ").append(this.getQueryTimeout()).append(TAB)
        .append("transactionIsolation = ").append(this.getTransactionIsolation()).append(TAB)
        .append("blockingTimeoutMillis = ").append(this.getBlockingTimeoutMillis()).append(TAB)
        .append("backgroundValidation = ").append(this.getBackgroundValidation()).append(TAB)
        .append( "idleTimeoutMinutes = ").append(this.getIdleTimeoutMinutes()).append(TAB)
        .append("validateOnMatch = ").append(this.getValidateOnMatch()).append(TAB)
        .append("checkValidConnectionSQL = ").append(this.getCheckValidConnectionSQL()).append(TAB)
        .append("validConnectionCheckerClassName = ").append(this.getValidConnectionCheckerClassName()).append(TAB)
        .append("trackStatements = ").append(this.getTrackStatements()).append(TAB)
        .append("prefill = " ).append(this.getPrefill()).append(TAB)
        .append("useFastFail = ").append(this.getUseFastFail()).append(TAB)
        .append("sharePreparedStatements = ").append(this.getSharePreparedStatements()).append(TAB)
        .append("newConnectionSQL = ").append( this.getNewConnectionSQL()).append(TAB)
        .append("noTxSeparatePools = ").append( this.getNoTxSeparatePools() ).append(TAB)
        .append("backgroundValidationMinutes = ").append(this.getBackGroundValidationMinutes()).append(")");
        return sb.toString();
    }
}
