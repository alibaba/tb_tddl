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
import java.sql.SQLException;

import com.taobao.datasource.resource.adapter.jdbc.ExceptionSorter;

/**
 * Implementation of ExceptionSorter for Oracle. 
 * 
 * @author <a href="mailto:mark.mcgregor@par3.com">Mark McGregor</a>
 * @author <a href="mailto:weston.price@jboss.com">Weston Price</a>
 *
 */
public class OracleExceptionSorter implements ExceptionSorter, Serializable {
	static final long serialVersionUID = 573723525408205079L;

	public OracleExceptionSorter() {
	} // OracleExceptionSorter constructor

	public boolean isExceptionFatal(final SQLException e) {
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
				|| (error_code >= 12100 && error_code <= 12299)
				|| (error_code == 17002) //connection reset
				|| (error_code == 17008) //connection closed
		) // TNS issues
		{
			return true;
		}

		final String error_text = (e.getMessage()).toUpperCase();

		// Exclude oracle user defined error codes (20000 through 20999) from consideration when looking for
		// certain strings.

		if ((error_code < 20000 || error_code >= 21000)
				&& ((error_text.indexOf("SOCKET") > -1) //for control socket error
						|| (error_text.indexOf("CONNECTION HAS ALREADY BEEN CLOSED") > -1) || (error_text
						.indexOf("BROKEN PIPE") > -1))) {
			return true;
		}

		return false;
	}
}