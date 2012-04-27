/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.sorter;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import com.taobao.tddl.client.jdbc.sorter.OracleExceptionSorter;

/**
 * 
 * @author yangzhu
 *
 */
public class OracleExceptionSorterUnitTest {

	@Test
	public void all() {
		OracleExceptionSorter s = new OracleExceptionSorter();
		int[] errors = new int[] { 28, 600, 1012 };

		SQLException e = new SQLException("reason", "08S01");

		//Assert.assertTrue(s.isExceptionFatal(e));

		for (int err : errors) {
			e = new SQLException("reason", "01XXX", err);
			Assert.assertTrue(s.isExceptionFatal(e));
		}

		e = new SQLException("NO DATASOURCE!", "01XXX", 21001);
		Assert.assertTrue(s.isExceptionFatal(e));
		e = new SQLException("NO ALIVE DATASOURCE", "01XXX", 21001);
		Assert.assertTrue(s.isExceptionFatal(e));

		e = new SQLException("ORA-XXX-TNS-XXX", "01XXX", 21001);
		Assert.assertTrue(s.isExceptionFatal(e));

		e = new SQLException("msg", "01XXX", -1);
		Assert.assertFalse(s.isExceptionFatal(e));

		e = new SQLException("msg", "01XXX", -1);

		e.initCause(new SQLException("msg", "01XXX", -1));
		Assert.assertFalse(s.isExceptionFatal(e));

		e = new SQLException("msg", "01XXX", -1);
		e.initCause(null);
		Assert.assertFalse(s.isExceptionFatal(e));
	}
}
