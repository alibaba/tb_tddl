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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.Logger;
import org.jboss.util.timeout.Timeout;
import org.jboss.util.timeout.TimeoutFactory;
import org.jboss.util.timeout.TimeoutTarget;

import com.taobao.datasource.tm.integrity.TransactionIntegrity;

/**
 *  Our <code>Transaction</code> implementation.
 *
 *  @see TxManager
 *
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Ãberg</a>
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 *  @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 *  @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 *  @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *  @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 *  @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 *  @version $Revision: 57208 $
 */
public class TransactionImpl
   implements Transaction, TimeoutTarget
{
   // Constants -----------------------------------------------------

   /**
    * Code meaning "no heuristics seen",
    * must not be XAException.XA_HEURxxx
    */
   private static final int HEUR_NONE = XAException.XA_RETRY;

   // Resource states
   private final static int RS_NEW           = 0; // not yet enlisted
   private final static int RS_ENLISTED      = 1; // enlisted
   private final static int RS_SUSPENDED     = 2; // suspended
   private final static int RS_ENDED         = 3; // not associated
   private final static int RS_VOTE_READONLY = 4; // voted read-only
   private final static int RS_VOTE_OK       = 5; // voted ok
   private final static int RS_FORGOT        = 6; // RM has forgotten


   // Attributes ----------------------------------------------------

   /** Class logger, we don't want a new logger with every transaction. */
   private static Logger log = Logger.getLogger(TransactionImpl.class);

   /** True if trace messages should be logged. */
   private boolean trace = log.isTraceEnabled();

   /** The ID of this transaction. */
   private XidImpl xid;

   /** The global id */
   private GlobalId gid;

   private HashSet threads = new HashSet(1);

   private Map transactionLocalMap = Collections.synchronizedMap(new HashMap());

   private Throwable cause;

   /**
    *  The synchronizations to call back.
    */
   private Synchronization[] sync = new Synchronization[3];

   /**
    *  Size of allocated synchronization array.
    */
   private int syncAllocSize = 3;

   /**
    *  Count of synchronizations for this transaction.
    */
   private int syncCount = 0;

   /**
    *  A list of the XAResources that have participated in this transaction.
    */
   private ArrayList resources = new ArrayList(3);

   /**
    * The XAResource used in the last resource gambit
    */
   private Resource lastResource;

   /**
    *  Flags that it is too late to enlist new resources.
    */
   private boolean resourcesEnded = false;

   /**
    *  Last branch id used.
    */
   private long lastBranchId = 0;

   /**
    *  Status of this transaction.
    */
   private int status;

   /**
    *  The heuristics status of this transaction.
    */
   private int heuristicCode = HEUR_NONE;

   /**
    *  The time when this transaction was started.
    */
   private long start;

   /**
    *  The timeout handle for this transaction.
    */
   private Timeout timeout;

   /**
    * Timeout in millisecs
    */
   private long timeoutPeriod;

   /**
    *  Mutex for thread-safety. This should only be changed in the
    *  <code>lock()</code> and <code>unlock()</code> methods.
    */
   private Thread locked = null;

   /**
    * The lock depth
    */
   private int lockDepth = 0;

   /** Any current work associated with the transaction */
   private Work work;

   /**
    *  Flags that we are done with this transaction and that it can be reused.
    */
   private boolean done = false;

   // Static --------------------------------------------------------

   /**
    *  Factory for Xid instances of specified class.
    *  This is set from the <code>TransactionManagerService</code>
    *  MBean.
    */
   static XidFactoryMBean xidFactory;

//   static TransactionManagerService txManagerService;

   /** The timeout factory */
   static TimeoutFactory timeoutFactory = TimeoutFactory.getSingleton();

   /**
    * This static code is only present for testing purposes so a
    * tm can be usable without a lot of setup.
    */
   static void defaultXidFactory()
   {
      if (xidFactory == null)
         xidFactory = new XidFactory();
   }


   // Constructors --------------------------------------------------

   TransactionImpl(long timeout)
   {
      xid = xidFactory.newXid();
      gid = xid.getTrulyGlobalId();

      status = Status.STATUS_ACTIVE;

      start = System.currentTimeMillis();
      this.timeout = timeoutFactory.createTimeout(start+timeout, this);
      this.timeoutPeriod = timeout;
      if (trace)
         log.trace("Created new instance for tx=" + toString());
   }

   TransactionImpl(GlobalId gid, long timeout)
   {
      this.gid = gid;
      xid = xidFactory.newXid();

      status = Status.STATUS_ACTIVE;

      start = System.currentTimeMillis();
      this.timeout = timeoutFactory.createTimeout(start+timeout, this);
      this.timeoutPeriod = timeout;
      if (trace)
         log.trace("Created new instance for tx=" + toString());
   }

   // Implements TimeoutTarget --------------------------------------

   /**
    *  Called when our timeout expires.
    */
   public void timedOut(Timeout timeout)
   {
      lock();
      try
      {

         log.warn("Transaction " + toString() + " timed out." +
                  " status=" + getStringStatus(status));

         if (this.timeout == null)
            return; // Don't race with timeout cancellation.
         this.timeout = null;

         switch (status)
         {
            case Status.STATUS_ROLLEDBACK:
            case Status.STATUS_COMMITTED:
            case Status.STATUS_NO_TRANSACTION:
               return; // Transaction done.

            case Status.STATUS_ROLLING_BACK:
               return; // Will be done shortly.

            case Status.STATUS_COMMITTING:
               // This is _very_ bad:
               // We are in the second commit phase, and have decided
               // to commit, but now we get a timeout and should rollback.
               // So we end up with a mixed decision.
               gotHeuristic(null, XAException.XA_HEURMIX);
               status = Status.STATUS_MARKED_ROLLBACK;
               return; // commit will fail

            case Status.STATUS_PREPARED:
               // This is bad:
               // We are done with the first phase, and are persistifying
               // our decision. Fortunately this case is currently never
               // hit, as we do not release the lock between the two phases.
            case Status.STATUS_ACTIVE:
               status = Status.STATUS_MARKED_ROLLBACK;
               // fall through..
            case Status.STATUS_MARKED_ROLLBACK:
               // don't rollback for now, this messes up with the TxInterceptor.
               interruptThreads();
               return;

            case Status.STATUS_PREPARING:
               status = Status.STATUS_MARKED_ROLLBACK;
               return; // commit will fail

            default:
               log.warn("Unknown status at timeout, tx=" + toString());
               return;
         }
      }
      finally
      {
         unlock();
      }
   }

   // Implements Transaction ----------------------------------------

   public void commit()
      throws RollbackException,
             HeuristicMixedException,
             HeuristicRollbackException,
             java.lang.SecurityException,
             java.lang.IllegalStateException,
             SystemException
   {
      lock();
      try
      {
         if (trace)
            log.trace("Committing, tx=" + this + ", status=" + getStringStatus(status));

         beforePrepare();

         if (status == Status.STATUS_ACTIVE)
         {
            switch (getCommitStrategy())
            {
            case 0:
               // Zero phase commit is really fast ;-)
               if (trace)
                  log.trace("Zero phase commit " + this + ": No resources.");
               status = Status.STATUS_COMMITTED;
               break;
            case 1:
               // One phase commit
               if (trace)
                  log.trace("One phase commit " + this + ": One resource.");
               commitResources(true);
               break;
            default:
               // Two phase commit
               if (trace)
                  log.trace("Two phase commit " + this + ": Many resources.");

               if (!prepareResources())
               {
                  boolean commitDecision =
                     status == Status.STATUS_PREPARED &&
                     (heuristicCode == HEUR_NONE ||
                      heuristicCode == XAException.XA_HEURCOM);

                  // TODO: Save decision to stable storage for recovery
                  //       after system crash.

                  if (commitDecision)
                     commitResources(false);
               }
               else
                  status = Status.STATUS_COMMITTED; // all was read-only
            }
         }

         if (status != Status.STATUS_COMMITTED)
         {
            Throwable causedByThrowable = cause;
            rollbackResources();
            completeTransaction();

            // throw jboss rollback exception with the saved off cause
            throw new JBossRollbackException("Unable to commit, tx=" +
                  toString() + " status=" + getStringStatus(status),
                  causedByThrowable);
         }

         completeTransaction();
         checkHeuristics();

         if (trace)
            log.trace("Committed OK, tx=" + this);

      }
      finally
      {
         unlock();
      }
   }

   public void rollback()
      throws java.lang.IllegalStateException,
             java.lang.SecurityException,
             SystemException
   {
      lock();
      try
      {

         if (trace)
            log.trace("rollback(): Entered, tx=" + toString() +
            " status=" + getStringStatus(status));

         checkWork();

         switch (status)
         {
            case Status.STATUS_ACTIVE:
               status = Status.STATUS_MARKED_ROLLBACK;
               // fall through..
            case Status.STATUS_MARKED_ROLLBACK:
               endResources();
               rollbackResources();
               completeTransaction();
               // Cannot throw heuristic exception, so we just have to
               // clear the heuristics without reporting.
               heuristicCode = HEUR_NONE;
               return;
            case Status.STATUS_PREPARING:
               // Set status to avoid race with prepareResources().
               status = Status.STATUS_MARKED_ROLLBACK;
               return; // commit() will do rollback.
            default:
               throw new IllegalStateException("Cannot rollback(), " +
               "tx=" + toString() +
               " status=" +
               getStringStatus(status));
         }
      }
      finally
      {
         Thread.interrupted();// clear timeout that did an interrupt
         unlock();
      }
   }

   public boolean delistResource(XAResource xaRes, int flag)
      throws java.lang.IllegalStateException,
             SystemException
   {
      if (xaRes == null)
         throw new IllegalArgumentException("null xaRes tx=" + this);
      if (flag != XAResource.TMSUCCESS &&
          flag != XAResource.TMSUSPEND &&
          flag != XAResource.TMFAIL)
         throw new IllegalArgumentException("Bad flag: " + flag + " tx=" + this);

      lock();
      try
      {
         if (trace)
            log.trace("delistResource(): Entered, tx=" +
            toString() + " status=" + getStringStatus(status));

         Resource resource = findResource(xaRes);
         if (resource == null)
            throw new IllegalArgumentException("xaRes not enlisted " + xaRes);

         switch (status)
         {
            case Status.STATUS_ACTIVE:
            case Status.STATUS_MARKED_ROLLBACK:
               break;
            case Status.STATUS_PREPARING:
               throw new IllegalStateException("Already started preparing. " + this);
            case Status.STATUS_ROLLING_BACK:
               throw new IllegalStateException("Already started rolling back. " + this);
            case Status.STATUS_PREPARED:
               throw new IllegalStateException("Already prepared. " + this);
            case Status.STATUS_COMMITTING:
               throw new IllegalStateException("Already started committing. " + this);
            case Status.STATUS_COMMITTED:
               throw new IllegalStateException("Already committed. " + this);
            case Status.STATUS_ROLLEDBACK:
               throw new IllegalStateException("Already rolled back. " + this);
            case Status.STATUS_NO_TRANSACTION:
               throw new IllegalStateException("No transaction. " + this);
            case Status.STATUS_UNKNOWN:
               throw new IllegalStateException("Unknown state " + this);
            default:
               throw new IllegalStateException("Illegal status: " + getStringStatus(status) + " tx=" + this);
         }

         try
         {
            return resource.delistResource(xaRes, flag);
         }
         catch (XAException xae)
         {
            logXAException(xae);
            status = Status.STATUS_MARKED_ROLLBACK;
            cause = xae;
            return false;
         }
      }
      finally
      {
         unlock();
      }
   }

   public boolean enlistResource(XAResource xaRes)
      throws RollbackException,
             java.lang.IllegalStateException,
             SystemException
   {
      if (xaRes == null)
         throw new IllegalArgumentException("null xaRes tx=" + this);

      lock();
      try
      {

         if (trace)
            log.trace("enlistResource(): Entered, tx=" +
            toString() + " status=" + getStringStatus(status) + " xaRes=" + xaRes);

         switch (status)
         {
            case Status.STATUS_ACTIVE:
            case Status.STATUS_PREPARING:
               break;
            case Status.STATUS_PREPARED:
               throw new IllegalStateException("Already prepared. " + this);
            case Status.STATUS_COMMITTING:
               throw new IllegalStateException("Already started committing. " + this);
            case Status.STATUS_COMMITTED:
               throw new IllegalStateException("Already committed. " + this);
            case Status.STATUS_MARKED_ROLLBACK:
               throw new RollbackException("Already marked for rollback " + this);
            case Status.STATUS_ROLLING_BACK:
               throw new RollbackException("Already started rolling back. " + this);
            case Status.STATUS_ROLLEDBACK:
               throw new RollbackException("Already rolled back. " + this);
            case Status.STATUS_NO_TRANSACTION:
               throw new IllegalStateException("No transaction. " + this);
            case Status.STATUS_UNKNOWN:
               throw new IllegalStateException("Unknown state " + this);
            default:
               throw new IllegalStateException("Illegal status: " + getStringStatus(status) + " tx=" + this);
         }

         if (resourcesEnded)
            throw new IllegalStateException("Too late to enlist resources " + this);

         // Add resource
         try
         {
            Resource resource = findResource(xaRes);

            // Existing resource
            if (resource != null)
            {
               if (resource.isEnlisted())
               {
                  if (trace)
                     log.trace("Already enlisted: tx=" +
                        toString() + " status=" + getStringStatus(status) + " xaRes=" + xaRes);
                  return true; // already enlisted
               }
               if (resource.isDelisted(xaRes))
                  // this is a resource that returns false on all calls to
                  // isSameRM.  Further, the last resource enlisted has
                  // already been delisted, so it is time to enlist it again.
                  resource = null;
               else
                  return resource.startResource();
            }

            resource = findResourceManager(xaRes);
            if (resource != null)
            {
               // The xaRes is new. We register the xaRes with the Xid
               // that the RM has previously seen from this transaction,
               // and note that it has the same RM.
               resource = addResource(xaRes, resource.getXid(), resource);
               return resource.startResource();
            }

            // New resource and new RM: Create a new transaction branch.
            resource = addResource(xaRes, createXidBranch(), null);
            return resource.startResource();
         }
         catch (XAException xae)
         {
            logXAException(xae);
            cause = xae;
            return false;
         }
      }
      finally
      {
         unlock();
      }

   }

   public int getStatus()
      throws SystemException
   {
      if (done)
         return Status.STATUS_NO_TRANSACTION;
      return status;
   }

   public void registerSynchronization(Synchronization s)
      throws RollbackException,
             java.lang.IllegalStateException,
             SystemException
   {
      if (s == null)
         throw new IllegalArgumentException("Null synchronization " + this);

      lock();
      try
      {
         if (trace)
         {
            log.trace("registerSynchronization(): Entered, " +
            "tx=" + toString() +
            " status=" + getStringStatus(status));
         }

         switch (status)
         {
         case Status.STATUS_ACTIVE:
         case Status.STATUS_PREPARING:
            break;
         case Status.STATUS_PREPARED:
            throw new IllegalStateException("Already prepared. " + this);
         case Status.STATUS_COMMITTING:
            throw new IllegalStateException("Already started committing. " + this);
         case Status.STATUS_COMMITTED:
            throw new IllegalStateException("Already committed. " + this);
         case Status.STATUS_MARKED_ROLLBACK:
            throw new RollbackException("Already marked for rollback " + this);
         case Status.STATUS_ROLLING_BACK:
            throw new RollbackException("Already started rolling back. " + this);
         case Status.STATUS_ROLLEDBACK:
            throw new RollbackException("Already rolled back. " + this);
         case Status.STATUS_NO_TRANSACTION:
            throw new IllegalStateException("No transaction. " + this);
         case Status.STATUS_UNKNOWN:
            throw new IllegalStateException("Unknown state " + this);
         default:
            throw new IllegalStateException("Illegal status: " + getStringStatus(status) + " tx=" + this);
         }

         if (syncCount == syncAllocSize)
         {
            // expand table
            syncAllocSize = 2 * syncAllocSize;

            Synchronization[] sy = new Synchronization[syncAllocSize];
            System.arraycopy(sync, 0, sy, 0, syncCount);
            sync = sy;
         }
         sync[syncCount++] = s;
      }
      finally
      {
         unlock();
      }
   }

   public void setRollbackOnly()
      throws java.lang.IllegalStateException,
             SystemException
   {
      lock();
      try
      {
         if (trace)
            log.trace("setRollbackOnly(): Entered, tx=" +
                      toString() + " status=" + getStringStatus(status));

         switch (status)
         {
            case Status.STATUS_ACTIVE:
            case Status.STATUS_PREPARING:
            case Status.STATUS_PREPARED:
               status = Status.STATUS_MARKED_ROLLBACK;
               // fall through..
            case Status.STATUS_MARKED_ROLLBACK:
            case Status.STATUS_ROLLING_BACK:
               return;
            case Status.STATUS_COMMITTING:
               throw new IllegalStateException("Already started committing. " + this);
            case Status.STATUS_COMMITTED:
               throw new IllegalStateException("Already committed. " + this);
            case Status.STATUS_ROLLEDBACK:
               throw new IllegalStateException("Already rolled back. " + this);
            case Status.STATUS_NO_TRANSACTION:
               throw new IllegalStateException("No transaction. " + this);
            case Status.STATUS_UNKNOWN:
               throw new IllegalStateException("Unknown state " + this);
            default:
               throw new IllegalStateException("Illegal status: " + getStringStatus(status) + " tx=" + this);
         }
      }
      finally
      {
         unlock();
      }
   }

   // Public --------------------------------------------------------

   public int getAssociatedThreadCount()
   {
      lock();
      try
      {
         return threads.size();
      }
      finally
      {
         unlock();
      }
   }

   public Set getAssociatedThreads()
   {
      lock();
      try
      {
         return Collections.unmodifiableSet(threads);
      }
      finally
      {
         unlock();
      }
   }

   public int hashCode()
   {
      return xid.hashCode();
   }

   public String toString()
   {
      return "TransactionImpl:" + xidFactory.toString(xid);
   }

   public boolean equals(Object obj)
   {
      if (obj != null && obj instanceof TransactionImpl)
         return getLocalIdValue() == (((TransactionImpl)obj).getLocalIdValue());
      return false;
   }


   /**
    *  Returns the local id of this transaction. The local id is used as
    *  a transaction propagation context within the JBoss server, and
    *  in the TxManager for mapping local transaction ids to transactions.
    */
   public long getLocalIdValue()
   {
      return xid.getLocalIdValue();
   }

   /**
    *  Returns the local id of this transaction. The local id is used as
    *  a transaction propagation context within the JBoss server, and
    *  in the TxManager for mapping local transaction ids to transactions.
    */
   public LocalId getLocalId()
   {
      return xid.getLocalId();
   }

   /**
    *  Returns the global id of this transaction. Ths global id is used in
    *  the TxManager, which keeps a map from global ids to transactions.
    */
   public GlobalId getGlobalId()
   {
      return gid;
   }

   /**
    *  Returns the xid of this transaction.
    */
   public XidImpl getXid()
   {
      return xid;
   }

   // Package protected ---------------------------------------------

   void associateCurrentThread()
   {
      Thread.interrupted();
      lock();
      try
      {
         threads.add(Thread.currentThread());
      }
      finally
      {
         unlock();
      }
   }

   void disassociateCurrentThread()
   {
      // Just a tidyup, no need to synchronize
      if (done)
      {
         threads.remove(Thread.currentThread());
      }
      else
      {
         // Removing the association for an active transaction
         lock();
         try
         {
            threads.remove(Thread.currentThread());
         }
         finally
         {
            unlock();
         }
      }
      Thread.interrupted();
   }

   /**
    *  Lock this instance.
    */
   synchronized void lock()
   {
      if (done)
         throw new IllegalStateException("Transaction has terminated " + this);

      Thread currentThread = Thread.currentThread();
      if (locked != null && locked != currentThread)
      {
         log.debug("Lock contention, tx=" + toString() + " otherThread=" + locked);
         //DEBUG Thread.currentThread().dumpStack();

         while (locked != null && locked != currentThread)
         {
            try
            {
               // Wakeup happens when:
               // - notify() is called from unlock()
               // - notifyAll is called from instanceDone()
               wait();
            }
            catch (InterruptedException ex)
            {
               // ignore
            }

            if (done)
               throw new IllegalStateException("Transaction has now terminated " + this);
         }
      }

      locked = currentThread;
      ++lockDepth;
   }

   /**
    *  Unlock this instance.
    */
   synchronized void unlock()
   {
      Thread currentThread = Thread.currentThread();
      if (locked == null || locked != currentThread)
      {
         log.warn("Unlocking, but not locked, tx=" + toString() + " otherThread=" + locked,
         new Throwable("[Stack trace]"));
      }
      else
      {
         if (--lockDepth == 0)
         {
            locked = null;
            notify();
         }
      }
   }

   /**
    * Prepare an external transaction
    *
    * @return XAResource.XA_RDONLY or XAResource.XA_OK
    */
   int prepare() throws HeuristicMixedException, HeuristicRollbackException, RollbackException
   {
      lock();
      try
      {
         if (trace)
            log.trace("Preparing, tx=" + this + ", status=" + getStringStatus(status));

         checkWork();

         beforePrepare();

         if (status == Status.STATUS_ACTIVE)
         {
            switch (getCommitStrategy())
            {
               case 0:
               {
                  // Nothing to do
                  if (trace)
                     log.trace("Prepare tx=" + this + ": No resources.");
                  status = Status.STATUS_COMMITTED;
                  completeTransaction();
                  return XAResource.XA_RDONLY;
               }
               default:
               {
                  // Two phase commit
                  if (trace)
                     log.trace("Prepare tx=" + this + ": Many resources.");

                  if (!prepareResources())
                  {
                     /*boolean commitDecision =
                        status == Status.STATUS_PREPARED &&
                        (heuristicCode == HEUR_NONE ||
                         heuristicCode == XAException.XA_HEURCOM);*/

                     // TODO: Save decision to stable storage for recovery
                     //       after system crash.
                  }
                  else
                  {
                     if (trace)
                        log.trace("Prepared tx=" + this + ": All readonly.");
                     status = Status.STATUS_COMMITTED;
                     completeTransaction();
                     return XAResource.XA_RDONLY;
                  }
               }
            }
         }

         if (status != Status.STATUS_PREPARED)
         {
            // save off the cause throwable as Instance done resets it to null
            Throwable causedByThrowable = cause;
            rollbackResources();
            completeTransaction();

            // throw jboss rollback exception with the saved off cause
            throw new JBossRollbackException("Unable to prepare, tx=" +
                  toString() + " status=" + getStringStatus(status),
                  causedByThrowable);
         }

         // We are ok to commit
         return XAResource.XA_OK;
      }
      finally
      {
         unlock();
      }
   }

   /**
    * Commit an external transaction
    *
    * @param onePhase whether the commit is one or two phase
    */
   void commit(boolean onePhase) throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException
   {
      checkWork();

      // One phase commit optimization
      if (onePhase)
      {
         commit();
         return;
      }

      // Two phase
      lock();
      try
      {
         if (trace)
            log.trace("Committing two phase, tx=" + this + ", status=" + getStringStatus(status));

         switch (status)
         {
            case Status.STATUS_PREPARING:
               throw new IllegalStateException("Still preparing. " + this);
            case Status.STATUS_ROLLING_BACK:
               throw new IllegalStateException("Already started rolling back. " + this);
            case Status.STATUS_ROLLEDBACK:
               instanceDone();
               checkHeuristics();
               throw new IllegalStateException("Already rolled back. " + this);
            case Status.STATUS_COMMITTING:
               throw new IllegalStateException("Already started committing. " + this);
            case Status.STATUS_COMMITTED:
               instanceDone();
               checkHeuristics();
               throw new IllegalStateException("Already committed. " + this);
            case Status.STATUS_NO_TRANSACTION:
               throw new IllegalStateException("No transaction. " + this);
            case Status.STATUS_UNKNOWN:
               throw new IllegalStateException("Unknown state " + this);
            case Status.STATUS_MARKED_ROLLBACK:
               endResources();
               rollbackResources();
               completeTransaction();
               checkHeuristics();
               throw new RollbackException("Already marked for rollback " + this);
            case Status.STATUS_PREPARED:
               break;
            default:
               throw new IllegalStateException("Illegal status: " + getStringStatus(status) + " tx=" + this);
         }

         commitResources(false);

         if (status != Status.STATUS_COMMITTED)
         {
            Throwable causedByThrowable = cause;
            rollbackResources();
            completeTransaction();

            // throw jboss rollback exception with the saved off cause
            throw new JBossRollbackException("Unable to commit, tx=" +
                  toString() + " status=" + getStringStatus(status),
                  causedByThrowable);
         }

         completeTransaction();
         checkHeuristics();

         if (trace)
            log.trace("Committed OK, tx=" + this);

      }
      finally
      {
         unlock();
      }
   }

   /**
    * Get the work
    *
    * @return the work
    */
   Work getWork()
   {
      return work;
   }

   /**
    * Set the work
    *
    * @param work the work
    * @throws WorkCompletedException with error code WorkException.TX_CONCURRENT_WORK_DISALLOWED
    *         when work is already present for the xid or whose completion is in progress, only
    *         the global part of the xid must be used for this check. Or with error code
    *         WorkException.TX_RECREATE_FAILED if it is unable to recreate the transaction context
    */
   void setWork(Work work) throws WorkCompletedException
   {
      lock();
      try
      {
         if (work == null)
         {
            this.work = null;
            return;
         }

         if (status == Status.STATUS_NO_TRANSACTION || status == Status.STATUS_UNKNOWN)
            throw new WorkCompletedException("The transaction is not active " + this + ": " + getStringStatus(status), WorkException.TX_RECREATE_FAILED);
         else if (status != Status.STATUS_ACTIVE)
            throw new WorkCompletedException("Too late to start work " + this + ": " + getStringStatus(status), WorkException.TX_CONCURRENT_WORK_DISALLOWED);
         else if (this.work != null)
            throw new WorkCompletedException("Already have work " + this + ": " + this.work, WorkException.TX_CONCURRENT_WORK_DISALLOWED);

         this.work = work;
      }
      finally
      {
         unlock();
      }
   }

   /**
    *  Getter for property done.
    */
   boolean isDone()
   {
      return done;
   }

   // Private -------------------------------------------------------

   /**
    * Before prepare
    */
   private void beforePrepare() throws HeuristicMixedException, HeuristicRollbackException, RollbackException
   {
      checkIntegrity();

      doBeforeCompletion();

      if (trace)
         log.trace("Before completion done, tx=" + this +
         ", status=" + getStringStatus(status));

      endResources();
   }

   /**
    * Check the integrity of the transaction
    */
   private void checkIntegrity() throws HeuristicMixedException, HeuristicRollbackException, RollbackException
   {
      // Spec defined checks for the transaction in a valid state
      checkBeforeStatus();

      TransactionIntegrity integrity = TxManager.getInstance().getTransactionIntegrity();
      if (integrity != null)
      {
         // Extra integrity checks
         unlock();
         try
         {
            integrity.checkTransactionIntegrity(this);
         }
         finally
         {
            lock();
         }

         // Recheck the transaction state
         checkBeforeStatus();
      }
   }

   /**
    * Check the before status
    */
   private void checkBeforeStatus()
      throws HeuristicMixedException,
             HeuristicRollbackException,
             RollbackException
   {
      switch (status)
      {
      case Status.STATUS_PREPARING:
         throw new IllegalStateException("Already started preparing. " + this);
      case Status.STATUS_PREPARED:
         throw new IllegalStateException("Already prepared. " + this);
      case Status.STATUS_ROLLING_BACK:
         throw new IllegalStateException("Already started rolling back. " +
                                         this);
      case Status.STATUS_ROLLEDBACK:
         instanceDone();
         checkHeuristics();
         throw new IllegalStateException("Already rolled back." + this);
      case Status.STATUS_COMMITTING:
         throw new IllegalStateException("Already started committing. " + this);
      case Status.STATUS_COMMITTED:
         instanceDone();
         checkHeuristics();
         throw new IllegalStateException("Already committed. " + this);
      case Status.STATUS_NO_TRANSACTION:
         throw new IllegalStateException("No transaction. " + this);
      case Status.STATUS_UNKNOWN:
         throw new IllegalStateException("Unknown state " + this);
      case Status.STATUS_MARKED_ROLLBACK:
         endResources();
         rollbackResources();
         completeTransaction();
         checkHeuristics();
         throw new RollbackException("Already marked for rollback " + this);
      case Status.STATUS_ACTIVE:
         break;
      default:
         throw new IllegalStateException("Illegal status: " +
                                         getStringStatus(status) +
                                            " tx=" + this);
      }
   }

   /**
    * Complete the transaction
    */
   private void completeTransaction()
   {
      cancelTimeout();
      doAfterCompletion();
      instanceDone();
   }

   /**
    * Interrupt all threads involved with transaction
    * This is called on timeout
    */
   private void interruptThreads()
   {
      TxManager manager = TxManager.getInstance();
      if (manager.isInterruptThreads())
      {
         HashSet clone = (HashSet) threads.clone();
         threads.clear();
         for (Iterator i = clone.iterator(); i.hasNext();)
         {
            Thread thread = (Thread) i.next();
            try
            {
               thread.interrupt();
            }
            catch (Throwable ignored)
            {
               if (trace)
                  log.trace("Ignored error interrupting thread: " + thread, ignored);
            }
         }
      }
   }

   /**
    *  Return a string representation of the given status code.
    */
   private String getStringStatus(int status)
   {
      switch (status)
      {
         case Status.STATUS_PREPARING:
            return "STATUS_PREPARING";
         case Status.STATUS_PREPARED:
            return "STATUS_PREPARED";
         case Status.STATUS_ROLLING_BACK:
            return "STATUS_ROLLING_BACK";
         case Status.STATUS_ROLLEDBACK:
            return "STATUS_ROLLEDBACK";
         case Status.STATUS_COMMITTING:
            return "STATUS_COMMITING";
         case Status.STATUS_COMMITTED:
            return "STATUS_COMMITED";
         case Status.STATUS_NO_TRANSACTION:
            return "STATUS_NO_TRANSACTION";
         case Status.STATUS_UNKNOWN:
            return "STATUS_UNKNOWN";
         case Status.STATUS_MARKED_ROLLBACK:
            return "STATUS_MARKED_ROLLBACK";
         case Status.STATUS_ACTIVE:
            return "STATUS_ACTIVE";

         default:
            return "STATUS_UNKNOWN(" + status + ")";
      }
   }

   /**
    *  Return a string representation of the given XA error code.
    */
   private String getStringXAErrorCode(int errorCode)
   {
      switch (errorCode)
      {
         case XAException.XA_HEURCOM:
            return "XA_HEURCOM";
         case XAException.XA_HEURHAZ:
            return "XA_HEURHAZ";
         case XAException.XA_HEURMIX:
            return "XA_HEURMIX";
         case XAException.XA_HEURRB:
            return "XA_HEURRB";

         case XAException.XA_NOMIGRATE:
            return "XA_NOMIGRATE";

         case XAException.XA_RBCOMMFAIL:
            return "XA_RBCOMMFAIL";
         case XAException.XA_RBDEADLOCK:
            return "XA_RBDEADLOCK";
         case XAException.XA_RBINTEGRITY:
            return "XA_RBINTEGRITY";
         case XAException.XA_RBOTHER:
            return "XA_RBOTHER";
         case XAException.XA_RBPROTO:
            return "XA_RBPROTO";
         case XAException.XA_RBROLLBACK:
            return "XA_RBROLLBACK";
         case XAException.XA_RBTIMEOUT:
            return "XA_RBTIMEOUT";
         case XAException.XA_RBTRANSIENT:
            return "XA_RBTRANSIENT";

         case XAException.XA_RDONLY:
            return "XA_RDONLY";
         case XAException.XA_RETRY:
            return "XA_RETRY";

         case XAException.XAER_ASYNC:
            return "XAER_ASYNC";
         case XAException.XAER_DUPID:
            return "XAER_DUPID";
         case XAException.XAER_INVAL:
            return "XAER_INVAL";
         case XAException.XAER_NOTA:
            return "XAER_NOTA";
         case XAException.XAER_OUTSIDE:
            return "XAER_OUTSIDE";
         case XAException.XAER_PROTO:
            return "XAER_PROTO";
         case XAException.XAER_RMERR:
            return "XAER_RMERR";
         case XAException.XAER_RMFAIL:
            return "XAER_RMFAIL";

         default:
            return "XA_UNKNOWN(" + errorCode + ")";
      }
   }

   private void logXAException(XAException xae)
   {
      log.warn("XAException: tx=" + toString() + " errorCode=" +
               getStringXAErrorCode(xae.errorCode), xae);
//      if (txManagerService != null)
//         txManagerService.formatXAException(xae, log);
   }

   /**
    *  Mark this transaction as non-existing.
    */
   private synchronized void instanceDone()
   {
      TxManager manager = TxManager.getInstance();

      if (status == Status.STATUS_COMMITTED)
         manager.incCommitCount();
      else
         manager.incRollbackCount();

      // Clear tables refering to external objects.
      // Even if a client holds on to this instance forever, the objects
      // that we have referenced may be garbage collected.
      sync = null;
      resources = null;
      transactionLocalMap.clear();
      threads.clear();

      // Garbage collection
      manager.releaseTransactionImpl(this);

      // Set the status
      status = Status.STATUS_NO_TRANSACTION;

      // Notify all threads waiting for the lock.
      notifyAll();

      // set the done flag
      done = true;
   }

   /**
    *  Cancel the timeout.
    *  This will release the lock while calling out.
    */
   private void cancelTimeout()
   {
      if (timeout != null)
      {
         unlock();
         try
         {
            timeout.cancel();
         }
         catch (Exception e)
         {
            if (trace)
               log.trace("failed to cancel timeout " + this, e);
         }
         finally
         {
            lock();
         }
         timeout = null;
      }
   }

   /**
    *  Return the resource for the given XAResource
    */
   private Resource findResource(XAResource xaRes)
   {
      // A linear search may seem slow, but please note that
      // the number of XA resources registered with a transaction
      // are usually low.
      // Note: This searches backwards intentionally!  It ensures that
      // if this resource was enlisted multiple times, then the last one
      // will be returned.  All others should be in the state RS_ENDED.
      // This allows ResourceManagers that always return false from isSameRM
      // to be enlisted and delisted multiple times.
      for (int idx = resources.size() - 1; idx >= 0; --idx)
      {
         Resource resource = (Resource) resources.get(idx);
         if (xaRes == resource.getXAResource())
            return resource;
      }

      return null;
   }

   private Resource findResourceManager(XAResource xaRes) throws XAException
   {
      for (int i = 0; i < resources.size(); ++i)
      {
         Resource resource = (Resource) resources.get(i);
         if (resource.isResourceManager(xaRes))
            return resource;
      }
      return null;
   }

   /**
    *  Add a resource, expanding tables if needed.
    *
    *  @param xaRes The new XA resource to add. It is assumed that the
    *         resource is not already in the table of XA resources.
    *  @param branchXid The Xid for the transaction branch that is to
    *         be used for associating with this resource.
    *  @param sameRMResource The resource of the first
    *         XA resource having the same resource manager as
    *         <code>xaRes</code>, or <code>null</code> if <code>xaRes</code>
    *         is the first resource seen with this resource manager.
    *
    *  @return the new resource
    */
   private Resource addResource(XAResource xaRes, Xid branchXid, Resource sameRMResource)
   {
      Resource resource = new Resource(xaRes, branchXid, sameRMResource);
      resources.add(resource);

      // Remember the first resource that wants the last resource gambit
      if (lastResource == null && xaRes instanceof LastResource)
         lastResource = resource;

      return resource;
   }

   /**
    *  End Tx association for all resources.
    */
   private void endResources()
   {
      for (int idx = 0; idx < resources.size(); ++idx)
      {
         Resource resource = (Resource) resources.get(idx);
         try
         {
            resource.endResource();
         }
         catch(XAException xae)
         {
            logXAException(xae);
            status = Status.STATUS_MARKED_ROLLBACK;
            cause = xae;
         }
      }
      resourcesEnded = true; // Too late to enlist new resources.
   }


   /**
    *  Call synchronization <code>beforeCompletion()</code>.
    *  This will release the lock while calling out.
    */
   private void doBeforeCompletion()
   {
      unlock();
      try
      {
         for (int i = 0; i < syncCount; i++)
         {
            try
            {
               if (trace)
                  log.trace("calling sync " + i + ", " + sync[i] + " tx=" + this);

               sync[i].beforeCompletion();
            }
            catch (Throwable t)
            {
               if (trace)
                  log.trace("failed before completion " + sync[i], t);

               status = Status.STATUS_MARKED_ROLLBACK;

               // save the cause off so the user can inspect it
               cause = t;
               break;
            }
         }
      }
      finally
      {
         lock();
      }
   }

   /**
    *  Call synchronization <code>afterCompletion()</code>.
    *  This will release the lock while calling out.
    */
   private void doAfterCompletion()
   {
      // Assert: Status indicates: Too late to add new synchronizations.
      unlock();
      try
      {
         for (int i = 0; i < syncCount; i++)
         {
            try
            {
               sync[i].afterCompletion(status);
            }
            catch (Throwable t)
            {
               if (trace)
                  log.trace("failed after completion " + sync[i], t);
            }
         }
      }
      finally
      {
         lock();
      }
   }

   /**
    *  We got another heuristic.
    *
    *  Promote <code>heuristicCode</code> if needed and tell
    *  the resource to forget the heuristic.
    *  This will release the lock while calling out.
    *
    *  @param resource The resource of the XA resource that got a
    *         heurictic in our internal tables, or <code>null</code>
    *         if the heuristic came from here.
    *  @param code The heuristic code, one of
    *         <code>XAException.XA_HEURxxx</code>.
    */
   private void gotHeuristic(Resource resource, int code)
   {
      switch (code)
      {
         case XAException.XA_HEURMIX:
            heuristicCode = XAException.XA_HEURMIX;
            break;
         case XAException.XA_HEURRB:
            if (heuristicCode == HEUR_NONE)
               heuristicCode = XAException.XA_HEURRB;
            else if (heuristicCode == XAException.XA_HEURCOM ||
            heuristicCode == XAException.XA_HEURHAZ)
               heuristicCode = XAException.XA_HEURMIX;
            break;
         case XAException.XA_HEURCOM:
            if (heuristicCode == HEUR_NONE)
               heuristicCode = XAException.XA_HEURCOM;
            else if (heuristicCode == XAException.XA_HEURRB ||
            heuristicCode == XAException.XA_HEURHAZ)
               heuristicCode = XAException.XA_HEURMIX;
            break;
         case XAException.XA_HEURHAZ:
            if (heuristicCode == HEUR_NONE)
               heuristicCode = XAException.XA_HEURHAZ;
            else if (heuristicCode == XAException.XA_HEURCOM ||
            heuristicCode == XAException.XA_HEURRB)
               heuristicCode = XAException.XA_HEURMIX;
            break;
         default:
            throw new IllegalArgumentException();
      }

      if (resource != null)
         resource.forget();
   }

   /**
    *  Check for heuristics, clear and throw exception if any found.
    */
   private void checkHeuristics()
      throws HeuristicMixedException, HeuristicRollbackException
   {
      switch (heuristicCode)
      {
         case XAException.XA_HEURHAZ:
         case XAException.XA_HEURMIX:
            heuristicCode = HEUR_NONE;
            if (trace)
               log.trace("Throwing HeuristicMixedException, tx=" + this +
               "status=" + getStringStatus(status));
            throw new HeuristicMixedException();
         case XAException.XA_HEURRB:
            heuristicCode = HEUR_NONE;
            if (trace)
               log.trace("Throwing HeuristicRollbackException, tx=" + this +
               "status=" + getStringStatus(status));
            throw new HeuristicRollbackException();
         case XAException.XA_HEURCOM:
            heuristicCode = HEUR_NONE;
            // Why isn't HeuristicCommitException used in JTA ?
            // And why define something that is not used ?
            // For now we just have to ignore this failure, even if it happened
            // on rollback.
            if (trace)
               log.trace("NOT Throwing HeuristicCommitException, tx=" + this +
               "status=" + getStringStatus(status));
            return;
      }
   }


   /**
    *  Prepare all enlisted resources.
    *  If the first phase of the commit process results in a decision
    *  to commit the <code>status</code> will be
    *  <code>Status.STATUS_PREPARED</code> on return.
    *  Otherwise the <code>status</code> will be
    *  <code>Status.STATUS_MARKED_ROLLBACK</code> on return.
    *  This will release the lock while calling out.
    *
    *  @return True iff all resources voted read-only.
    */
   private boolean prepareResources()
   {
      boolean readOnly = true;

      status = Status.STATUS_PREPARING;

      // Prepare te XAResources
      for (int i = 0; i < resources.size(); ++i)
      {
         // Abort prepare on state change.
         if (status != Status.STATUS_PREPARING)
            return false;

         Resource resource = (Resource) resources.get(i);

         if (resource.isResourceManager() == false)
            continue; // This RM already prepared.

         // Ignore the last resource it is done later
         if (resource == lastResource)
            continue;

         try
         {
            int vote = resource.prepare();

            if (vote == RS_VOTE_OK)
               readOnly = false;
            else if (vote != RS_VOTE_READONLY)
            {
               // Illegal vote: rollback.
               if (trace)
                  log.trace("illegal vote in prepare resources tx=" + this + " resource=" + resource, new Exception());
               status = Status.STATUS_MARKED_ROLLBACK;
               return false;
            }
         }
         catch (XAException e)
         {
            readOnly = false;

            logXAException(e);

            switch (e.errorCode)
            {
            case XAException.XA_HEURCOM:
               // Heuristic commit is not that bad when preparing.
               // But it means trouble if we have to rollback.
               gotHeuristic(resource, e.errorCode);
               break;
            case XAException.XA_HEURRB:
            case XAException.XA_HEURMIX:
            case XAException.XA_HEURHAZ:
               gotHeuristic(resource, e.errorCode);
               if (status == Status.STATUS_PREPARING)
                  status = Status.STATUS_MARKED_ROLLBACK;
               break;
            default:
               cause = e;
               if (status == Status.STATUS_PREPARING)
                  status = Status.STATUS_MARKED_ROLLBACK;
               break;
            }
         }
         catch (Throwable t)
         {
            if (trace)
               log.trace("unhandled throwable in prepareResources " + this, t);
            if (status == Status.STATUS_PREPARING)
               status = Status.STATUS_MARKED_ROLLBACK;
            cause = t;
         }
      }

      // Abort prepare on state change.
      if (status != Status.STATUS_PREPARING)
         return false;

      // Are we doing the last resource gambit?
      if (lastResource != null)
      {
         try
         {
            lastResource.prepareLastResource();
            lastResource.commit(false);
         }
         catch (XAException e)
         {
            logXAException(e);
            switch (e.errorCode)
            {
            case XAException.XA_HEURRB:
            case XAException.XA_HEURCOM:
            case XAException.XA_HEURMIX:
            case XAException.XA_HEURHAZ:
               //usually throws an exception, but not for a couple of cases.
               gotHeuristic(lastResource, e.errorCode);
               if (status == Status.STATUS_PREPARING)
                  status = Status.STATUS_MARKED_ROLLBACK;
               break;
            default:
               cause = e;
               if (status == Status.STATUS_PREPARING)
                  status = Status.STATUS_MARKED_ROLLBACK;
               break;
            }
         }
         catch (Throwable t)
         {
            if (trace)
               log.trace("unhandled throwable in prepareResources " + this, t);
            if (status == Status.STATUS_PREPARING)
               status = Status.STATUS_MARKED_ROLLBACK;
            cause = t;
         }
      }

      if (status == Status.STATUS_PREPARING)
         status = Status.STATUS_PREPARED;
      else
         return false;

      return readOnly;
   }

   /**
    *  Commit all enlisted resources.
    *  This will release the lock while calling out.
    */
   private void commitResources(boolean onePhase)
   {
      status = Status.STATUS_COMMITTING;

      for (int i = 0; i < resources.size(); ++i)
      {

         // Abort commit on state change.
         if (status != Status.STATUS_COMMITTING)
            return;

         Resource resource = (Resource) resources.get(i);

         // Ignore the last resource, it is already committed
         if (onePhase == false && lastResource == resource)
            continue;

         try
         {
            resource.commit(onePhase);
         }
         catch (XAException e)
         {
            logXAException(e);
            switch (e.errorCode) {
               case XAException.XA_HEURRB:
               case XAException.XA_HEURCOM:
               case XAException.XA_HEURMIX:
               case XAException.XA_HEURHAZ:
                  //usually throws an exception, but not for a couple of cases.
                  gotHeuristic(resource, e.errorCode);
                  //May not be correct for HEURCOM
                  //Two phase commit is committed after prepare is logged.
                  if (onePhase)
                     status = Status.STATUS_MARKED_ROLLBACK;

                  break;
               default:
                  cause = e;
                  if (onePhase)
                  {
                     status = Status.STATUS_MARKED_ROLLBACK;
                     break;
                  }
                  //Not much we can do if there is an RMERR in the
                  //commit phase of 2pc. I guess we try the other rms.
            }
         }
         catch (Throwable t)
         {
            if (trace)
               log.trace("unhandled throwable in commitResources " + this, t);
         }
      }

      if (status == Status.STATUS_COMMITTING)
         status = Status.STATUS_COMMITTED;
   }

   /**
    *  Rollback all enlisted resources.
    *  This will release the lock while calling out.
    */
   private void rollbackResources()
   {
      status = Status.STATUS_ROLLING_BACK;

      for (int i = 0; i < resources.size(); ++i)
      {
         Resource resource = (Resource) resources.get(i);
         try
         {
            resource.rollback();
         }
         catch (XAException e)
         {
            logXAException(e);
            switch (e.errorCode)
            {
               case XAException.XA_HEURRB:
                  // Heuristic rollback is not that bad when rolling back.
                  gotHeuristic(resource, e.errorCode);
                  continue;
               case XAException.XA_HEURCOM:
               case XAException.XA_HEURMIX:
               case XAException.XA_HEURHAZ:
                  gotHeuristic(resource, e.errorCode);
                  continue;
               default:
                  cause = e;
                  break;
            }
         }
         catch (Throwable t)
         {
            if (trace)
               log.trace("unhandled throwable in rollbackResources " + this, t);
         }
      }

      status = Status.STATUS_ROLLEDBACK;
   }

   /**
    *  Create an Xid representing a new branch of this transaction.
    */
   private Xid createXidBranch()
   {
      long branchId = ++lastBranchId;

      return xidFactory.newBranch(xid, branchId);
   }

   /**
    * Determine the commit strategy
    *
    * @return 0 for nothing to do, 1 for one phase and 2 for two phase
    */
   private int getCommitStrategy()
   {
      int resourceCount = resources.size();

      if (resourceCount == 0)
         return 0;

      if (resourceCount == 1)
         return 1;

      // first XAResource surely has -1, it's the first!
      for (int i = 1; i < resourceCount; ++i)
      {
         Resource resource = (Resource) resources.get(i);
         if (resource.isResourceManager())
         {
            // this one is not the same rm as previous ones,
            // there must be at least 2
            return 2;
         }

      }
      // all rms are the same one, one phase commit is ok.
      return 1;
   }

   public long getTimeLeftBeforeTimeout(boolean errorRollback) throws RollbackException
   {
      if (errorRollback && status != Status.STATUS_ACTIVE)
         throw new RollbackException("Transaction is not active: " + TxUtils.getStatusAsString(status));
      return (start + timeoutPeriod) - System.currentTimeMillis();
   }

   Object getTransactionLocalValue(TransactionLocal tlocal)
   {
      return transactionLocalMap.get(tlocal);
   }

   void putTransactionLocalValue(TransactionLocal tlocal, Object value)
   {
      transactionLocalMap.put(tlocal, value);
   }

   boolean containsTransactionLocal(TransactionLocal tlocal)
   {
      return transactionLocalMap.containsKey(tlocal);
   }

   /**
    * Check we have no outstanding work
    *
    * @throws IllegalStateException when there is still work
    */
   private void checkWork()
   {
      if (work != null)
         throw new IllegalStateException("Work still outstanding " + work + " tx=" + this);
   }

   // Inner classes -------------------------------------------------

   /**
    * Represents a resource enlisted in the transaction
    */
   private class Resource
   {
      /** The XAResource */
      private XAResource xaResource;

      /** The state of the resources */
      private int resourceState;

      /** The related xa resource from the same resource manager */
      private Resource resourceSameRM;

      /** The Xid of this resource */
      private Xid resourceXid;

      /**
       * Create a new resource
       */
      public Resource(XAResource xaResource, Xid resourceXid, Resource resourceSameRM)
      {
         this.xaResource = xaResource;
         this.resourceXid = resourceXid;
         this.resourceSameRM = resourceSameRM;
         resourceState = RS_NEW;
      }

      /**
       * Get the XAResource for this resource
       */
      public XAResource getXAResource()
      {
         return xaResource;
      }

      /**
       * Get the Xid for this resource
       */
      public Xid getXid()
      {
         return resourceXid;
      }

      /**
       * Is the resource enlisted?
       */
      public boolean isEnlisted()
      {
         return resourceState == RS_ENLISTED;
      }

      /**
       * Is this a resource manager
       */
      public boolean isResourceManager()
      {
         return resourceSameRM == null;
      }

      /**
       * Is this the resource manager for the passed xa resource
       */
      public boolean isResourceManager(XAResource xaRes) throws XAException
      {
         return resourceSameRM == null && xaRes.isSameRM(xaResource);
      }

      /**
       * Is the resource delisted and the XAResource always returns false
       * for isSameRM
       */
      public boolean isDelisted(XAResource xaRes) throws XAException
      {
         return resourceState == RS_ENDED && xaResource.isSameRM(xaRes) == false;
      }

      /**
       * Call <code>start()</code> on a XAResource and update
       * internal state information.
       * This will release the lock while calling out.
       *
       * @return when started, false otherwise
       */
      public boolean startResource()
         throws XAException
      {
         int flags = XAResource.TMJOIN;

         if (resourceSameRM == null)
         {
            switch (resourceState)
            {
            case RS_NEW:
               flags = XAResource.TMNOFLAGS;
               break;
            case RS_SUSPENDED:
               flags = XAResource.TMRESUME;
               break;

            default:
               if (trace)
                  log.trace("Unhandled resource state: " + resourceState +
                  " (not RS_NEW or RS_SUSPENDED, using TMJOIN flags)");
            }
         }

         if (trace)
            log.trace("startResource(" +
                  xidFactory.toString(resourceXid) +
                  ") entered: " + xaResource.toString() +
                  " flags=" + flags);

         unlock();
         // OSH FIXME: resourceState could be incorrect during this callout.
         try
         {
            try
            {
               xaResource.start(resourceXid, flags);
            }
            catch(XAException e)
            {
               throw e;
            }
            catch (Throwable t)
            {
               if (trace)
                  log.trace("unhandled throwable error in startResource", t);
               status = Status.STATUS_MARKED_ROLLBACK;
               return false;
            }

            // Now the XA resource is associated with a transaction.
            resourceState = RS_ENLISTED;
         }
         finally
         {
            lock();
            if (trace)
               log.trace("startResource(" +
                     xidFactory.toString(resourceXid) +
                     ") leaving: " + xaResource.toString() +
                     " flags=" + flags);
         }
         return true;
      }

      /**
       * Delist the resource unless we already did it
       */
      public boolean delistResource(XAResource xaRes, int flag) throws XAException
      {
         if (isDelisted(xaRes))
         {
            // This RM always returns false on isSameRM.  Further,
            // the last resource has already been delisted.
            log.warn("Resource already delisted.  tx=" + this.toString());
            return false;
         }
         endResource(flag);
         return true;
      }

      /**
       * End the resource
       */
      public void endResource() throws XAException
      {
         if (resourceState == RS_ENLISTED || resourceState == RS_SUSPENDED)
         {
            if (trace)
               log.trace("endresources(" + xaResource + "): state=" +
                     resourceState);
            endResource(XAResource.TMSUCCESS);
         }
      }

      /**
       *  Call <code>end()</code> on the XAResource and update
       *  internal state information.
       *  This will release the lock while calling out.
       *
       *  @param flag The flag argument for the end() call.
       */
      private void endResource(int flag) throws XAException
      {
         if (trace)
            log.trace("endResource(" +
                  xidFactory.toString(resourceXid) +
                  ") entered: " + xaResource.toString() +
                  " flag=" + flag);

         unlock();
         // OSH FIXME: resourceState could be incorrect during this callout.
         try
         {
            try
            {
               xaResource.end(resourceXid, flag);
            }
            catch(XAException e)
            {
               throw e;
            }
            catch (Throwable t)
            {
               if (trace)
                  log.trace("unhandled throwable error in endResource", t);
               status = Status.STATUS_MARKED_ROLLBACK;
               // Resource may or may not be ended after illegal exception.
               // We just assume it ended.
               resourceState = RS_ENDED;
               return;
            }

            // Update our internal state information
            if (flag == XAResource.TMSUSPEND)
               resourceState = RS_SUSPENDED;
            else
            {
               if (flag == XAResource.TMFAIL)
                  status = Status.STATUS_MARKED_ROLLBACK;
               resourceState = RS_ENDED;
            }
         }
         finally
         {
            lock();
            if (trace)
               log.trace("endResource(" +
                     xidFactory.toString(resourceXid) +
                     ") leaving: " + xaResource.toString() +
                     " flag=" + flag);
         }
      }

      /**
       * Forget the resource
       */
      public void forget()
      {
         unlock();
         try
         {
            xaResource.forget(resourceXid);
         }
         catch (XAException xae)
         {
            logXAException(xae);
            cause = xae;
         }
         finally
         {
            lock();
         }
         resourceState = RS_FORGOT;
      }

      /**
       * Prepare the resource
       */
      public int prepare() throws XAException
      {
         int vote;
         unlock();
         try
         {
            vote = xaResource.prepare(resourceXid);
         }
         finally
         {
            lock();
         }

         if (vote == XAResource.XA_OK)
            resourceState = RS_VOTE_OK;
         else if (vote == XAResource.XA_RDONLY)
            resourceState = RS_VOTE_READONLY;

         return resourceState;
      }

      /**
       * Prepare the last resource
       */
      public void prepareLastResource() throws XAException
      {
         resourceState = RS_VOTE_OK;
      }

      /**
       * Commit the resource
       */
      public void commit(boolean onePhase) throws XAException
      {
         if (trace)
            log.trace("Committing resource " + xaResource + " state=" +
                  resourceState);

         if (!onePhase && resourceState != RS_VOTE_OK)
            return; // Voted read-only at prepare phase.

         if (resourceSameRM != null)
            return; // This RM already committed.

         unlock();
         try
         {
            xaResource.commit(resourceXid, onePhase);
         }
         finally
         {
            lock();
         }
      }

      /**
       * Rollback the resource
       */
      public void rollback() throws XAException
      {
         if (resourceState == RS_VOTE_READONLY)
            return;
         // Already forgotten
         if (resourceState == RS_FORGOT)
            return;
         if (resourceSameRM != null)
            return; // This RM already rolled back.

         unlock();
         try
         {
            xaResource.rollback(resourceXid);
         }
         finally
         {
            lock();
         }
      }
   }
}
