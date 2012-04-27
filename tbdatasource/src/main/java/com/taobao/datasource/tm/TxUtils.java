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

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;

import org.jboss.util.NestedRuntimeException;

/**
 * TxUtils.java has utility methods for determining transaction status
 * in various useful ways.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 63567 $
 */
public class TxUtils
{
   /** Transaction Status Strings */
   private static final String[] TxStatusStrings =
   {
      "STATUS_ACTIVE",
      "STATUS_MARKED_ROLLBACK",
      "STATUS_PREPARED",
      "STATUS_COMMITTED",
      "STATUS_ROLLEDBACK",
      "STATUS_UNKNOWN",
      "STATUS_NO_TRANSACTION",
      "STATUS_PREPARING",
      "STATUS_COMMITTING",
      "STATUS_ROLLING_BACK"
   };

   /**
    * Do now allow instances of this class
    */
   private TxUtils()
   {

   }

   public static boolean isActive(Transaction tx)
   {
      if (tx == null)
         return false;

      try
      {
         int status = tx.getStatus();
         return isActive(status);
      }
      catch (SystemException error)
      {
         throw new NestedRuntimeException(error);
      }
   }

   public static boolean isActive(TransactionManager tm)
   {
      try
      {
         return isActive(tm.getTransaction());
      }
      catch (SystemException error)
      {
         throw new NestedRuntimeException(error);
      }
   }

   public static boolean isActive(UserTransaction ut)
   {
      try
      {
         int status = ut.getStatus();
         return isActive(status);
      }
      catch (SystemException error)
      {
         throw new NestedRuntimeException(error);
      }
   }

   public static boolean isActive(int status)
   {
      return status == Status.STATUS_ACTIVE;
   }

   public static boolean isUncommitted(Transaction tx)
   {
      if (tx == null)
         return false;

      try
      {
         int status = tx.getStatus();
         return isUncommitted(status);
      }
      catch (SystemException error)
      {
         throw new NestedRuntimeException(error);
      }
   }

   public static boolean isUncommitted(TransactionManager tm)
   {
      try
      {
         return isUncommitted(tm.getTransaction());
      }
      catch (SystemException error)
      {
         throw new NestedRuntimeException(error);
      }
   }

   public static boolean isUncommitted(UserTransaction ut)
   {
      try
      {
         int status = ut.getStatus();
         return isUncommitted(status);

      }
      catch (SystemException error)
      {
         throw new NestedRuntimeException(error);
      }
   }

   public static boolean isUncommitted(int status)
   {
      return status == Status.STATUS_ACTIVE
          || status == Status.STATUS_MARKED_ROLLBACK;
   }

   public static boolean isCompleted(Transaction tx)
   {
      if (tx == null)
         return true;

      try
      {
         int status = tx.getStatus();
         return isCompleted(status);
      }
      catch (SystemException error)
      {
         throw new NestedRuntimeException(error);
      }
   }

   public static boolean isCompleted(TransactionManager tm)
   {
      try
      {
         return isCompleted(tm.getTransaction());
      }
      catch (SystemException error)
      {
         throw new NestedRuntimeException(error);
      }
   }

   public static boolean isCompleted(UserTransaction ut)
   {
      try
      {
         int status = ut.getStatus();
         return isCompleted(status);

      }
      catch (SystemException error)
      {
         throw new NestedRuntimeException(error);
      }
   }

   public static boolean isCompleted(int status)
   {
      return status == Status.STATUS_COMMITTED
          || status == Status.STATUS_ROLLEDBACK
          || status == Status.STATUS_NO_TRANSACTION;
   }

   public static boolean isRollback(Transaction tx)
   {
      if (tx == null)
         return false;

      try
      {
         int status = tx.getStatus();
         return isRollback(status);
      }
      catch (SystemException error)
      {
         throw new NestedRuntimeException(error);
      }
   }

   public static boolean isRollback(TransactionManager tm)
   {
      try
      {
         return isRollback(tm.getTransaction());
      }
      catch (SystemException error)
      {
         throw new NestedRuntimeException(error);
      }
   }

   public static boolean isRollback(UserTransaction ut)
   {
      try
      {
         int status = ut.getStatus();
         return isRollback(status);
      }
      catch (SystemException error)
      {
         throw new NestedRuntimeException(error);
      }
   }

   public static boolean isRollback(int status)
   {
      return status == Status.STATUS_MARKED_ROLLBACK
          || status == Status.STATUS_ROLLING_BACK
          || status == Status.STATUS_ROLLEDBACK;
   }

   /**
    * Converts a tx Status index to a String
    *
    * @see javax.transaction.Status
    *
    * @param status the Status index
    * @return status as String or "STATUS_INVALID"
    */
   public static String getStatusAsString(int status)
   {
      if (status >= Status.STATUS_ACTIVE && status <= Status.STATUS_ROLLING_BACK)
      {
         return TxStatusStrings[status];
      }
      else
      {
         return "STATUS_INVALID";
      }
   }

   /**
    * Converts a XAResource flag to a String
    *
    * @see javax.transaction.xa.XAResource
    *
    * @param flags the flags passed in to start(), end(), recover()
    * @return the flags in String form
    */
   public static String getXAResourceFlagsAsString(int flags)
   {
      if (flags == XAResource.TMNOFLAGS)
      {
         return "|TMNOFLAGS";
      }
      else
      {
         StringBuffer sbuf = new StringBuffer(64);

         if ((flags & XAResource.TMONEPHASE) != 0)
         {
            sbuf.append("|TMONEPHASE");
         }
         if ((flags & XAResource.TMJOIN) != 0)
         {
            sbuf.append("|TMJOIN");
         }
         if ((flags & XAResource.TMRESUME) != 0)
         {
            sbuf.append("|TMRESUME");
         }
         if ((flags & XAResource.TMSUCCESS) != 0)
         {
            sbuf.append("|TMSUCCESS");
         }
         if ((flags & XAResource.TMFAIL) != 0)
         {
            sbuf.append("|TMFAIL");
         }
         if ((flags & XAResource.TMSUSPEND) != 0)
         {
            sbuf.append("|TMSUSPEND");
         }
         if ((flags & XAResource.TMSTARTRSCAN) != 0)
         {
            sbuf.append("|TMSTARTRSCAN");
         }
         if ((flags & XAResource.TMENDRSCAN) != 0)
         {
            sbuf.append("|TMENDRSCAN");
         }
         return sbuf.toString();
      }
   }
}
