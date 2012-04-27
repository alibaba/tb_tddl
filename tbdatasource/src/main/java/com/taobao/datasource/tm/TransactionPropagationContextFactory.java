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
 *  Implementations of this interface are used for getting
 *  a transaction propagation context at the client-side.
 *  We need a specific implementation of this interface for
 *  each kind of DTM we are going to interoperate with. (So
 *  we may have 20 new classes if we are going to interoperate
 *  with 20 different kinds of distributed transaction
 *  managers.)
 *  The reason for having the methods in this interface return
 *  Object is that we do not really know what kind of transaction
 *  propagation context is returned.
 *
 *  @see TransactionPropagationContextImporter
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 57208 $
 */
public interface TransactionPropagationContextFactory
{
   /**
    *  Return a transaction propagation context for the transaction
    *  currently associated with the invoking thread, or <code>null</code>
    *  if the invoking thread is not associated with a transaction.
    */
   public Object getTransactionPropagationContext();

   /**
    *  Return a transaction propagation context for the transaction
    *  given as an argument, or <code>null</code>
    *  if the argument is <code>null</code> or of a type unknown to
    *  this factory.
    */
   public Object getTransactionPropagationContext(Transaction tx);

}

