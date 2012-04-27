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

import java.util.LinkedList;

/**
 * PoolFiller
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 60788 $
 */
public class PoolFiller implements Runnable
{
   private final LinkedList pools = new LinkedList();

   private final Thread fillerThread;

   private static final PoolFiller filler = new PoolFiller();

   public static void fillPool(InternalManagedConnectionPool mcp)
   {
      filler.internalFillPool(mcp);
   }

   public PoolFiller ()
   {
      fillerThread = new Thread(this, "JCA PoolFiller");
      fillerThread.start();
   }

   public void run()
   {
      ClassLoader myClassLoader = getClass().getClassLoader();
      Thread.currentThread().setContextClassLoader(myClassLoader);
      //keep going unless interrupted
      while (true)
      {
         try 
         {
            InternalManagedConnectionPool mcp = null;
            //keep iterating through pools till empty, exception escapes.
            while (true)
            {
                     
               synchronized (pools)
               {
                  mcp = (InternalManagedConnectionPool)pools.removeFirst();
               }
               if (mcp == null) 
                  break;
                        
               mcp.fillToMin();
            }
         }
         catch (Exception e)
         {
         }
                        
         try 
         {
            synchronized (pools)
            {
               while(pools.isEmpty())
               {
                  pools.wait();                        
                  
               }
            }
         }
         catch (InterruptedException ie)
         {
            return;
         }
      }
   }

   private void internalFillPool(InternalManagedConnectionPool mcp)
   {
      synchronized (pools)
      {
         pools.addLast(mcp);
         pools.notify();
      }
   }
}
