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

import javax.transaction.Transaction;


/**
 *  Implementations of this interface are used for importing a transaction
 *  propagation context into the transaction manager.
 *
 *  @see TransactionPropagationContextFactory
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 57208 $
 */
public interface TransactionPropagationContextImporter
{
   /**
    *  Import the transaction propagation context into the transaction
    *  manager, and return the resulting transaction.
    *  If this transaction propagation context has already been imported
    *  into the transaction manager, this method simply returns the
    *  <code>Transaction</code> representing the transaction propagation
    *  context in the local VM.
    *  Returns <code>null</code> if the transaction propagation context is
    *  <code>null</code>, or if it represents a <code>null</code> transaction.
    */
   public Transaction importTransactionPropagationContext(Object tpc);
}

