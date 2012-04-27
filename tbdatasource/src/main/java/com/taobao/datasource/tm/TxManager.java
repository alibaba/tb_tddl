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
package com.taobao.datasource.tm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.util.UnexpectedThrowable;
import org.jboss.util.UnreachableStatementException;

import com.taobao.datasource.tm.integrity.TransactionIntegrity;

/**
 * Our TransactionManager implementation.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="reverbel@ime.usp.br">Francisco Reverbel</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 57208 $
 */
public class TxManager
      implements TransactionManager,
      TransactionPropagationContextImporter,
      TransactionPropagationContextFactory,
      TransactionLocalDelegate,
      TransactionTimeoutConfiguration,
      JBossXATerminator
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** True if the TxManager should keep a map from GlobalIds to transactions. */
   private boolean globalIdsEnabled = false;

   /** Whether to interrupt threads at transaction timeout */
   private boolean interruptThreads = false;

   /** Instance logger. */
   private Logger log = Logger.getLogger(this.getClass());

   /** True if trace messages should be logged. */
   private boolean trace = log.isTraceEnabled();

   /**
    *  Default timeout in milliseconds.
    *  Must be >= 1000!
    */
   private long timeOut = 5 * 60 * 1000;

   // The following two fields are ints (not longs) because
   // volatile 64Bit types are broken (i.e. access is not atomic) in most VMs, and we
   // don't want to lock just for a statistic. Additionaly,
   // it will take several years on a highly loaded system to
   // exceed the int range. Note that we might loose an
   // increment every now and then, since the ++ operation is
   // not atomic on volatile data types.
   /** A count of the transactions that have been committed */
   private volatile int commitCount;
   /** A count of the transactions that have been rolled back */
   private volatile int rollbackCount;

   /** The transaction integrity policy */
   private TransactionIntegrity integrity;

   // Static --------------------------------------------------------

   /**
    *  The singleton instance.
    */
   private static TxManager singleton = new TxManager();

   /**
    *  Get a reference to the singleton instance.
    */
   public static TxManager getInstance()
   {
      return singleton;
   }

   // Constructors --------------------------------------------------

   /**
    *  Private constructor for singleton. Use getInstance() to obtain
    *  a reference to the singleton.
    */
   private TxManager()
   {
      //make sure TxCapsule can be used
      TransactionImpl.defaultXidFactory();
   }

   // Public --------------------------------------------------------

   /**
    *  Setter for attribute <code>globalIdsEnabled</code>.
    */
   public void setGlobalIdsEnabled(boolean newValue)
   {
      XidImpl.setTrulyGlobalIdsEnabled(newValue);
      globalIdsEnabled = newValue;
   }

   /**
    *  Getter for attribute <code>globalIdsEnabled</code>.
    */
   public boolean getGlobalIdsEnabled()
   {
      return globalIdsEnabled;
   }

   /**
    * Enable/disable thread interruption at transaction timeout.
    *
    * @param interruptThreads pass true to interrupt threads, false otherwise
    */
   public void setInterruptThreads(boolean interruptThreads)
   {
      this.interruptThreads = interruptThreads;
   }

   /**
    * Is thread interruption enabled at transaction timeout
    *
    * @return true for interrupt threads, false otherwise
    */
   public boolean isInterruptThreads()
   {
      return interruptThreads;
   }

   /**
    * Set the transaction integrity policy
    *
    * @param integrity the transaction integrity policy
    */
   public void setTransactionIntegrity(TransactionIntegrity integrity)
   {
      this.integrity = integrity;
   }

   /**
    * Get the transaction integrity policy
    *
    * @return the transaction integrity policy
    */
   public TransactionIntegrity getTransactionIntegrity()
   {
      return integrity;
   }

   /**
    *  Begin a new transaction.
    *  The new transaction will be associated with the calling thread.
    */
   public void begin()
         throws NotSupportedException, SystemException
   {
      trace = log.isTraceEnabled();

      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null)
      {
         if (current.isDone())
            disassociateThread(ti);
         else
            throw new NotSupportedException
                  ("Transaction already active, cannot nest transactions.");
      }

      long timeout = (ti.timeout == 0) ? timeOut : ti.timeout;
      TransactionImpl tx = new TransactionImpl(timeout);
      associateThread(ti, tx);
      localIdTx.put(tx.getLocalId(), tx);
      if (globalIdsEnabled)
         globalIdTx.put(tx.getGlobalId(), tx);

      if (trace)
         log.trace("began tx: " + tx);
   }

   /**
    *  Commit the transaction associated with the currently running thread.
    */
   public void commit()
         throws RollbackException,
         HeuristicMixedException,
         HeuristicRollbackException,
         SecurityException,
         IllegalStateException,
         SystemException
   {
      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null)
      {
         current.commit();
         disassociateThread(ti);
         if (trace)
            log.trace("commited tx: " + current);
      }
      else
         throw new IllegalStateException("No transaction.");
   }

   /**
    *  Return the status of the transaction associated with the currently
    *  running thread, or <code>Status.STATUS_NO_TRANSACTION</code> if no
    *  active transaction is currently associated.
    */
   public int getStatus() throws SystemException
   {
      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null)
      {
         if (current.isDone())
            disassociateThread(ti);
         else
            return current.getStatus();
      }
      return Status.STATUS_NO_TRANSACTION;
   }

   /**
    *  Return the transaction currently associated with the invoking thread,
    *  or <code>null</code> if no active transaction is currently associated.
    */
   public Transaction getTransaction() throws SystemException
   {
      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null && current.isDone())
      {
         current = null;
         disassociateThread(ti);
      }

      return current;
   }

   /**
    *  Resume a transaction.
    *
    *  Note: This will not enlist any resources involved in this
    *  transaction. According to JTA1.0.1 specification section 3.2.3,
    *  that is the responsibility of the application server.
    */
   public void resume(Transaction transaction)
         throws InvalidTransactionException,
         IllegalStateException,
         SystemException
   {
      if (transaction != null && !(transaction instanceof TransactionImpl))
         throw new RuntimeException("Not a TransactionImpl, but a " +
               transaction.getClass().getName());

      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null)
      {
         if (current.isDone())
            current = ti.tx = null;
         else
            throw new IllegalStateException("Already associated with a tx");
      }

      if (current != transaction)
      {
         associateThread(ti, (TransactionImpl)transaction);
      }

      if (trace)
         log.trace("resumed tx: " + ti.tx);
   }

   /**
    *  Suspend the transaction currently associated with the current
    *  thread, and return it.
    *
    *  Note: This will not delist any resources involved in this
    *  transaction. According to JTA1.0.1 specification section 3.2.3,
    *  that is the responsibility of the application server.
    */
   public Transaction suspend() throws SystemException
   {
      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null)
      {
         current.disassociateCurrentThread();
         ti.tx = null;

         if (trace)
            log.trace("suspended tx: " + current);

         if (current.isDone())
            current = null;
      }

      return current;
   }

   /**
    *  Roll back the transaction associated with the currently running thread.
    */
   public void rollback()
         throws IllegalStateException, SecurityException, SystemException
   {
      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null)
      {
         if (!current.isDone())
         {
            current.rollback();

            if (trace)
               log.trace("rolled back tx: " + current);
            return;
         }
         disassociateThread(ti);
      }
      throw new IllegalStateException("No transaction.");
   }

   /**
    *  Mark the transaction associated with the currently running thread
    *  so that the only possible outcome is a rollback.
    */
   public void setRollbackOnly()
         throws IllegalStateException, SystemException
   {
      ThreadInfo ti = getThreadInfo();
      TransactionImpl current = ti.tx;

      if (current != null)
      {
         if (!current.isDone())
         {
            current.setRollbackOnly();

            if (trace)
               log.trace("tx marked for rollback only: " + current);
            return;
         }
         ti.tx = null;
      }
      throw new IllegalStateException("No transaction.");
   }

   public int getTransactionTimeout()
   {
      return (int) (getThreadInfo().timeout / 1000);
   }

   /**
    *  Set the transaction timeout for new transactions started by the
    *  calling thread.
    */
   public void setTransactionTimeout(int seconds)
         throws SystemException
   {
      getThreadInfo().timeout = 1000 * seconds;

      if (trace)
         log.trace("tx timeout is now: " + seconds + "s");
   }

   /**
    *  Set the default transaction timeout for new transactions.
    *  This default value is used if <code>setTransactionTimeout()</code>
    *  was never called, or if it was called with a value of <code>0</code>.
    */
   public void setDefaultTransactionTimeout(int seconds)
   {
      timeOut = 1000L * seconds;

      if (trace)
         log.trace("default tx timeout is now: " + seconds + "s");
   }

   /**
    *  Get the default transaction timeout.
    *
    *  @return Default transaction timeout in seconds.
    */
   public int getDefaultTransactionTimeout()
   {
      return (int) (timeOut / 1000);
   }

   public long getTimeLeftBeforeTransactionTimeout(boolean errorRollback) throws RollbackException
   {
      try
      {
         ThreadInfo ti = getThreadInfo();
         TransactionImpl current = ti.tx;
         if (current != null && current.isDone())
         {
            disassociateThread(ti);
            return -1;
         }
         return current.getTimeLeftBeforeTimeout(errorRollback);
      }
      catch (RollbackException e)
      {
         throw e;
      }
      catch (Exception ignored)
      {
         return -1;
      }
   }

   /**
    *  The following 2 methods are here to provide association and
    *  disassociation of the thread.
    */
   public Transaction disassociateThread()
   {
      return disassociateThread(getThreadInfo());
   }

   private Transaction disassociateThread(ThreadInfo ti) {
      TransactionImpl current = ti.tx;
      ti.tx=null;
      current.disassociateCurrentThread();
      return current;
   }

   public void associateThread(Transaction transaction)
   {
      if (transaction != null && !(transaction instanceof TransactionImpl))
         throw new RuntimeException("Not a TransactionImpl, but a " +
               transaction.getClass().getName());

      // Associate with the thread
      TransactionImpl transactionImpl = (TransactionImpl) transaction;
      ThreadInfo ti = getThreadInfo();
      ti.tx = transactionImpl;
      transactionImpl.associateCurrentThread();
   }

   private void associateThread(ThreadInfo ti, TransactionImpl transaction)
   {
      // Associate with the thread
      ti.tx = transaction;
      transaction.associateCurrentThread();
   }

   /**
    * Return the number of active transactions
    */
   public int getTransactionCount()
   {
      return localIdTx.size();
   }
   /** A count of the transactions that have been committed */
   public long getCommitCount()
   {
      return commitCount;
   }
   /** A count of the transactions that have been rolled back */
   public long getRollbackCount()
   {
      return rollbackCount;
   }

   // Implements TransactionPropagationContextImporter ---------------

   /**
    *  Import a transaction propagation context into this TM.
    *  The TPC is loosely typed, as we may (at a later time) want to
    *  import TPCs that come from other transaction domains without
    *  offloading the conversion to the client.
    *
    *  @param tpc The transaction propagation context that we want to
    *             import into this TM. Currently this is an instance
    *             of LocalId. At some later time this may be an instance
    *             of a transaction propagation context from another
    *             transaction domain like
    *             org.omg.CosTransactions.PropagationContext.
    *
    *  @return A transaction representing this transaction propagation
    *          context, or null if this TPC cannot be imported.
    */
   public Transaction importTransactionPropagationContext(Object tpc)
   {
      if (tpc instanceof LocalId)
      {
         LocalId id = (LocalId) tpc;
         return (Transaction) localIdTx.get(id);
      }
      else if (globalIdsEnabled && tpc instanceof GlobalId)
      {
         GlobalId id = (GlobalId) tpc;
         Transaction tx = (Transaction) globalIdTx.get(id);
         if (trace)
         {
            if (tx != null)
               log.trace("Successfully imported transaction context " + tpc);
            else
               log.trace("Could not import transaction context " + tpc);
         }
         return tx;
      }

      log.warn("Cannot import transaction propagation context: " + tpc);
      return null;
   }

   // Implements TransactionPropagationContextFactory ---------------

   /**
    *  Return a TPC for the current transaction.
    */
   public Object getTransactionPropagationContext()
   {
      return getTransactionPropagationContext(getThreadInfo().tx);
   }

   /**
    *  Return a TPC for the argument transaction.
    */
   public Object getTransactionPropagationContext(Transaction tx)
   {
      // If no transaction or unknown transaction class, return null.
      if (tx == null)
         return null;
      if (!(tx instanceof TransactionImpl))
      {
         log.warn("Cannot export transaction propagation context: " + tx);
         return null;
      }

      return ((TransactionImpl) tx).getLocalId();
   }

   // Implements XATerminator ----------------------------------

   public void registerWork(Work work, Xid xid, long timeout) throws WorkCompletedException
   {
      if (trace)
         log.trace("registering work=" + work + " xid=" + xid + " timeout=" + timeout);
      try
      {
         TransactionImpl tx = importExternalTransaction(xid, timeout);
         tx.setWork(work);
      }
      catch (WorkCompletedException e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         WorkCompletedException e = new WorkCompletedException("Error registering work", t);
         e.setErrorCode(WorkException.TX_RECREATE_FAILED);
         throw e;
      }
      if (trace)
         log.trace("registered work= " + work + " xid=" + xid + " timeout=" + timeout);
   }

   public void startWork(Work work, Xid xid) throws WorkCompletedException
   {
      if (trace)
         log.trace("starting work="+ work +" xid=" + xid);
      TransactionImpl tx = getExternalTransaction(xid);
      associateThread(tx);
      if (trace)
         log.trace("started work= " + work + " xid=" + xid);
   }

   public void endWork(Work work, Xid xid)
   {
      if (trace)
         log.trace("ending work="+ work +" xid=" + xid);
      try
      {
         TransactionImpl tx = getExternalTransaction(xid);
         tx.setWork(null);
         disassociateThread();
      }
      catch (WorkCompletedException e)
      {
         log.error("Unexpected error from endWork ", e);
         throw new UnexpectedThrowable(e.toString());
      }
      if (trace)
         log.trace("ended work="+ work +" xid=" + xid);
   }

   public void cancelWork(Work work, Xid xid)
   {
      if (trace)
         log.trace("cancling work="+ work +" xid=" + xid);
      try
      {
         TransactionImpl tx = getExternalTransaction(xid);
         tx.setWork(null);
      }
      catch (WorkCompletedException e)
      {
         log.error("Unexpected error from cancelWork ", e);
         throw new UnexpectedThrowable(e.toString());
      }
      if (trace)
         log.trace("cancled work="+ work +" xid=" + xid);
   }

   public int prepare(Xid xid) throws XAException
   {
      if (trace)
         log.trace("preparing xid=" + xid);
      try
      {
         TransactionImpl tx = getExternalTransaction(xid);
         int result = tx.prepare();
         if (trace)
            log.trace("prepared xid=" + xid + " result=" + result);
         return result;
      }
      catch (Throwable t)
      {
         JBossXAException.rethrowAsXAException("Error during prepare", t);
         throw new UnreachableStatementException();
      }
   }

   public void rollback(Xid xid) throws XAException
   {
      if (trace)
         log.trace("rolling back xid=" + xid);
      try
      {
         TransactionImpl tx = getExternalTransaction(xid);
         tx.rollback();
      }
      catch (Throwable t)
      {
         JBossXAException.rethrowAsXAException("Error during rollback", t);
      }
      if (trace)
         log.trace("rolled back xid=" + xid);
   }

   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      if (trace)
         log.trace("committing xid=" + xid + " onePhase=" + onePhase);
      try
      {
         TransactionImpl tx = getExternalTransaction(xid);
         tx.commit(onePhase);
      }
      catch (Throwable t)
      {
         JBossXAException.rethrowAsXAException("Error during commit", t);
      }
      if (trace)
         log.trace("committed xid=" + xid);
   }

   public void forget(Xid xid) throws XAException
   {
      if (trace)
         log.trace("forgetting xid=" + xid);
      try
      {
         TransactionImpl tx = getExternalTransaction(xid);
         tx.rollback();
      }
      catch (Throwable t)
      {
         JBossXAException.rethrowAsXAException("Error during forget", t);
      }
      if (trace)
         log.trace("forgot xid=" + xid);
   }

   public Xid[] recover(int flag) throws XAException
   {
      // TODO recover
      return new Xid[0];
   }

   TransactionImpl importExternalTransaction(Xid xid, long timeOut)
   {
      GlobalId gid = new GlobalId(xid);
      TransactionImpl tx = (TransactionImpl) globalIdTx.get(gid);
      if (tx != null)
      {
         if (trace)
            log.trace("imported existing transaction xid: " + xid + " tx=" + tx);
      }
      else
      {
         ThreadInfo ti = getThreadInfo();
         long timeout = (ti.timeout == 0) ? timeOut : ti.timeout;
         tx = new TransactionImpl(gid, timeout);
         localIdTx.put(tx.getLocalId(), tx);
         if (globalIdsEnabled)
            globalIdTx.put(gid, tx);

         if (trace)
            log.trace("imported new transaction xid: " + xid + " tx=" + tx + " timeout=" + timeout);
      }
      return tx;
   }

   TransactionImpl getExternalTransaction(Xid xid) throws WorkCompletedException
   {
      GlobalId gid = new GlobalId(xid);
      TransactionImpl tx = (TransactionImpl) globalIdTx.get(gid);
      if (tx == null)
         throw new WorkCompletedException("Xid not found " + xid, WorkException.TX_RECREATE_FAILED);
      return tx;
   }

   // Implements TransactionLocalDelegate ----------------------

   public void lock(TransactionLocal local, Transaction tx) throws InterruptedException
   {
      TransactionImpl tximpl = (TransactionImpl) tx;
      tximpl.lock();
   }

   public void unlock(TransactionLocal local, Transaction tx)
   {
      TransactionImpl tximpl = (TransactionImpl) tx;
      tximpl.unlock();
   }

   public Object getValue(TransactionLocal local, Transaction tx)
   {
      TransactionImpl tximpl = (TransactionImpl) tx;
      return tximpl.getTransactionLocalValue(local);
   }

   public void storeValue(TransactionLocal local, Transaction tx, Object value)
   {
      TransactionImpl tximpl = (TransactionImpl) tx;
      tximpl.putTransactionLocalValue(local, value);
   }

   public boolean containsValue(TransactionLocal local, Transaction tx)
   {
      TransactionImpl tximpl = (TransactionImpl) tx;
      return tximpl.containsTransactionLocal(local);
   }

   // Package protected ---------------------------------------------

   /**
    *  Release the given TransactionImpl.
    */
   void releaseTransactionImpl(TransactionImpl tx)
   {
      localIdTx.remove(tx.getLocalId());
      if (globalIdsEnabled)
         globalIdTx.remove(tx.getGlobalId());
   }

   /**
    * Increment the commit count
    */
   void incCommitCount()
   {
      ++commitCount;
   }

   /**
    * Increment the rollback count
    */
   void incRollbackCount()
   {
      ++rollbackCount;
   }

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   /**
    *  This keeps track of the thread association with transactions
    *  and timeout values.
    *  In some cases terminated transactions may not be cleared here.
    */
   private ThreadLocal threadTx = new ThreadLocal();

   /**
    *  This map contains the active transactions as values.
    *  The keys are the <code>LocalId</code>s of the transactions.
    */
   private Map localIdTx = Collections.synchronizedMap(new HashMap());


   /**
    *  If <code>globalIdsEnabled</code> is true, this map associates
    *  <code>GlobalId</code>s to active transactions.
    */
   private Map globalIdTx = Collections.synchronizedMap(new HashMap());


   /**
    *  Return the ThreadInfo for the calling thread, and create if not
    *  found.
    */
   private ThreadInfo getThreadInfo()
   {
      ThreadInfo ret = (ThreadInfo) threadTx.get();

      if (ret == null)
      {
         ret = new ThreadInfo();
         ret.timeout = timeOut;
         threadTx.set(ret);
      }

      return ret;
   }


   // Inner classes -------------------------------------------------

   /**
    *  A simple aggregate of a thread-associated timeout value
    *  and a thread-associated transaction.
    */
   static class ThreadInfo
   {
      long timeout;
      TransactionImpl tx;
   }
}
