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
package com.taobao.datasource.resource.adapter.jdbc.vendor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import org.jboss.logging.Logger;
import org.jboss.util.NestedRuntimeException;

import com.taobao.datasource.resource.adapter.jdbc.ValidConnectionChecker;

/**
 * Implements check valid connection sql
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 57189 $
 */
public class OracleValidConnectionChecker
   implements ValidConnectionChecker, Serializable
{
   private static final long serialVersionUID = -2227528634302168877L;

   private static final Logger log = Logger.getLogger(OracleValidConnectionChecker.class);

   private Method ping;

   // The timeout (apparently the timeout is ignored?)
   private static Object[] params = new Object[] { new Integer(5000) };

   public OracleValidConnectionChecker()
   {
      try
      {
         Class oracleConnection = Thread.currentThread().getContextClassLoader().loadClass("oracle.jdbc.driver.OracleConnection");
         ping = oracleConnection.getMethod("pingDatabase", new Class[] { Integer.TYPE });
      }
      catch (Exception e)
      {
         throw new NestedRuntimeException("Unable to resolve pingDatabase method:", e);
      }
   }

   public SQLException isValidConnection(Connection c)
   {
      try
      {
         Integer status = (Integer) ping.invoke(c, params);

         // Error
         if (status.intValue() < 0)
            return new SQLException("pingDatabase failed status=" + status);
      }
      catch (Exception e)
      {
         // What do we do here? Assume it is a misconfiguration
         log.warn("Unexpected error in pingDatabase", e);
      }

      // OK
      return null;
   }
}
