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

import java.io.IOException;
import javax.transaction.xa.Xid;

/**
 *  This object encapsulates the global transaction ID of a transaction.
 *  It is similar to an Xid, but holds only the GlobalId part.
 *  This implementation is immutable and always serializable at runtime.
 *
 *  @see XidImpl
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @author <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 *  @version $Revision: 57208 $
 */
public class GlobalId
   implements java.io.Externalizable
{
   static final long serialVersionUID = 6879509375433435464L;
   /**
    *  Format id of this instance.
    */
	private int formatId;

   /**
    *  Global transaction id of this instance.
    *  The coding of this class depends on the fact that this variable is
    *  initialized in the constructor and never modified. References to
    *  this array are never given away, instead a clone is delivered.
    */
   private byte[] globalId;

   /**
    *  Hash code of this instance. For a native GlobalId (one whose formatId
    *  is XidImpl.JBOSS_FORMAT_ID), this is really a sequence number.
    */
   private int hash;

   // Constructors --------------------------------------------------

   public GlobalId()
   {
      // Used for Externalizable support
   }
   
   /**
    *  Create a new instance. This constructor is package-private, as it
    *  trusts the hash parameter to be good.
    */
   GlobalId(int formatId, byte[] globalId, int hash)
   {
      this.formatId = formatId;
      this.globalId = globalId;
      this.hash = hash;
   }

   /**
    *  Create a new instance. This constructor is public <em>only</em>
    *  to get around a class loader problem; it should be package-private.
    */
   public GlobalId(int formatId, byte[] globalId)
   {
      this.formatId = formatId;
      this.globalId = globalId;
      hash = computeHash();
   }

   public GlobalId(Xid xid)
   {
      formatId = xid.getFormatId();
      globalId = xid.getGlobalTransactionId();
      if (xid instanceof XidImpl && formatId == XidImpl.JBOSS_FORMAT_ID) 
      {
         // native GlobalId: use its hash code (a sequence number)
         hash = xid.hashCode();
      }
      else 
      {
         // foreign GlobalId: do the hash computation
         hash = computeHash();
      }
   }

   public GlobalId(int formatId, int bqual_length, byte[] tid)
   {
      this.formatId = formatId;
      if (bqual_length == 0)
         globalId = tid;
      else 
      {
         int len = tid.length - bqual_length;
         globalId = new byte[len];
         System.arraycopy(tid, 0, globalId, 0, len);
      }
      hash = computeHash();
   }

   // Public --------------------------------------------------------

   /**
    *  Compare for equality.
    *
    *  Instances are considered equal if they both refer to the same
    *  global transaction id.
    */
   public boolean equals(Object obj)
   {
      if (obj instanceof GlobalId) {
         GlobalId other = (GlobalId)obj;

         if (formatId != other.formatId)
            return false;

         if (globalId == other.globalId)
            return true;

         if (globalId.length != other.globalId.length)
            return false;

         int len = globalId.length;
         for (int i = 0; i < len; ++i)
            if (globalId[i] != other.globalId[i])
               return false;

         return true;
      }
      return false;
   }

   public int hashCode()
   {
      return hash;
   }
   
   public String toString() 
   {
      return getClass().getName() + "[formatId=" + formatId
            + ", globalId=" + new String(globalId).trim()
            + ", hash=" + hash + "]";
   }

   // Externalizable implementation ---------------------------------

   public void writeExternal(java.io.ObjectOutput out)
      throws IOException
   {
      out.writeInt(formatId);
      out.writeObject(globalId);
   }
   
   public void readExternal(java.io.ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      formatId = in.readInt();
      globalId = (byte[])in.readObject();
      hash = computeHash();
   }

   // Private -------------------------------------------------------

   private int computeHash()
   {
      if (formatId == XidImpl.JBOSS_FORMAT_ID)
      {
         return (int)TransactionImpl.xidFactory.extractLocalIdFrom(globalId);
      }
      else
      {
         int len = globalId.length;
         int hashval = 0;
         
         // TODO: use a better hash function
         for (int i = 0; i < len; ++i)
            hashval = 3 * globalId[i] + hashval;
         hashval += formatId;
         return hashval;
      }
   }

}
