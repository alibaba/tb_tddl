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

import javax.resource.spi.XATerminator;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkCompletedException;
import javax.transaction.xa.Xid;

/**
 * Extends XATerminator to include registration calls
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57208 $
 */
public interface JBossXATerminator extends XATerminator
{
   /**
    * Invoked for transaction inflow of work
    * 
    * @param work the work starting
    * @param xid the xid of the work
    * @param timeout the transaction timeout
    * @throws WorkCompletedException with error code WorkException.TX_CONCURRENT_WORK_DISALLOWED
    *         when work is already present for the xid or whose completion is in progress, only
    *         the global part of the xid must be used for this check.
    */
   void registerWork(Work work, Xid xid, long timeout) throws WorkCompletedException;
   
   /**
    * Invoked for transaction inflow of work
    * 
    * @param work the work starting
    * @param xid the xid of the work
    * @throws WorkCompletedException with error code WorkException.TX_RECREATE_FAILED if it is unable to recreate the transaction context
    */
   void startWork(Work work, Xid xid) throws WorkCompletedException;

   /**
    * Invoked when transaction inflow work ends
    * 
    * @param work the work ending
    * @param xid the xid of the work
    */
   void endWork(Work work, Xid xid);

   /**
    * Invoked when the work fails
    * 
    * @param work the work ending
    * @param xid the xid of the work
    */
   void cancelWork(Work work, Xid xid);
}
