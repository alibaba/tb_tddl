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

/**
 * MBean interface.
 */
public interface XidFactoryMBean
{

   /**
    * mbean get-set pair for field BaseGlobalId Get the value of BaseGlobalId
    * @return value of BaseGlobalId
    */
   java.lang.String getBaseGlobalId();

   /**
    * Set the value of BaseGlobalId
    * @param BaseGlobalId Value to assign to BaseGlobalId
    */
   void setBaseGlobalId(java.lang.String baseGlobalId);

   /**
    * mbean get-set pair for field globalIdNumber Get the value of globalIdNumber
    * @return value of globalIdNumber
    */
   long getGlobalIdNumber();

   /**
    * Set the value of globalIdNumber
    * @param globalIdNumber Value to assign to globalIdNumber
    */
   void setGlobalIdNumber(long globalIdNumber);

   /**
    * mbean get-set pair for field pad Get the value of pad
    * @return value of pad
    */
   boolean isPad();

   /**
    * Set the value of pad
    * @param pad Value to assign to pad
    */
   void setPad(boolean pad);

   /**
    * mbean get-set pair for field instance Get the value of instance
    * @return value of instance
    */
   com.taobao.datasource.tm.XidFactoryMBean getInstance();

   /**
    * Describe <code>newXid</code> method here.
    * @return a <code>XidImpl</code> value
    */
   com.taobao.datasource.tm.XidImpl newXid();

   /**
    * Describe <code>newBranch</code> method here.
    * @param xid a <code>XidImpl</code> value
    * @param branchIdNum a <code>long</code> value
    * @return a <code>XidImpl</code> value
    */
   com.taobao.datasource.tm.XidImpl newBranch(com.taobao.datasource.tm.XidImpl xid, long branchIdNum);

   /**
    * Extracts the local id contained in a global id.
    * @param globalId a global id
    * @return the local id extracted from the global id
    */
   long extractLocalIdFrom(byte[] globalId);

   /**
    * Describe <code>toString</code> method here.
    * @param xid a <code>Xid</code> value
    * @return a <code>String</code> value
    */
   java.lang.String toString(javax.transaction.xa.Xid xid);

}
