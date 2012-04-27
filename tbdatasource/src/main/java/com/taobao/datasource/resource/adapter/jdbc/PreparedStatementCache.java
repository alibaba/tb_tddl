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
package com.taobao.datasource.resource.adapter.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jboss.logging.Logger;
import org.jboss.util.LRUCachePolicy;

/**
 * LRU cache for PreparedStatements.  When ps ages out, close it.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author Scott.Stark@jboss.org 
 * @version $Revision: 57189 $
 */
public class PreparedStatementCache extends LRUCachePolicy
{
   private final Logger log = Logger.getLogger(getClass());

   public static class Key
   {
      public static final int PREPARED_STATEMENT = 1;
      public static final int CALLABLE_STATEMENT = 2;
      private final String sql;
      private final int type;
      private final int resultSetType;
      private final int resultSetConcurrency;

      public Key(String sql, int type, int resultSetType, int resultSetConcurrency)
      {
         this.sql = sql;
         this.type = type;
         this.resultSetType = resultSetType;
         this.resultSetConcurrency = resultSetConcurrency;
      }

      public boolean equals(Object o)
      {
         if (this == o) return true;
         if (o == null || o instanceof Key == false) return false;

         final Key key = (Key) o;

         if (resultSetConcurrency != key.resultSetConcurrency) return false;
         if (resultSetType != key.resultSetType) return false;
         if (type != key.type) return false;
         return !(sql != null ? !sql.equals(key.sql) : key.sql != null);

      }

      public int hashCode()
      {
         int result;
         result = (sql != null ? sql.hashCode() : 0);
         result = 29 * result + type;
         result = 29 * result + resultSetType;
         result = 29 * result + resultSetConcurrency;
         return result;
      }
      public String toString()
      {
         StringBuffer tmp = new StringBuffer(super.toString());
         tmp.append('[');
         tmp.append("sql=");
         tmp.append(sql);
         tmp.append(" type=");
         tmp.append(type==PREPARED_STATEMENT ? "PS" : "CS");
         tmp.append(" resultSetType=");
         switch (resultSetType)
         {
            case ResultSet.TYPE_FORWARD_ONLY:
            {
               tmp.append("TYPE_FORWARD_ONLY");
               break;
            }
            case ResultSet.TYPE_SCROLL_INSENSITIVE:
            {
               tmp.append("TYPE_SCROLL_INSENSITIVE");
               break;
            }
            case ResultSet.TYPE_SCROLL_SENSITIVE:
            {
               tmp.append("TYPE_SCROLL_SENSITIVE");
               break;
            }
            default:
               tmp.append(resultSetType);
         }
         tmp.append(" resultSetConcurrency=");
         switch (resultSetConcurrency)
         {
            case ResultSet.CONCUR_READ_ONLY:
            {
               tmp.append("CONCUR_READ_ONLY");
               break;
            }
            case ResultSet.CONCUR_UPDATABLE:
            {
               tmp.append("CONCUR_UPDATABLE");
               break;
            }
            default:
               tmp.append(resultSetConcurrency);
         }
         tmp.append(']');
         return tmp.toString();
      }
   }

   public PreparedStatementCache(int max)
   {
      super(2, max);
      create();
   }

   protected void ageOut(LRUCachePolicy.LRUCacheEntry entry)
   {
      try
      {
         CachedPreparedStatement ws = (CachedPreparedStatement) entry.m_object;
         ws.agedOut();
      }
      catch (SQLException e)
      {
         log.debug("Failed closing cached statement", e);
      }
      finally
      {
         super.ageOut(entry);
      }
   }
}
