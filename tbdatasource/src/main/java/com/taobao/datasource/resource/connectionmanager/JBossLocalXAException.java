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

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.transaction.xa.XAException;

import org.jboss.util.NestedThrowable;

/**
 * JBossLocalXAException
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57189 $
 */
public class JBossLocalXAException extends XAException implements NestedThrowable
{
   private static final long serialVersionUID = -6208145503935506281L;

   private final Throwable t;

   public JBossLocalXAException()
   {
      super();
      t = null;
   }

   public JBossLocalXAException(int errcode)
   {
      super(errcode);
      t = null;
   }

   public JBossLocalXAException(String message)
   {
      super(message);
      t = null;
   }

   public JBossLocalXAException(String message, int errorcode)
   {
      super(message);
      this.errorCode = errorcode;
      t = null;
   }

   public JBossLocalXAException(String message, Throwable t)
   {
      super(message);
      this.t = t;
   }

   public JBossLocalXAException(String message, int errorcode, Throwable t)
   {
      super(message);
      this.errorCode = errorcode;
      this.t = t;
   }

   // Implementation of org.jboss.util.NestedThrowable

   public Throwable getNested()
   {
      return t;
   }

   public Throwable getCause()
   {
      return t;
   }

   /**
    * Returns the composite throwable message.
    *
    * @return  The composite throwable message.
    */
   public String getMessage()
   {
      return NestedThrowable.Util.getMessage(super.getMessage(), t);
   }

   /**
    * Prints the composite message and the embedded stack trace to the
    * specified print stream.
    *
    * @param stream  Stream to print to.
    */
   public void printStackTrace(final PrintStream stream)
   {
      if (t == null || NestedThrowable.PARENT_TRACE_ENABLED)
         super.printStackTrace(stream);
      NestedThrowable.Util.print(t, stream);
   }

   /**
    * Prints the composite message and the embedded stack trace to the
    * specified print writer.
    *
    * @param writer  Writer to print to.
    */
   public void printStackTrace(final PrintWriter writer)
   {
      if (t == null || NestedThrowable.PARENT_TRACE_ENABLED)
         super.printStackTrace(writer);
      NestedThrowable.Util.print(t, writer);
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
