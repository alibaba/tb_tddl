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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.logging.Logger;
import org.jboss.util.Strings;

import com.taobao.datasource.tm.TxUtils;

/**
 * The CachedConnectionManager mbean manages associations between meta-aware objects
 * (those accessed through interceptor chains) and connection handles, and between
 *  user transactions and connection handles.  Normally there should only be one
 * such mbean.  It is called by CachedConnectionInterceptor, UserTransaction,
 * and all BaseConnectionManager2 instances.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:E.Guib@ceyoniq.com">Erwin Guib</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 65150 $
 */
public class CachedConnectionManager{
   private static final Logger log = Logger.getLogger(CachedConnectionManager.class);

   private boolean specCompliant;

   protected boolean trace;

   private boolean debug;

   protected boolean error;

   private TransactionManager tm;

   /**
    * ThreadLocal that holds current calling meta-programming aware
    * object, used in case someone is idiotic enough to cache a
    * connection between invocations.and want the spec required
    * behavior of it getting hooked up to an appropriate
    * ManagedConnection on each method invocation.
    */
   private final ThreadLocal currentObjects = new ThreadLocal();

   /**
    * The variable <code>objectToConnectionManagerMap</code> holds the
    * map of meta-aware object to set of connections it holds, used by
    * the idiot spec compliant behavior.
    */
   private final Map objectToConnectionManagerMap = new HashMap();

   /**
    * Connection stacktraces
    */
   private Map connectionStackTraces = new WeakHashMap();

   /**
    * Default CachedConnectionManager managed constructor for mbeans.
    * Remember that this mbean should be a singleton.
    *
    * @jmx.managed-constructor
    */
   public CachedConnectionManager()
   {
      super();
      trace = log.isTraceEnabled();
   }

   public boolean isSpecCompliant()
   {
      return specCompliant;
   }

   public void setSpecCompliant(boolean specCompliant)
   {
      if (specCompliant)
         log.warn("THE SpecCompliant ATTRIBUTE IS MISNAMED SEE http://jira.jboss.com/jira/browse/JBAS-1662");
      this.specCompliant = specCompliant;
   }

   public boolean isDebug()
   {
      return debug;
   }

   public void setDebug(boolean value)
   {
      this.debug = value;
   }

   public boolean isError()
   {
      return error;
   }

   public void setError(boolean value)
   {
      this.error = value;
   }

   public void setTransactionManager(TransactionManager transactionManager) {
       this.tm = transactionManager;
   }

   public TransactionManager getTransactionManager() {
       return tm;
   }

   public int getInUseConnections()
   {
      synchronized (connectionStackTraces)
      {
         return connectionStackTraces.size();
      }
   }

   public Map listInUseConnections()
   {
      synchronized (connectionStackTraces)
      {
         HashMap result = new HashMap();
         for (Iterator i = connectionStackTraces.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry) i.next();
            Throwable stackTrace = (Throwable) entry.getValue();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            stackTrace.printStackTrace(ps);
            result.put(entry.getKey().toString(), baos.toString());
         }
         return result;
      }
   }

    public void init() throws Exception {
        TransactionSynchronizer.setTransactionManager(tm);
        // TODO: 找出以下方法的替代方案
        // ServerVMClientUserTransaction.getSingleton().registerTxStartedListener(this);
        // EnterpriseContext.setUserTransactionStartedListener(this);
    }

    public void destroy() throws Exception {
        // TODO: 找出以下方法的替代方案
        // ServerVMClientUserTransaction.getSingleton().unregisterTxStartedListener(this);
        // EnterpriseContext.setUserTransactionStartedListener(null);
    }

   //Object registration for meta-aware objects (i.e. this is called by interceptors)

   /**
    * Describe <code>pushMetaAwareObject</code> method here.
    * PUBLIC for TESTING PURPOSES ONLY!
    *
    * @param rawKey an <code>Object</code> value
    * @param unsharableResources the unsharable resources
    * @exception ResourceException if an error occurs
    */
   public void pushMetaAwareObject(final Object rawKey, Set unsharableResources)
           throws ResourceException
   {
      LinkedList stack = (LinkedList) currentObjects.get();
      if (stack == null)
      {
         if (trace)
            log.trace("new stack for key: " + Strings.defaultToString(rawKey));
         stack = new LinkedList();
         currentObjects.set(stack);
      } // end of if ()
      else
      {
         if (trace)
            log.trace("old stack for key: " + Strings.defaultToString(rawKey));
         //At one time I attempted to recycle connections held over method calls.
         //This caused problems if the other method call started a new transaction.
         //To assure optimal use of connections, close them before calling out.
      } // end of else
      //check for reentrancy, reconnect if not reentrant.
      //wrap key to be based on == rather than equals
      KeyConnectionAssociation key = new KeyConnectionAssociation(rawKey);
      if (specCompliant && !stack.contains(key))
      {
         reconnect(key, unsharableResources);
      }
      stack.addLast(key);
   }

   /**
    * Describe <code>popMetaAwareObject</code> method here.
    * PUBLIC for TESTING PURPOSES ONLY!
    *
    * @exception ResourceException if an error occurs
    */
   public void popMetaAwareObject(Set unsharableResources)
           throws ResourceException
   {
      LinkedList stack = (LinkedList) currentObjects.get();
      KeyConnectionAssociation oldKey = (KeyConnectionAssociation) stack.removeLast();
      if (trace)
         log.trace("popped object: " + Strings.defaultToString(oldKey));
      if (specCompliant)
      {
         if (!stack.contains(oldKey))
         {
            disconnect(oldKey, unsharableResources);
         } // end of if ()
      }
      else if (debug)
      {
         if (closeAll(oldKey.getCMToConnectionsMap()) && error)
            throw new ResourceException("Some connections were not closed, see the log for the allocation stacktraces");
      }

      //At one time I attempted to recycle connections held over method calls.
      //This caused problems if the other method call started a new transaction.
      //To assure optimal use of connections, close them before calling out.
   }

   KeyConnectionAssociation peekMetaAwareObject()
   {
      LinkedList stack = (LinkedList) currentObjects.get();
      if (stack == null)
         return null;
      if (!stack.isEmpty())
         return (KeyConnectionAssociation) stack.getLast();
      else
         return null;
   }

   //ConnectionRegistration -- called by ConnectionCacheListeners (normally ConnectionManagers)

   void registerConnection(ConnectionCacheListener cm, ConnectionListener cl, Object connection, ConnectionRequestInfo cri)
   {
      if (debug)
      {
         synchronized (connectionStackTraces)
         {
            connectionStackTraces.put(connection, new Throwable("STACKTRACE"));
         }
      }

      KeyConnectionAssociation key = peekMetaAwareObject();
      if (trace)
         log.trace("registering connection from " + cm + ", connection : " + connection + ", key: " + key);
      if (key == null)
         return; //not participating properly in this management scheme.

      ConnectionRecord cr = new ConnectionRecord(cl, connection, cri);
      Map cmToConnectionsMap = key.getCMToConnectionsMap();
      Collection conns = (Collection) cmToConnectionsMap.get(cm);
      if (conns == null)
      {
         conns = new ArrayList();
         cmToConnectionsMap.put(cm, conns);
      }
      conns.add(cr);
   }

   void unregisterConnection(ConnectionCacheListener cm, Object c)
   {
      if (debug)
      {
         CloseConnectionSynchronization cas = getCloseConnectionSynchronization(false);
         if (cas != null)
            cas.remove(c);
         synchronized (connectionStackTraces)
         {
            connectionStackTraces.remove(c);
         }
      }

      KeyConnectionAssociation key = peekMetaAwareObject();
      if (trace)
         log.trace("unregistering connection from " + cm + ", object: " + c + ", key: " + key);
      if (key == null)
         return; //not participating properly in this management scheme.

      Map cmToConnectionsMap = key.getCMToConnectionsMap();
      Collection conns = (Collection) cmToConnectionsMap.get(cm);
      if (conns == null)
         return; // Can happen if connections are "passed" between contexts
      for (Iterator i = conns.iterator(); i.hasNext();)
      {
         if (((ConnectionRecord) i.next()).connection == c)
         {
            i.remove();
            return;
         }
      }
      throw new IllegalStateException("Trying to return an unknown connection2! " + c);
   }

   // FIXME: UserTransaction开始时需要调用这个方法
   //called by UserTransaction after starting a transaction
   public void userTransactionStarted()
           throws SystemException
   {
      KeyConnectionAssociation key = peekMetaAwareObject();
      if (trace)
         log.trace("user tx started, key: " + key);
      if (key == null)
         return; //not participating properly in this management scheme.

      Map cmToConnectionsMap = key.getCMToConnectionsMap();
      for (Iterator i = cmToConnectionsMap.keySet().iterator(); i.hasNext();)
      {
         ConnectionCacheListener cm = (ConnectionCacheListener) i.next();
         Collection conns = (Collection) cmToConnectionsMap.get(cm);
         cm.transactionStarted(conns);
      }
   }

   /**
    * The <code>reconnect</code> method gets the cmToConnectionsMap
    * from objectToConnectionManagerMap, copies it to the key, and
    * reconnects all the connections in it.
    *
    * @param key a <code>KeyConnectionAssociation</code> value
    * @param unsharableResources a <code>Set</code> value
    * @exception ResourceException if an error occurs
    */
   private void reconnect(KeyConnectionAssociation key, Set unsharableResources)
           throws ResourceException
   {
      Map cmToConnectionsMap = null;
      synchronized (objectToConnectionManagerMap)
      {
         cmToConnectionsMap = (Map) objectToConnectionManagerMap.get(key);
         if (cmToConnectionsMap == null)
            return;
      }
      key.setCMToConnectionsMap(cmToConnectionsMap);
      for (Iterator i = cmToConnectionsMap.keySet().iterator(); i.hasNext();)
      {
         ConnectionCacheListener cm = (ConnectionCacheListener) i.next();
         Collection conns = (Collection) cmToConnectionsMap.get(cm);
         cm.reconnect(conns, unsharableResources);
      }
   }

   private void disconnect(KeyConnectionAssociation key, Set unsharableResources)
           throws ResourceException
   {
      Map cmToConnectionsMap = key.getCMToConnectionsMap();
      if (!cmToConnectionsMap.isEmpty())
      {
         synchronized (objectToConnectionManagerMap)
         {
            objectToConnectionManagerMap.put(key, cmToConnectionsMap);
         }
         for (Iterator i = cmToConnectionsMap.keySet().iterator(); i.hasNext();)
         {
            ConnectionCacheListener cm = (ConnectionCacheListener) i.next();
            Collection conns = (Collection) cmToConnectionsMap.get(cm);
            cm.disconnect(conns, unsharableResources);
         }
      }
   }

   private boolean closeAll(Map cmToConnectionsMap)
   {
      if (debug == false)
         return false;

      boolean unclosed = false;

      Collection connections = cmToConnectionsMap.values();
      if (connections.size() != 0)
      {
         for (Iterator i = connections.iterator(); i.hasNext();)
         {
            Collection conns = (Collection) i.next();
            for (Iterator j = conns.iterator(); j.hasNext();)
            {
               Object c = ((ConnectionRecord) j.next()).connection;
               CloseConnectionSynchronization cas = getCloseConnectionSynchronization(true);
               if (cas == null)
               {
                  unclosed = true;
                  closeConnection(c);
               }
               else
                  cas.add(c);
            }
         }
      }

      return unclosed;
   }

   /**
    * Describe <code>unregisterConnectionCacheListener</code> method here.
    * This is a shutdown method called by a connection manager.  It will remove all reference
    * to that connection manager from the cache, so cached connections from that manager
    * will never be recoverable.
    * Possibly this method should not exist.
    *
    * @param cm a <code>ConnectionCacheListener</code> value
    */
   void unregisterConnectionCacheListener(ConnectionCacheListener cm)
   {
      if (trace)
         log.trace("unregisterConnectionCacheListener: " + cm);
      synchronized (objectToConnectionManagerMap)
      {
         for (Iterator i = objectToConnectionManagerMap.values().iterator(); i.hasNext();)
         {
            Map cmToConnectionsMap = (Map) i.next();
            if (cmToConnectionsMap != null)
               cmToConnectionsMap.remove(cm);
         }
      }
   }

   /**
    * The class <code>KeyConnectionAssociation</code> wraps objects so they may be used in hashmaps
    * based on their object identity rather than equals implementation. Used for keys.
    */
   private final static class KeyConnectionAssociation
   {
      //key
      private final Object o;

      //map of cm to list of connections for that cm.
      private Map cmToConnectionsMap;

      KeyConnectionAssociation(final Object o)
      {
         this.o = o;
      }

      public boolean equals(Object other)
      {
         return (other instanceof KeyConnectionAssociation) && o == ((KeyConnectionAssociation) other).o;
      }

      public String toString()
      {
         return Strings.defaultToString(o);
      }

      public int hashCode()
      {
         return System.identityHashCode(o);
      }

      public void setCMToConnectionsMap(Map cmToConnectionsMap)
      {
         this.cmToConnectionsMap = cmToConnectionsMap;
      }

      public Map getCMToConnectionsMap()
      {
         if (cmToConnectionsMap == null)
         {
            cmToConnectionsMap = new HashMap();
         } // end of if ()
         return cmToConnectionsMap;
      }
   }

   private void closeConnection(Object c)
   {
      try
      {
         Throwable e;
         synchronized (connectionStackTraces)
         {
            e = (Throwable) connectionStackTraces.remove(c);
         }
         Method m = c.getClass().getMethod("close", new Class[]{});
         try
         {
            if (e != null)
               log.info("Closing a connection for you.  Please close them yourself: " + c, e);
            else
               log.info("Closing a connection for you.  Please close them yourself: " + c);
            m.invoke(c, new Object[]{});
         }
         catch (Throwable t)
         {
            log.info("Throwable trying to close a connection for you, please close it yourself", t);
         }
      }
      catch (NoSuchMethodException nsme)
      {
         log.info("Could not find a close method on alleged connection objects.  Please close your own connections.");
      }
   }

   private CloseConnectionSynchronization getCloseConnectionSynchronization(boolean createIfNotFound)
   {
      try
      {
         Transaction tx = tm.getTransaction();
         if (tx != null)
         {
            TransactionSynchronizer.lock(tx);
            try
            {
               CloseConnectionSynchronization cas = (CloseConnectionSynchronization) TransactionSynchronizer.getCCMSynchronization(tx);
               if (cas == null && createIfNotFound && TxUtils.isActive(tx))
               {
                  cas = new CloseConnectionSynchronization();
                  TransactionSynchronizer.registerCCMSynchronization(tx, cas);
               }
               return cas;
            }
            finally
            {
               TransactionSynchronizer.unlock(tx);
            }
         }
      }
      catch (Throwable t)
      {
         log.debug("Unable to synchronize with transaction", t);
      }
      return null;
   }

   private class CloseConnectionSynchronization implements Synchronization
   {
      HashSet connections = new HashSet();
      boolean closing = false;

      public CloseConnectionSynchronization()
      {
      }

      public synchronized void add(Object c)
      {
         if (closing)
            return;
         connections.add(c);
      }

      public synchronized void remove(Object c)
      {
         if (closing)
            return;
         connections.remove(c);
      }

      public void beforeCompletion()
      {
      }

      public void afterCompletion(int status)
      {
         synchronized (this)
         {
            closing = true;
         }
         for (Iterator i = connections.iterator(); i.hasNext();)
            closeConnection(i.next());
         connections.clear(); // Help the GC
      }
   }

}
