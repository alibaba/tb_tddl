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
 * MySQLExceptionSorter
 * 
 * This is a basic exception sorter for the MySQL RDBMS. All error
 * codes are taken from the MySQL Connector Java 3.0.16 SQLError class. 
 *
 * @author <a href="mailto:u.schroeter@mobilcom.de">Ulf Schroeter</a>
 */
public class MySQLExceptionSorter implements ExceptionSorter, Serializable {
	private static final long serialVersionUID = 2375890129763721017L;

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

	private boolean isExceptionFatal0(SQLException e) {
		String sqlState = e.getSQLState();
		if (sqlState != null && sqlState.startsWith("08")) {
			return true;
		}
		switch (e.getErrorCode()) {
		// Communications Errors
		case 1040: // ER_CON_COUNT_ERROR
		case 1042: // ER_BAD_HOST_ERROR
		case 1043: // ER_HANDSHAKE_ERROR
		case 1047: // ER_UNKNOWN_COM_ERROR
		case 1081: // ER_IPSOCK_ERROR
		case 1129: // ER_HOST_IS_BLOCKED
		case 1130: // ER_HOST_NOT_PRIVILEGED

			// Authentication Errors
		case 1045: // ER_ACCESS_DENIED_ERROR

			// Resource errors
		case 1004: // ER_CANT_CREATE_FILE
		case 1005: // ER_CANT_CREATE_TABLE
		case 1015: // ER_CANT_LOCK
		case 1021: // ER_DISK_FULL
		case 1041: // ER_OUT_OF_RESOURCES

			// Out-of-memory errors
		case 1037: // ER_OUTOFMEMORY
		case 1038: // ER_OUT_OF_SORTMEMORY

			return true;
		}

		final String error_text = e.getMessage();

		if ("no datasource!".equals(error_text) || "no alive datasource".equals(error_text)) {
			//兼容rjdbc抛出的错误
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
