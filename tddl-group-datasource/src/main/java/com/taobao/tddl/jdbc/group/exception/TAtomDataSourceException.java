/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.exception;

//jdk1.5 java.sql.SQLException没有带Throwable cause的构造函数
//public class TAtomDataSourceException extends java.sql.SQLException {

/**
 * @author yangzhu
 * 
 */
public class TAtomDataSourceException extends RuntimeException {

	private static final long serialVersionUID = -1L;

	public TAtomDataSourceException() {
		super();
	}

	public TAtomDataSourceException(String msg) {
		super(msg);
	}

	public TAtomDataSourceException(Throwable cause) {
		super(cause);
	}

	public TAtomDataSourceException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
