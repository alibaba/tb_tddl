/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.exception;

/**
 * @author qihao
 *
 */
public class AtomInitialException extends Exception {

	private static final long serialVersionUID = -2933446568649742125L;

	public AtomInitialException() {
		super();
	}

	public AtomInitialException(String msg) {
		super(msg);
	}

	public AtomInitialException(Throwable cause) {
		super(cause);
	}

	public AtomInitialException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
