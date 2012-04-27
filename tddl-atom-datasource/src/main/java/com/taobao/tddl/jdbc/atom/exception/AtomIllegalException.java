/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.exception;

/**
 * @author qihao
 *
 */
public class AtomIllegalException extends Exception {

	private static final long serialVersionUID = -5341803227125385166L;

	public AtomIllegalException() {
		super();
	}

	public AtomIllegalException(String msg) {
		super(msg);
	}

	public AtomIllegalException(Throwable cause) {
		super(cause);
	}

	public AtomIllegalException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
