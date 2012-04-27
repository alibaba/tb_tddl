/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.exception.sqlexceptionwrapper;

import java.sql.SQLException;

public class TDDLSQLExceptionWrapper extends SQLException {

	public TDDLSQLExceptionWrapper(String message,
			SQLException targetSQLESqlException) {
		if (targetSQLESqlException == null) {
			throw new IllegalArgumentException("±ÿ–ÎÃÓ»ÎSQLException");
		}
		this.targetSQLException = targetSQLESqlException;
		this.message = message;
	}

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4558269080286141706L;
	final SQLException targetSQLException;
	final String message;

	public String getSQLState() {
		return targetSQLException.getSQLState();
	}

	public int getErrorCode() {
		return targetSQLException.getErrorCode();
	}

	public SQLException getNextException() {
		return targetSQLException.getNextException();
	}

	public void setNextException(SQLException ex) {
		targetSQLException.setNextException(ex);
	}
	

	public Throwable getCause() {
		return targetSQLException;
	}

	public String getMessage() {
		return message;
	}
}
