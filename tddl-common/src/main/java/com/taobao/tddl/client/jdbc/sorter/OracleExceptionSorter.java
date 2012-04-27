/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	/*
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
package com.taobao.tddl.client.jdbc.sorter;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * OracleExceptionSorter.java
 *
 *
 * Created: Fri Mar 14 21:54:23 2003
 *
 * @author <a href="mailto:an_test@mail.ru">Andrey Demchenko</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:weston.price@jboss.com>Weston Price</a>
 * @version 1.0
 */
public class OracleExceptionSorter implements ExceptionSorter, Serializable {
	private static final long serialVersionUID = 573723525408205079L;

	public OracleExceptionSorter() {
	}

	public boolean isExceptionFatal(SQLException e) {
		int loopCount = 20; //防止人为失误，当两个Throwable互为对方的initCause()时，造成死循环

		Throwable cause = e;
		while (cause != null) {
			if (cause instanceof SQLException) {
				SQLException sqlException = (SQLException) cause;

				if (isExceptionFatal0(sqlException))
					return true;
			}

			cause = cause.getCause();

			if (--loopCount < 0)
				break;
		}

		return false;
	}

	public boolean isExceptionFatal0(final SQLException e) {
		final int error_code = Math.abs(e.getErrorCode()); // I can't remember if the errors are negative or positive.

		if ((error_code == 28) //session has been killed
				|| (error_code == 600) //Internal oracle error
				|| (error_code == 1012) //not logged on
				|| (error_code == 1014) //Oracle shutdown in progress
				|| (error_code == 1033) //Oracle initialization or shutdown in progress
				|| (error_code == 1034) //Oracle not available
				|| (error_code == 1035) //ORACLE only available to users with RESTRICTED SESSION privilege
				|| (error_code == 1089) //immediate shutdown in progress - no operations are permitted
				|| (error_code == 1090) //shutdown in progress - connection is not permitted
				|| (error_code == 1092) //ORACLE instance terminated. Disconnection forced
				|| (error_code == 1094) //ALTER DATABASE CLOSE in progress. Connections not permitted
				|| (error_code == 2396) //exceeded maximum idle time, please connect again
				|| (error_code == 3106) //fatal two-task communication protocol error
				|| (error_code == 3111) //break received on communication channel
				|| (error_code == 3113) //end-of-file on communication channel
				|| (error_code == 3114) //not connected to ORACLE
				|| (error_code >= 12100 && error_code <= 12299) // TNS issues
				|| (error_code == 17002) //connection reset
				|| (error_code == 17008)) //connection closed
		{
			return true;
		}

		if (e.getMessage() == null) {
			return false;
		}
		final String error_text = (e.getMessage()).toUpperCase();

		// Exclude oracle user defined error codes (20000 through 20999) from consideration when looking for
		// certain strings.

		if ((error_code < 20000 || error_code >= 21000)
				&& ("NO DATASOURCE!".equals(error_text)
						|| "NO ALIVE DATASOURCE".equals(error_text) //兼容rjdbc抛出的错误
						|| (error_text.indexOf("SOCKET") > -1) //for control socket error
						|| (error_text.indexOf("CONNECTION HAS ALREADY BEEN CLOSED") > -1)
						|| (error_text.indexOf("BROKEN PIPE") > -1) || (error_text.indexOf("TNS") > -1 && error_text
						.indexOf("ORA-") > -1))) {
			return true;
		}

		if (externalExceptionSorters != null) {
			for (ExceptionSorter externalSorter : externalExceptionSorters) {
				if (externalSorter.isExceptionFatal(e)) {
					return true;
				}
			}
		}
		return false;
	}

	private static List<ExceptionSorter> externalExceptionSorters;

	/**
	 * @param sorter 外部的ExceptionSorter只需判断SQLException本身即可，不需要判断其cause链
	 */
	public static void addExceptionSorter(ExceptionSorter sorter) {
		if (externalExceptionSorters == null) {
			externalExceptionSorters = new LinkedList<ExceptionSorter>();
		}
		externalExceptionSorters.add(sorter);
	}
}
