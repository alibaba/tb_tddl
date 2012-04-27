/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.exception;


public class DBRouterException extends Exception{
	private static final long serialVersionUID = -7468723962922760016L;

	/**
	 * @param msg
	 * @param cause
	 */
	public DBRouterException(String msg, Throwable cause) {
		super(msg, cause);
	}

	/**
	 * @param cause
	 */
	public DBRouterException(Throwable cause) {
		super(cause);
	}

	public DBRouterException() {
		super();
	}

	/**
	 * @param string
	 */
	public DBRouterException(String message) {
		super(message);
	}

}
