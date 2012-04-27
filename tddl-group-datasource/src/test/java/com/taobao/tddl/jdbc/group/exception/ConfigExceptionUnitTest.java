/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.exception;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author yangzhu
 *
 */
public class ConfigExceptionUnitTest {

	@Test
	public void all() {
		Throwable cause = new Throwable();
		String msg = "msg";
		ConfigException e = new ConfigException();
		e = new ConfigException(msg);
		Assert.assertEquals(msg, e.getMessage());

		e = new ConfigException(cause);

		Assert.assertEquals(cause, e.getCause());

		e = new ConfigException(msg, cause);
		
		Assert.assertEquals(msg, e.getMessage());

		Assert.assertEquals(cause, e.getCause());
	}
}
