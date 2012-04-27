/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.testutil;

import java.util.HashMap;
import java.util.Map;


import com.taobao.diamond.mockserver.MockServer;
import com.taobao.tddl.jdbc.atom.common.TAtomConfParser;
import com.taobao.tddl.jdbc.atom.common.TAtomConstants;
import com.taobao.tddl.jdbc.atom.config.object.TAtomDsConfDO;

/**
 * 
 * @author yangzhu
 *
 */
public class TAtomDataSourceConfigBase {
	public static final String appName = "unitTest";
	public static final String dbGroupKey = "myDbGroupKey";
	public static final String dbKey1 = "db1";
	public static final String dbKey2 = "db2";
	public static final String dbKey3 = "db3";

	public static void initConfig() throws Exception {
		//初始化持久配置中心数据
		Map<String, String> dataMap = new HashMap<String, String>();
		//全局配置
		String globaStr = PropLoadTestUtil.loadPropFile2String("conf/group_test1/globa.properties");
		dataMap.put(TAtomConstants.getGlobalDataId(dbKey1), globaStr);

		//应用配置
		String appStr = PropLoadTestUtil.loadPropFile2String("conf/group_test1/app.properties");
		dataMap.put(TAtomConstants.getAppDataId(appName, dbKey1), appStr);

		//解析配置
		TAtomDsConfDO tAtomDsConfDO = TAtomConfParser.parserTAtomDsConfDO(globaStr, appStr);
		//密码配置
		String passwdStr = PropLoadTestUtil.loadPropFile2String("conf/group_test1/psswd.properties");
		dataMap.put(TAtomConstants.getPasswdDataId(tAtomDsConfDO.getDbName(), tAtomDsConfDO.getDbType(), tAtomDsConfDO
				.getUserName()), passwdStr);

		globaStr = PropLoadTestUtil.loadPropFile2String("conf/group_test2/globa.properties");
		dataMap.put(TAtomConstants.getGlobalDataId(dbKey2), globaStr);

		appStr = PropLoadTestUtil.loadPropFile2String("conf/group_test2/app.properties");
		dataMap.put(TAtomConstants.getAppDataId(appName, dbKey2), appStr);

		tAtomDsConfDO = TAtomConfParser.parserTAtomDsConfDO(globaStr, appStr);
		passwdStr = PropLoadTestUtil.loadPropFile2String("conf/group_test2/psswd.properties");
		dataMap.put(TAtomConstants.getPasswdDataId(tAtomDsConfDO.getDbName(), tAtomDsConfDO.getDbType(), tAtomDsConfDO
				.getUserName()), passwdStr);

		globaStr = PropLoadTestUtil.loadPropFile2String("conf/group_test3/globa.properties");
		dataMap.put(TAtomConstants.getGlobalDataId(dbKey3), globaStr);

		appStr = PropLoadTestUtil.loadPropFile2String("conf/group_test3/app.properties");
		dataMap.put(TAtomConstants.getAppDataId(appName, dbKey3), appStr);

		tAtomDsConfDO = TAtomConfParser.parserTAtomDsConfDO(globaStr, appStr);
		passwdStr = PropLoadTestUtil.loadPropFile2String("conf/group_test3/psswd.properties");
		//System.out.println("passwdStr = " + passwdStr);
		dataMap.put(TAtomConstants.getPasswdDataId(tAtomDsConfDO.getDbName(), tAtomDsConfDO.getDbType(), tAtomDsConfDO
				.getUserName()), passwdStr);

		MockServer.setConfigInfos(dataMap);

		//System.out.println("dataMap = " + dataMap);
	}
}
