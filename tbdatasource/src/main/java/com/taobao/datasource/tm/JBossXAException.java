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

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.transaction.xa.XAException;

import org.jboss.util.NestedThrowable;

/**
 * Thrown to indicate a problem with a xaresource related operation.
 *
 * <p>
 * Properly displays linked exception (ie. nested exception)
 * when printing the stack trace.
 *
 * @version <tt>$Revision: 57208 $</tt>
 * @author  <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JBossXAException
   extends XAException
   implements NestedThrowable
{
   /** The serial version uid*/
   private static final long serialVersionUID = 6614203184612359692L;

   /** The linked exception */
   Throwable linked;
   
   /**
    * Rethrow as an xa exception if it is not already
    * 
    * @param message the message
    * @param t the original exception
    * @throws XAException the xa exception
    */
   public static void rethrowAsXAException(String message, Throwable t) throws XAException
   {
      if (t instanceof XAException)
         throw (XAException) t;
      else
         throw new JBossXAException(message, t);
   }
   
   /**
    * Construct a <tt>JBossXAException</tt> with the specified detail
    * message.
    *
    * @param msg  Detail message.
    */
   public JBossXAException(final String msg)
   {
      super(msg);
   }

   /**
    * Construct a <tt>JBossXAException</tt> with the specified detail
    * message and error code.
    *
    * @param code  Error code.
    */
   public JBossXAException(final int code)
   {
      super(code);
   }

   /**
    * Construct a <tt>JBossXAException</tt> with the specified detail
    * message and linked <tt>Exception</tt>.
    *
    * @param msg     Detail message.
    * @param linked  Linked <tt>Exception</tt>.
    */
   public JBossXAException(final String msg, final Throwable linked)
   {
      super(msg);
      this.linked = linked;
   }

   /**
    * Construct a <tt>JBossXAException</tt> with the specified
    * linked <tt>Exception</tt>.
    *
    * @param linked  Linked <tt>Exception</tt>.
    */
   public JBossXAException(final Throwable linked)
   {
      this(linked.getMessage(), linked);
   }

   /**
    * Return the nested <tt>Throwable</tt>.
    *
    * @return  Nested <tt>Throwable</tt>.
    */
   public Throwable getNested()
   {
      return linked;
   }

   /**
    * Return the nested <tt>Throwable</tt>.
    *
    * <p>For JDK 1.4 compatibility.
    *
    * @return  Nested <tt>Throwable</tt>.
    */
   public Throwable getCause()
   {
      return linked;
   }

   /**
    * Returns the composite throwable message.
    *
    * @return  The composite throwable message.
    */
   public String getMessage()
   {
      return NestedThrowable.Util.getMessage(super.getMessage(), linked);
   }

   /**
    * Prints the composite message and the embedded stack trace to the
    * specified print stream.
    *
    * @param stream  Stream to print to.
    */
   public void printStackTrace(final PrintStream stream)
   {
      if (linked == null || NestedThrowable.PARENT_TRACE_ENABLED)
         super.printStackTrace(stream);
      NestedThrowable.Util.print(linked, stream);
   }

   /**
    * Prints the composite message and the embedded stack trace to the
    * specified print writer.
    *
    * @param writer  Writer to print to.
    */
   public void printStackTrace(final PrintWriter writer)
   {
      if (linked == null || NestedThrowable.PARENT_TRACE_ENABLED)
         super.printStackTrace(writer);
      NestedThrowable.Util.print(linked, writer);
   }

   /**
    * Prints the composite message and the embedded stack trace to
    * <tt>System.err</tt>.
    */
   public void printStackTrace()
   {
      printStackTrace(System.err);
   }
}
