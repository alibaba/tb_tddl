/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.sample.base;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.jdbc.core.JdbcTemplate;

import com.taobao.tddl.jdbc.atom.TAtomDataSource;

public class AtomSampleCase extends BaseSampleCase {
	protected static JdbcTemplate tddlJT;
	protected static TAtomDataSource tds;

	@BeforeClass
	public static void setUp() throws Exception {
		tds = new TAtomDataSource();
		tds.setAppName(APPNAME);
		tds.setDbKey(DBKEY_0);
		tds.init();
		tddlJT = getJT();
	}

	@Before
	public void init() throws Exception {
		clearData(tddlJT, "delete from normaltbl_0001 where pk=?", new Object[] { RANDOM_ID });
	}

	@After
	public void destroy() throws Exception {
		clearData(tddlJT, "delete from normaltbl_0001 where pk=?", new Object[] { RANDOM_ID });
	}

	protected static JdbcTemplate getJT() {
		return new JdbcTemplate(tds);
	}

	protected static JdbcTemplate getJT(String path, String appName, String dbKey) throws Exception {
		TAtomDataSource atomDs = new TAtomDataSource();
		atomDs.setAppName(appName);
		atomDs.setDbKey(dbKey);
		atomDs.init();
		return new JdbcTemplate(atomDs);
	}

}
