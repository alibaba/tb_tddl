/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.exception;

/**
 * @author jiechen.qzm
 * SQL WITCH IS IN FORBIDDEN SET
 */
public class SqlForbidException extends RuntimeException {

	private static final long serialVersionUID = -1L;

	public SqlForbidException() {
		super();
	}

	public SqlForbidException(String msg) {
		super(msg);
	}

	public SqlForbidException(Throwable cause) {
		super(cause);
	}

	public SqlForbidException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
