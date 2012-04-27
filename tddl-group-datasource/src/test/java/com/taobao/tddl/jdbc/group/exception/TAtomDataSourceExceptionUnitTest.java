/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author yangzhu
 *
 */
public class TAtomDataSourceExceptionUnitTest {

	@Test
	public void all() {
		Throwable cause = new Throwable();
		String msg = "msg";
		TAtomDataSourceException e = new TAtomDataSourceException();
		e = new TAtomDataSourceException(msg);
		Assert.assertEquals(msg, e.getMessage());

		e = new TAtomDataSourceException(cause);

		Assert.assertEquals(cause, e.getCause());

		e = new TAtomDataSourceException(msg, cause);
		
		Assert.assertEquals(msg, e.getMessage());

		Assert.assertEquals(cause, e.getCause());
	}
}
