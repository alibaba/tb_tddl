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

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnection;
import javax.transaction.SystemException;

/**
 * A jboss connection listener
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 *
 * @version $Revision: 57189 $
 */
public interface ConnectionListener extends ConnectionEventListener
{
   /** Normal state */
   public static final int NORMAL = 0;

   /** Destroy this connection */
   public static final int DESTROY = 1;

   /** This connection has been destroyed */
   public static final int DESTROYED = 2;

   /**
    * Retrieve the managed connection for this listener
    *
    * @return the managed connection
    */
   ManagedConnection getManagedConnection();

   /**
    * Retrieve the managed connection pool for this listener
    *
    * @return the managed connection pool
    */
   ManagedConnectionPool getManagedConnectionPool();

   /**
    * Tidyup<p>
    *
    * Invoked just before returning the connection to the pool
    * when the connection is not being destroyed
    *
    * @throws ResourceException for any error
    */
   void tidyup() throws ResourceException;

   /**
    * Retrieve the context used by the pool
    *
    * @return the context
    */
   Object getContext();

   /**
    * Retrieve the state of this connection
    *
    * @return the state
    */
   int getState();

   /**
    * Set the state of this connection
    */
   void setState(int newState);

   /**
    * Has the connection timed out?
    *
    * @param timeout the timeout
    * @return true for timed out, false otherwise
    */
   boolean isTimedOut(long timeout);

   /**
    * Mark the connection as used
    */
   void used();

   /**
    * Register a new connection
    *
    * @param handle the connection handle
    */
   void registerConnection(Object handle);

   /**
    * Unregister a connection
    *
    * @param handle the connection handle
    */
   void unregisterConnection(Object handle);

   /**
    * Is the managed connection free?
    *
    * @return true when it is free
    */
   boolean isManagedConnectionFree();

   /**
    * Enlist the managed connection
    */
   void enlist() throws SystemException;

   /**
    * Delist the managed connection
    */
   void delist() throws ResourceException;

   /**
    * Get whether the listener is track by transaction
    *
    * @return true for track by transaction, false otherwise
    */
   boolean isTrackByTx();

   /**
    * Set whether the listener is track by transaction
    *
    * @param trackByTx true for track by transaction, false otherwise
    */
   void setTrackByTx(boolean trackByTx);

   /**
    * Whether the connection has a permit
    *
    * @return true when it has permit, false otherwise
    */
   boolean hasPermit();

   /**
    * Tell the connection listener whether it owns the permit.
    *
    * @param value true for owning the permit, false otherwise
    */
   void grantPermit(boolean value);


   /**
    * Retrieve the last time this connection was validated.
    *
    * @return the last time the connection was validated
    */
   long getLastValidatedTime();


   /**
    * Set the last time, in milliseconds, that this connection was validated.
    *
    * @param interval the last time the connection was validated in milliseconds.
    */
   void setLastValidatedTime(long interval);

}
