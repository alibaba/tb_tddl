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

import javax.transaction.Transaction;

import org.jboss.logging.Logger;

import com.taobao.datasource.tm.TransactionImpl;

/**
 * A NOOP implementation of transaction integrity.<p>
 * 
 * Implementations should extend this for future compatibility.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57208 $
 */
public class AbstractTransactionIntegrity implements TransactionIntegrity
{
   /** The log */
   protected Logger log = Logger.getLogger(getClass());
   
   public void checkTransactionIntegrity(TransactionImpl transaction)
   {
      // Do nothing
   }
   
   /**
    * Mark the transaction for rollback
    * 
    * @param transaction the transacton
    */
   protected void markRollback(Transaction transaction)
   {
      try
      {
         transaction.setRollbackOnly();
      }
      catch (Exception e)
      {
         log.warn("Unable to mark the transaction for rollback " + transaction, e);
      }
   }
}
