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
package com.taobao.datasource.resource.connectionmanager;

/**
 * A JBossManagedConnectionPoolMBean.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 *
 * @version $Revision: 59880 $
 */
public interface JBossManagedConnectionPoolMBean {

    public static final String STOPPED = "Stopped";

    public static final String STOPPING = "Stopping";

    public static final String STARTED = "Started";

    public static final String STARTING = "Starting";

    /**
     * start the service, create is already called
     */
    public void start() throws Exception;

    /**
     * stop the service
     */
    public void stop() throws Exception;

    public String getState();

    /**
     * Get number of available free connections
     *
     * @return number of available connections
     */
    long getAvailableConnectionCount();

    long getMaxConnectionsInUseCount();

    /**
     * Get number of connections currently in use
     *
     * @return number of connections currently in use
     */
    long getInUseConnectionCount();

    /**
     * The MinSize attribute indicates the minimum number of connections this
     * pool should hold. These are not created until a Subject is known from a
     * request for a connection. MinSize connections will be created for each
     * sub-pool.
     *
     * @return the MinSize value.
     */
    int getMinSize();

    /**
     * Set the MinSize value.
     *
     * @param newMinSize The new MinSize value.
     */
    void setMinSize(int newMinSize);

    /**
     * The MaxSize attribute indicates the maximum number of connections for a
     * pool. No more than MaxSize connections will be created in each sub-pool.
     *
     * @return the MaxSize value.
     */
    int getMaxSize();

    /**
     * Set the MaxSize value.
     *
     * @param newMaxSize The new MaxSize value.
     */
    void setMaxSize(int newMaxSize);

    /**
     * The BlockingTimeoutMillis attribute indicates the maximum time to block
     * while waiting for a connection before throwing an exception. Note that
     * this blocks only while waiting for a permit for a connection, and will
     * never throw an exception if creating a new connection takes an
     * inordinately long time.
     *
     * @return the BlockingTimeout value.
     */
    int getBlockingTimeoutMillis();

    /**
     * Set the BlockingTimeout value.
     *
     * @param newBlockingTimeout The new BlockingTimeout value.
     */
    void setBlockingTimeoutMillis(int newBlockingTimeout);

    /**
     * The IdleTimeoutMinutes attribute indicates the maximum time a connection
     * may be idle before being closed. The actual maximum time depends also on
     * the IdleRemover scan time, which is 1/2 the smallest IdleTimeout of any
     * pool.
     *
     * @return the IdleTimeoutMinutes value.
     */
    long getIdleTimeoutMinutes();

    /**
     * Set the IdleTimeoutMinutes value.
     *
     * @param newIdleTimeoutMinutes The new IdleTimeoutMinutes value.
     */
    void setIdleTimeoutMinutes(long newIdleTimeoutMinutes);

    /**
     * The Criteria attribute indicates if Subject (from security domain) or app
     * supplied parameters (such as from getConnection(user, pw)) are used to
     * distinguish connections in the pool. Choices are
     * ByContainerAndApplication (use both), ByContainer (use Subject),
     * ByApplication (use app supplied params only), ByNothing (all connections
     * are equivalent, usually if adapter supports reauthentication)
     *
     * @return the Criteria value.
     */
    String getCriteria();

    /**
     * Set the Criteria value.
     *
     * @param newCriteria The new Criteria value.
     */
    void setCriteria(String newCriteria);

    /**
     * Separate pools for transactional use
     *
     * @return true when connections should have different pools for
     *         transactional and non-transaction use.
     */
    boolean getNoTxSeparatePools();

    void setNoTxSeparatePools(boolean value);

    /**
     * The <code>flush</code> method puts all currently checked out connections
     * on a list to be destroyed when returned and disposes of all current
     * pooled connections.
     */
    void flush();

    /**
     * Retrieve the connection count.
     *
     * @return the connection count
     */
    int getConnectionCount();

    /**
     * Retrieve the connection created count.
     *
     * @return the connection created count
     */
    int getConnectionCreatedCount();

    /**
     * Retrieve the destrooyed count.
     *
     * @return the destroyed count
     */
    int getConnectionDestroyedCount();

    /**
     * Get background validation
     *
     * @return true of false if background validation is enabled.
     */
    public boolean getBackGroundValidation();

    /**
     * Set the background validation
     *
     * @param backgroundValidation true or false if background validation is to
     *            be enabled
     */
    public void setBackGroundValidation(boolean backgroundValidation);

    /**
     * Set the background validation in minutes
     *
     * @param backgroundValidationInterval the background interval in minutes
     */
    public void setBackGroundValidationMinutes(long backgroundValidationInterval);

    /**
     * Get the background validation in minutes
     *
     * @return the background validation in minutes
     */
    public long getBackGroundValidationMinutes();

    /**
     * Get prefill
     *
     * @return true or false depending upon prefill being set
     */
    public boolean getPreFill();

    /**
     * Set the prefill
     *
     * @param prefill true or false depending upon if prefill is being used
     */
    public void setPreFill(boolean prefill);

    /**
     * Whether or not we want to immeadiately create a new connection when an
     * attempt to acquire a connection from the pool fails.
     *
     * @return true of false depending upon whether fast fail is being used.
     *
     */
    public boolean getUseFastFail();

    /**
     * Indicate whether or not we want to immeadiately create a new connection
     * when an attempt to acquire a connection from the pool fails.
     *
     *
     * @param useFastFail whether or not we want to use fast fail semantics in a
     *            connection attempt.
     */
    public void setUseFastFail(boolean useFastFail);

}
