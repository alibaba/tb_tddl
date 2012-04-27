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

import java.util.Collection;
import java.util.Set;

import javax.resource.ResourceException;
import javax.transaction.SystemException;

/**
 * ConnectionCacheListener
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:E.Guib@ceyoniq.com">Erwin Guib</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57189 $
 */
public interface ConnectionCacheListener 
{
   /**
    * Notification of transaction started
    * 
    * @param conns the connections
    * @throws SystemException for any error
    */
   void transactionStarted(Collection conns) throws SystemException;

   /**
    * Notification to reconnect connections
    * 
    * @param conns the connections
    * @param unsharableResources unshareable resources
    * @throws ResourceException for any error
    */
   void reconnect(Collection conns, Set unsharableResources) throws ResourceException;
   
   /**
    * Notification to disconnect connections
    * 
    * @param conns the connections
    * @param unsharableResources the unshareable resources
    * @throws ResourceException for any error
    */
   void disconnect(Collection conns, Set unsharableResources) throws ResourceException;
}
