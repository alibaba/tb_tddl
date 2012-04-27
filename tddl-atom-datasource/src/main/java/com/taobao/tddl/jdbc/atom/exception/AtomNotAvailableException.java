/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.exception;

import java.sql.SQLException;

/**
 * Atom层通过ExceptionSorter检测到数据源不可用时抛出，
 * 或者数据库不可用，同时没有trylock到重试机会时也抛出
 * 便于group层重试
 * 
 * 
 * @author linxuan
 *
 */
public class AtomNotAvailableException extends SQLException {
	private static final long serialVersionUID = 1L;

	public AtomNotAvailableException() {
		super();
	}

	public AtomNotAvailableException(String msg) {
		super(msg);
	}

	public AtomNotAvailableException(String reason, String SQLState) {
		super(reason, SQLState);
	}

	public AtomNotAvailableException(String reason, String SQLState, int vendorCode) {
		super(reason, SQLState, vendorCode);
	}

}
