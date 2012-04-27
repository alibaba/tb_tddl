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
package com.taobao.datasource.resource.adapter.jdbc.local;

import org.jboss.util.JBossStringBuilder;

import com.taobao.datasource.resource.JBossResourceException;

import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.ResourceException;
import javax.security.auth.Subject;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Collections;
import java.sql.Driver;
import java.sql.Connection;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 59773 $</tt>
 */
public class HALocalManagedConnectionFactory
   extends LocalManagedConnectionFactory
{
   private static final long serialVersionUID = -6506610639011749394L;

   private URLSelector urlSelector;
   private String urlDelimiter;

   public String getURLDelimiter()
   {
      return urlDelimiter;
   }

   public void setURLDelimiter(String urlDelimiter)
   {
      this.urlDelimiter = urlDelimiter;
      if(getConnectionURL() != null)
      {
         initUrlSelector();
      }
   }

   public void setConnectionURL(String connectionURL)
   {
      super.setConnectionURL(connectionURL);
      if(urlDelimiter != null)
      {
         initUrlSelector();
      }
   }

   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cri)
      throws ResourceException
   {
      boolean trace = log.isTraceEnabled();
      Properties props = getConnectionProperties(subject, cri);
      // Some friendly drivers (Oracle, you guessed right) modify the props you supply.
      // Since we use our copy to identify compatibility in matchManagedConnection, we need
      // a pristine copy for our own use.  So give the friendly driver a copy.
      Properties copy = (Properties)props.clone();
      if(trace)
      {
         // Make yet another copy to mask the password
         Properties logCopy = copy;
         if(copy.getProperty("password") != null)
         {
            logCopy = (Properties)props.clone();
            logCopy.setProperty("password", "--hidden--");
         }
         log.trace("Using properties: " + logCopy);
      }

      return doCreateManagedConnection(copy, props);
   }

   private ManagedConnection doCreateManagedConnection(Properties copy, Properties props)
      throws JBossResourceException
   {
      boolean trace = log.isTraceEnabled();
      
      if (urlSelector == null)
      {
         JBossStringBuilder buffer = new JBossStringBuilder();
         buffer.append("Missing configuration for HA local datasource. ");
         if (getConnectionURL() == null)
            buffer.append("No connection-url. ");
         if (urlDelimiter == null)
            buffer.append("No url-delimiter. ");
         throw new JBossResourceException(buffer.toString());
      }

      // try to get a connection as many times as many urls we have in the list
      for(int i = 0; i < urlSelector.getUrlList().size(); ++i)
      {
         String url = urlSelector.getUrl();
         log.warn("Trying to create a connection to " + url);
         try
         {
            Driver d = getDriver(url);
            Connection con = d.connect(url, copy);
            if(con == null)
            {
               log.warn("Wrong driver class for this connection URL: " + url);
               urlSelector.failedUrl(url);
            }
            else
            {
               return new LocalManagedConnection(this, con, props, transactionIsolation, preparedStatementCacheSize);
            }
         }
         catch(Exception e)
         {
            log.warn("Failed to create connection for " + url + ": " + e.getMessage());
            urlSelector.failedUrl(url);
         }
      }

      // we have supposedly tried all the urls
      throw new JBossResourceException(
         "Could not create connection using any of the URLs: " + urlSelector.getUrlList()
      );
   }

   private void initUrlSelector()
   {
      boolean trace = log.isTraceEnabled();
      
      List urlsList = new ArrayList();
      String urlsStr = getConnectionURL();
      String url;
      int urlStart = 0;
      int urlEnd = urlsStr.indexOf(urlDelimiter);
      while(urlEnd > 0)
      {
         url = urlsStr.substring(urlStart, urlEnd);
         urlsList.add(url);
         urlStart = ++urlEnd;
         urlEnd = urlsStr.indexOf(urlDelimiter, urlEnd);
         if (trace)
          log.trace("added HA connection url: " + url);
      }

      if(urlStart != urlsStr.length())
      {
         url = urlsStr.substring(urlStart, urlsStr.length());
         urlsList.add(url);
         if (trace)
            log.trace("added HA connection url: " + url);
      }

      this.urlSelector = new URLSelector(urlsList);
   }

   // Inner

   public static class URLSelector
   {
      private final List urls;
      private int urlIndex;
      private String url;

      public URLSelector(List urls)
      {
         if(urls == null || urls.size() == 0)
         {
            throw new IllegalStateException("Expected non-empty list of connection URLs but got: " + urls);
         }
         this.urls = Collections.unmodifiableList(urls);
      }

      public synchronized String getUrl()
      {
         if(url == null)
         {
            if(urlIndex == urls.size())
            {
               urlIndex = 0;
            }
            url = (String)urls.get(urlIndex++);
         }
         return url;
      }

      public synchronized void failedUrl(String url)
      {
         if(url.equals(this.url))
         {
            this.url = null;
         }
      }

      public List getUrlList()
      {
         return urls;
      }
   }
}
