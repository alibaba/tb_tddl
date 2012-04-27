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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.taobao.datasource.resource.adapter.jdbc.ValidConnectionChecker;

/**
 * Checks a PostgreSQL to see if it is alive. Uses an empty query.
 * 
 * @author <a href="mike@middlesoft.co.uk">Michael Barker</a>
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 */
public class PostgreSQLValidConnectionChecker implements ValidConnectionChecker, Serializable

{
   private static final long serialVersionUID = 4867167301823753925L;

   /**
    * @see com.taobao.datasource.resource.adapter.jdbc.ValidConnectionChecker#isValidConnection(java.sql.Connection)
    */
   public SQLException isValidConnection(Connection cn)
   {
      SQLException ex = null;
      Statement stmt = null;
      try
      {
         stmt = cn.createStatement();
         stmt.execute("");
      }
      catch (SQLException e)
      {
         ex = e;
      }
      finally
      {
         if (stmt != null)
         {
            try
            {
               stmt.close();
            }
            catch (Exception e)
            {
            }
         }
      }
      return ex;
   }
}
