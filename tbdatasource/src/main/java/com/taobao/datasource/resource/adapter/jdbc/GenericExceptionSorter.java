package com.taobao.datasource.resource.adapter.jdbc;

import java.sql.SQLException;

/**
 * A GenericExceptionSorter returning true for all exceptions.
 * 
 * @author <a href="mailto:weston.price@jboss.org>Weston Price</a>
 * @version $Revision: 1.1 $
 */
public class GenericExceptionSorter implements ExceptionSorter
{

   public boolean isExceptionFatal(final SQLException e)
   {
      return true;
   }

}
