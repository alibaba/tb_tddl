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

import com.taobao.datasource.tm.TransactionImpl;

/**
 * A policy that checks a transaction before allowing a commit
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57208 $
 */
public interface TransactionIntegrity
{
   /**
    * Checks whether a transaction can be committed.<p>
    * 
    * The policy is allowed to wait, e.g. if there
    * are other threads still associated with the transaction.<p>
    * 
    * This method is invoked before any transaction synchronizations'
    * beforeCompletions.<p>
    *
    * This policy should not invoke any methods that change the
    * state of the transaction other than <code>setRollbackOnly()</code>
    * to force a rollback or registering a transaction synchronization.
    * 
    * @param transaction the transaction
    * @throws SecurityException if a commit is not allowed from this context
    */
   void checkTransactionIntegrity(TransactionImpl transaction);
}
