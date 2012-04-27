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
package com.taobao.datasource.tm.integrity;

import java.util.Set;

import com.taobao.datasource.tm.TransactionImpl;

/**
 * A transaction integrity that rolls back the transaction
 * if there are other threads associated with it.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57208 $
 */
public class FailIncompleteTransactionIntegrity extends AbstractTransactionIntegrity
{
   @SuppressWarnings("unchecked")
public void checkTransactionIntegrity(TransactionImpl transaction)
   {
      // Assert the only thread is ourselves
      Set threads = transaction.getAssociatedThreads();
      String rollbackError = null;
      synchronized (threads)
      {
         if (threads.size() > 1)
            rollbackError = "Too many threads " + threads + " associated with transaction " + transaction;
         else if (threads.size() != 0)
         {
            Thread other = (Thread) threads.iterator().next();
            Thread current = Thread.currentThread();
            if (current.equals(other) == false)
               rollbackError = "Attempt to commit transaction " + transaction + " on thread " + current +
                          " with other threads still associated with the transaction " + other;
         }
      }
      if (rollbackError != null)
      {
         log.error(rollbackError, new IllegalStateException("STACKTRACE"));
         markRollback(transaction);
      }
   }
}
