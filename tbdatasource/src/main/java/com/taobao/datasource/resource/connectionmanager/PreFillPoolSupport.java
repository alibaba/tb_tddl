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

import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

/**
 * PreFillPoolSupport allows for prefilling connection pools.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 57189 $
 */
public interface PreFillPoolSupport
{
   
   /**
    * Prefill the connection pool 
    * 
    */
   public void prefill();
   
   /**
    * Prefill the connection pool
    * 
    * @param noTxSeperatePool whether or not we are seperating non transaction and transaction pools
    */
   public void prefill(boolean noTxSeperatePool);
   
   /**
    * Prefill the connection pool
    * 
    * @param subject the subject the subject 
    * @param cri the connection request info
    * @param noTxnSeperatePool whether or not we are seperating non transaction and transaction pools
    *   
    */
   public void prefill(Subject subject, ConnectionRequestInfo cri, boolean noTxnSeperatePool);

   /**
    * Get the flag indicating whether or not to attempt to prefill this pool.
    * 
    * @return true or false depending on whether or not to prefill this pool.
    */
   public boolean shouldPreFill();
}
