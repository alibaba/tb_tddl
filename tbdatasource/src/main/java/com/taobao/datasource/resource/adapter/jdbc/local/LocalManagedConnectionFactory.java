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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.security.auth.Subject;

import org.jboss.util.NestedRuntimeException;

import com.taobao.datasource.resource.JBossResourceException;
import com.taobao.datasource.resource.adapter.jdbc.BaseWrapperManagedConnectionFactory;

/**
 * LocalManagedConnectionFactory
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 *
 * @version $Revision: 57189 $
 */
public class LocalManagedConnectionFactory extends BaseWrapperManagedConnectionFactory
{
   static final long serialVersionUID = 4698955390505160469L;

   private String driverClass;

   private transient Driver driver;

   private String connectionURL;

   protected String connectionProperties;

   public LocalManagedConnectionFactory()
   {

   }

   /**
    * Get the value of ConnectionURL.
    *
    * @return value of ConnectionURL.
    */
   public String getConnectionURL()
   {
      return connectionURL;
   }

   /**
    * Set the value of ConnectionURL.
    *
    * @param connectionURL  Value to assign to ConnectionURL.
    */
   public void setConnectionURL(final String connectionURL)
   {
      this.connectionURL = connectionURL;
   }

   /**
    * Get the DriverClass value.
    *
    * @return the DriverClass value.
    */
   public String getDriverClass()
   {
      return driverClass;
   }

   /**
    * Set the DriverClass value.
    *
    * @param driverClass The new DriverClass value.
    */
   public synchronized void setDriverClass(final String driverClass)
   {
      this.driverClass = driverClass;
      driver = null;
   }

   /**
    * Get the value of connectionProperties.
    *
    * @return value of connectionProperties.
    */
   public String getConnectionProperties()
   {
      return connectionProperties;
   }

   /**
    * Set the value of connectionProperties.
    *
    * @param connectionProperties  Value to assign to connectionProperties.
    */
   public void setConnectionProperties(String connectionProperties)
   {
      this.connectionProperties = connectionProperties;
      connectionProps.clear();
      if (connectionProperties != null)
      {
         // Map any \ to \\
         connectionProperties = connectionProperties.replaceAll("\\\\", "\\\\\\\\");

         InputStream is = new ByteArrayInputStream(connectionProperties.getBytes());
         try
         {
            connectionProps.load(is);
         }
         catch (IOException ioe)
         {
            throw new NestedRuntimeException("Could not load connection properties", ioe);
         }
      }
   }

   public void setConnectionProperties(Map<String, String> connectionProperties) {
       connectionProps.clear();
       connectionProps.putAll(connectionProperties);

       StringBuilder sb = new StringBuilder();
       for (Map.Entry<String, String> property : connectionProperties.entrySet()) {
           if (sb.length() > 0) {
               sb.append('\n');
           }
           sb.append(property.getKey()).append('=').append(property.getValue());
       }
   }

   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cri)
         throws javax.resource.ResourceException
   {
      Properties props = getConnectionProperties(subject, cri);
      // Some friendly drivers (Oracle, you guessed right) modify the props you supply.
      // Since we use our copy to identify compatibility in matchManagedConnection, we need
      // a pristine copy for our own use.  So give the friendly driver a copy.
      Properties copy = (Properties) props.clone();
      boolean trace = log.isTraceEnabled();
      if (trace)
      {
         // Make yet another copy to mask the password
         Properties logCopy = copy;
         if (copy.getProperty("password") != null)
         {
            logCopy = (Properties) props.clone();
            logCopy.setProperty("password", "--hidden--");
         }
         log.trace("Using properties: " + logCopy);
      }

      try
      {
         String url = getConnectionURL();
         log.warn("Trying to create a connection to " + url);
         Driver d = getDriver(url);
         Connection con = d.connect(url, copy);
         if (con == null)
            throw new JBossResourceException("Wrong driver class for this connection URL");

         return new LocalManagedConnection(this, con, props, transactionIsolation, preparedStatementCacheSize);
      }
      catch (Exception e)
      {
         throw new JBossResourceException("Could not create connection", e);
      }
   }

   public ManagedConnection matchManagedConnections(final Set mcs, final Subject subject, final ConnectionRequestInfo cri)
         throws ResourceException
   {
      Properties newProps = getConnectionProperties(subject, cri);

      for (Iterator i = mcs.iterator(); i.hasNext();)
      {
         Object o = i.next();

         if (o instanceof LocalManagedConnection)
         {
            LocalManagedConnection mc = (LocalManagedConnection) o;

            //First check the properties
            if (mc.getProps().equals(newProps))
            {
               //Next check to see if we are validating on matchManagedConnections
               if((getValidateOnMatch() && mc.checkValid()) || !getValidateOnMatch())
               {

                  return mc;

               }

            }
         }
      }

      return null;
   }

   public int hashCode()
   {
      int result = 17;
      result = result * 37 + ((connectionURL == null) ? 0 : connectionURL.hashCode());
      result = result * 37 + ((driverClass == null) ? 0 : driverClass.hashCode());
      result = result * 37 + ((userName == null) ? 0 : userName.hashCode());
      result = result * 37 + ((password == null) ? 0 : password.hashCode());
      result = result * 37 + transactionIsolation;
      return result;
   }

   public boolean equals(Object other)
   {
      if (this == other)
         return true;
      if (getClass() != other.getClass())
         return false;
      LocalManagedConnectionFactory otherMcf = (LocalManagedConnectionFactory) other;
      return this.connectionURL.equals(otherMcf.connectionURL) && this.driverClass.equals(otherMcf.driverClass)
            && ((this.userName == null) ? otherMcf.userName == null : this.userName.equals(otherMcf.userName))
            && ((this.password == null) ? otherMcf.password == null : this.password.equals(otherMcf.password))
            && this.transactionIsolation == otherMcf.transactionIsolation;

   }

   /**
    * Check the driver for the given URL.  If it is not registered already
    * then register it.
    *
    * @param url   The JDBC URL which we need a driver for.
    */
   protected synchronized Driver getDriver(final String url) throws ResourceException
   {
      boolean trace = log.isTraceEnabled();

      // don't bother if it is loaded already
      if (driver != null)
      {
         return driver;
      }
      if (trace)
         log.trace("Checking driver for URL: " + url);

      if (driverClass == null)
      {
         throw new JBossResourceException("No Driver class specified!");
      }

      // Check if the driver is already loaded, if not then try to load it

      if (isDriverLoadedForURL(url))
      {
         return driver;
      } // end of if ()

      try
      {
         //try to load the class... this should register with DriverManager.
         Class clazz = Class.forName(driverClass, true, Thread.currentThread().getContextClassLoader());
         if (isDriverLoadedForURL(url))
            //return immediately, some drivers (Cloudscape) do not let you create an instance.
            return driver;

         //We loaded the class, but either it didn't register
         //and is not spec compliant, or is the wrong class.
         driver = (Driver) clazz.newInstance();
         DriverManager.registerDriver(driver);
         if (isDriverLoadedForURL(url))
            return driver;
         //We can even instantiate one, it must be the wrong class for the URL.
      }
      catch (Exception e)
      {
         throw new JBossResourceException("Failed to register driver for: " + driverClass, e);
      }

      throw new JBossResourceException("Apparently wrong driver class specified for URL: class: " + driverClass
            + ", url: " + url);
   }

   private boolean isDriverLoadedForURL(String url)
   {
      boolean trace = log.isTraceEnabled();

      try
      {
         driver = DriverManager.getDriver(url);
         if (trace)
            log.trace("Driver already registered for url: " + url);
         return true;
      }
      catch (Exception e)
      {
         if (trace)
            log.trace("Driver not yet registered for url: " + url);
         return false;
      }
   }

   protected String internalGetConnectionURL()
   {
      return connectionURL;
   }
}
