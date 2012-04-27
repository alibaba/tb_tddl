/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.datasource;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.taobao.datasource.resource.security.SecureIdentityLoginModule;

public class LocalTxDataSourceDO  implements Cloneable{

	private String jndiName;

    private String connectionURL;

    private String driverClass;

    private String transactionIsolation="-1";

    private Map<String, String> connectionProperties = new HashMap<String, String>();

    private String userName;

    private String password;
    
    private String encPassword;

    private String securityDomain;

    private int minPoolSize;

    private int maxPoolSize;

    private int blockingTimeoutMillis;

    private boolean backgroundValidation;

    private long idleTimeoutMinutes;

    private boolean validateOnMatch;

    private String checkValidConnectionSQL;

    private String validConnectionCheckerClassName;

    private String exceptionSorterClassName;

    private String trackStatements="nowarn";

    private boolean prefill;

    private boolean useFastFail;

    private int preparedStatementCacheSize;

    private boolean sharePreparedStatements;

    private String newConnectionSQL;

    private boolean noTxSeparatePools;

    private boolean txQueryTimeout;

    private int queryTimeout;

    private long backgroundValidationMinutes;
    
    private boolean useJmx=true;
    
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public void setBackgroundValidation(boolean backgroundValidation) {
        this.backgroundValidation = backgroundValidation;
    }

    public void setBackgroundValidationMinutes(long backgroundValidationMinutes) {
        this.backgroundValidationMinutes = backgroundValidationMinutes;
    }

    public void setBlockingTimeoutMillis(int blockingTimeoutMillis) {
        this.blockingTimeoutMillis = blockingTimeoutMillis;
    }

    public void setCheckValidConnectionSQL(String checkValidConnectionSQL) {
        this.checkValidConnectionSQL = checkValidConnectionSQL;
    }

    public void addConnectionProperty(String name, String value) {
        this.connectionProperties.put(name, value);
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public void setExceptionSorterClassName(String exceptionSorterClassName) {
        this.exceptionSorterClassName = exceptionSorterClassName;
    }

    public void setIdleTimeoutMinutes(long idleTimeoutMinutes) {
        this.idleTimeoutMinutes = idleTimeoutMinutes;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public void setNewConnectionSQL(String newConnectionSQL) {
        this.newConnectionSQL = newConnectionSQL;
    }

    public void setNoTxSeparatePools(boolean noTxSeparatePools) {
        this.noTxSeparatePools = noTxSeparatePools;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPrefill(boolean prefill) {
        this.prefill = prefill;
    }

    public void setPreparedStatementCacheSize(int preparedStatementCacheSize) {
        this.preparedStatementCacheSize = preparedStatementCacheSize;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void setSecurityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
    }

    public void setSharePreparedStatements(boolean sharePreparedStatements) {
        this.sharePreparedStatements = sharePreparedStatements;
    }

    public void setTrackStatements(String trackStatements) {
        this.trackStatements = trackStatements;
    }

    public void setTransactionIsolation(String transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

    public void setTxQueryTimeout(boolean txQueryTimeout) {
        this.txQueryTimeout = txQueryTimeout;
    }

    public void setUseFastFail(boolean useFastFail) {
        this.useFastFail = useFastFail;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setValidateOnMatch(boolean validateOnMatch) {
        this.validateOnMatch = validateOnMatch;
    }

    public void setValidConnectionCheckerClassName(String validConnectionCheckerClassName) {
        this.validConnectionCheckerClassName = validConnectionCheckerClassName;
    }

    public String getJndiName() {
        return jndiName;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public String getTransactionIsolation() {
        return transactionIsolation;
    }

    public Map<String, String> getConnectionProperties() {
        return connectionProperties;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getSecurityDomain() {
        return securityDomain;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getBlockingTimeoutMillis() {
        return blockingTimeoutMillis;
    }

    public boolean isBackgroundValidation() {
        return backgroundValidation;
    }

    public long getIdleTimeoutMinutes() {
        return idleTimeoutMinutes;
    }

    public boolean isValidateOnMatch() {
        return validateOnMatch;
    }

    public String getCheckValidConnectionSQL() {
        return checkValidConnectionSQL;
    }

    public String getValidConnectionCheckerClassName() {
        return validConnectionCheckerClassName;
    }

    public String getExceptionSorterClassName() {
        return exceptionSorterClassName;
    }

    public String getTrackStatements() {
        return trackStatements;
    }

    public boolean isPrefill() {
        return prefill;
    }

    public boolean isUseFastFail() {
        return useFastFail;
    }

    public int getPreparedStatementCacheSize() {
        return preparedStatementCacheSize;
    }

    public boolean isSharePreparedStatements() {
        return sharePreparedStatements;
    }

    public String getNewConnectionSQL() {
        return newConnectionSQL;
    }

    public boolean isNoTxSeparatePools() {
        return noTxSeparatePools;
    }

    public boolean isTxQueryTimeout() {
        return txQueryTimeout;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public String getCriteria() {
        if (StringUtils.isNotBlank(securityDomain)) {
            return "ByContainer";
        } else {
            return "ByNothing";
        }
    }

    public long getBackgroundValidationMinutes() {
        return backgroundValidationMinutes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (backgroundValidation ? 1231 : 1237);
        result = prime * result + (int) (backgroundValidationMinutes ^ (backgroundValidationMinutes >>> 32));
        result = prime * result + blockingTimeoutMillis;
        result = prime * result + ((checkValidConnectionSQL == null) ? 0 : checkValidConnectionSQL.hashCode());
        result = prime * result + ((connectionProperties == null) ? 0 : connectionProperties.hashCode());
        result = prime * result + ((connectionURL == null) ? 0 : connectionURL.hashCode());
        result = prime * result + ((driverClass == null) ? 0 : driverClass.hashCode());
        result = prime * result + ((exceptionSorterClassName == null) ? 0 : exceptionSorterClassName.hashCode());
        result = prime * result + (int) (idleTimeoutMinutes ^ (idleTimeoutMinutes >>> 32));
        result = prime * result + ((jndiName == null) ? 0 : jndiName.hashCode());
        result = prime * result + maxPoolSize;
        result = prime * result + minPoolSize;
        result = prime * result + ((newConnectionSQL == null) ? 0 : newConnectionSQL.hashCode());
        result = prime * result + (noTxSeparatePools ? 1231 : 1237);
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + (prefill ? 1231 : 1237);
        result = prime * result + preparedStatementCacheSize;
        result = prime * result + queryTimeout;
        result = prime * result + ((securityDomain == null) ? 0 : securityDomain.hashCode());
        result = prime * result + (sharePreparedStatements ? 1231 : 1237);
        result = prime * result + ((trackStatements == null) ? 0 : trackStatements.hashCode());
        result = prime * result + ((transactionIsolation == null) ? 0 : transactionIsolation.hashCode());
        result = prime * result + (txQueryTimeout ? 1231 : 1237);
        result = prime * result + (useFastFail ? 1231 : 1237);
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        result = prime * result
                + ((validConnectionCheckerClassName == null) ? 0 : validConnectionCheckerClassName.hashCode());
        result = prime * result + (validateOnMatch ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LocalTxDataSourceDO other = (LocalTxDataSourceDO) obj;
        if (backgroundValidation != other.backgroundValidation)
            return false;
        if (backgroundValidationMinutes != other.backgroundValidationMinutes)
            return false;
        if (blockingTimeoutMillis != other.blockingTimeoutMillis)
            return false;
        if (checkValidConnectionSQL == null) {
            if (other.checkValidConnectionSQL != null)
                return false;
        } else if (!checkValidConnectionSQL.equals(other.checkValidConnectionSQL))
            return false;
        if (connectionProperties == null) {
            if (other.connectionProperties != null)
                return false;
        } else if (!connectionProperties.equals(other.connectionProperties))
            return false;
        if (connectionURL == null) {
            if (other.connectionURL != null)
                return false;
        } else if (!connectionURL.equals(other.connectionURL))
            return false;
        if (driverClass == null) {
            if (other.driverClass != null)
                return false;
        } else if (!driverClass.equals(other.driverClass))
            return false;
        if (exceptionSorterClassName == null) {
            if (other.exceptionSorterClassName != null)
                return false;
        } else if (!exceptionSorterClassName.equals(other.exceptionSorterClassName))
            return false;
        if (idleTimeoutMinutes != other.idleTimeoutMinutes)
            return false;
        if (jndiName == null) {
            if (other.jndiName != null)
                return false;
        } else if (!jndiName.equals(other.jndiName))
            return false;
        if (maxPoolSize != other.maxPoolSize)
            return false;
        if (minPoolSize != other.minPoolSize)
            return false;
        if (newConnectionSQL == null) {
            if (other.newConnectionSQL != null)
                return false;
        } else if (!newConnectionSQL.equals(other.newConnectionSQL))
            return false;
        if (noTxSeparatePools != other.noTxSeparatePools)
            return false;
        if (password == null) {
            if (other.password != null)
                return false;
        } else if (!password.equals(other.password))
            return false;
        if (prefill != other.prefill)
            return false;
        if (preparedStatementCacheSize != other.preparedStatementCacheSize)
            return false;
        if (queryTimeout != other.queryTimeout)
            return false;
        if (securityDomain == null) {
            if (other.securityDomain != null)
                return false;
        } else if (!securityDomain.equals(other.securityDomain))
            return false;
        if (sharePreparedStatements != other.sharePreparedStatements)
            return false;
        if (trackStatements == null) {
            if (other.trackStatements != null)
                return false;
        } else if (!trackStatements.equals(other.trackStatements))
            return false;
        if (transactionIsolation == null) {
            if (other.transactionIsolation != null)
                return false;
        } else if (!transactionIsolation.equals(other.transactionIsolation))
            return false;
        if (txQueryTimeout != other.txQueryTimeout)
            return false;
        if (useFastFail != other.useFastFail)
            return false;
        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        if (validConnectionCheckerClassName == null) {
            if (other.validConnectionCheckerClassName != null)
                return false;
        } else if (!validConnectionCheckerClassName.equals(other.validConnectionCheckerClassName))
            return false;
        if (validateOnMatch != other.validateOnMatch)
            return false;
        return true;
    }
    
	public boolean isUseJmx() {
		return useJmx;
	}

	public void setUseJmx(boolean useJmx) {
		this.useJmx = useJmx;
	}

	public String getEncPassword() {
		return encPassword;
	}

	public void setEncPassword(String encPassword) throws Exception {
		if(StringUtils.isNotBlank(encPassword)){
			this.password=new String(SecureIdentityLoginModule.decode(encPassword));
			this.encPassword = encPassword;
		}
	}

	/**
     * Constructs a <code>String</code> with all attributes in name = value
     * format.
     *
     * @return a <code>String</code> representation of this object.
     */
    public String toString() {
        final String TAB = ", ";
        StringBuilder sb=new StringBuilder(); 
        sb.append("LocalTxDataSourceDO(").append(super.toString()).append(TAB)
        .append("jndiName = ").append(this.jndiName).append(TAB)
        .append("connectionURL = ").append(this.connectionURL).append(TAB)
        .append("driverClass = " ).append(this.driverClass).append(TAB)
        .append("userName = " ).append(this.userName).append(TAB)
        .append("encPassword = ").append(this.encPassword).append(TAB)
        .append("maxPoolSize = ").append(this.maxPoolSize).append(TAB)
        .append("minPoolSize = ").append(this.minPoolSize).append(TAB)
        .append("securityDomain = " ).append(this.securityDomain).append(TAB)
        .append("preparedStatementCacheSize = ").append(this.preparedStatementCacheSize).append(TAB)
        .append("transactionIsolation = ").append(this.transactionIsolation).append(TAB)
        .append("connectionProperties = ").append(this.connectionProperties).append(TAB)
        .append("blockingTimeoutMillis = ").append(this.blockingTimeoutMillis).append(TAB)
        .append("backgroundValidation = ").append(this.backgroundValidation).append(TAB)
        .append( "idleTimeoutMinutes = ").append(this.idleTimeoutMinutes).append(TAB)
        .append("validateOnMatch = ").append(this.validateOnMatch).append(TAB)
        .append("checkValidConnectionSQL = ").append(this.checkValidConnectionSQL).append(TAB)
        .append("validConnectionCheckerClassName = ").append(this.validConnectionCheckerClassName).append(TAB)
        .append("exceptionSorterClassName = " ).append(this.exceptionSorterClassName).append(TAB)
        .append("trackStatements = ").append(this.trackStatements).append(TAB)
        .append("prefill = " ).append(this.prefill).append(TAB)
        .append("useFastFail = ").append(this.useFastFail).append(TAB)
        .append("sharePreparedStatements = ").append(this.sharePreparedStatements).append(TAB)
        .append("newConnectionSQL = ").append( this.newConnectionSQL).append(TAB)
        .append("noTxSeparatePools = ").append( this.noTxSeparatePools ).append(TAB)
        .append("txQueryTimeout = ").append(this.txQueryTimeout).append(TAB)
        .append("queryTimeout = ").append(this.queryTimeout).append(TAB)
        .append("backgroundValidationMinutes = ").append(this.backgroundValidationMinutes).append(TAB)
        .append("useJmx = ").append(this.useJmx).append(")");
        return sb.toString();
    }

	public LocalTxDataSourceDO clone() {
		LocalTxDataSourceDO LocalTxDataSourceDO=null;
		try {
			 LocalTxDataSourceDO= (LocalTxDataSourceDO) super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return LocalTxDataSourceDO;
	}

	public void setConnectionProperties(Map<String, String> connectionProperties) {
		this.connectionProperties = connectionProperties;
	}
}
