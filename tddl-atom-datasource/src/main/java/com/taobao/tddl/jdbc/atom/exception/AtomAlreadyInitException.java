/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.exception;

public class AtomAlreadyInitException extends Exception {

	private static final long serialVersionUID = -3907211238952987907L;

	public AtomAlreadyInitException() {
		super();
	}

	public AtomAlreadyInitException(String msg) {
		super(msg);
	}

	public AtomAlreadyInitException(Throwable cause) {
		super(cause);
	}

	public AtomAlreadyInitException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
