/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.common;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import junit.framework.Assert;

import org.junit.Test;

import com.taobao.datasource.resource.security.SecureIdentityLoginModule;
import com.taobao.tddl.jdbc.atom.config.object.TAtomDsConfDO;

public class TAtomConfParserUnitTest {

	@Test
	public void parserTAtomDsConfDO_解析全局配置() throws IOException {
		String globaFile = "conf/oracle/globa.properties";
		String globaStr = PropLoadTestUtil.loadPropFile2String(globaFile);
		TAtomDsConfDO tAtomDsConfDO = TAtomConfParser.parserTAtomDsConfDO(globaStr, null);
		Properties prop = PropLoadTestUtil.loadPropFromFile(globaFile);
		Assert.assertEquals(tAtomDsConfDO.getIp(), prop.get(TAtomConfParser.GLOBA_IP_KEY));
		Assert.assertEquals(tAtomDsConfDO.getPort(), prop.get(TAtomConfParser.GLOBA_PORT_KEY));
		Assert.assertEquals(tAtomDsConfDO.getDbName(), prop.get(TAtomConfParser.GLOBA_DB_NAME_KEY));
		Assert.assertEquals(tAtomDsConfDO.getDbType(), prop.get(TAtomConfParser.GLOBA_DB_TYPE_KEY));
		Assert.assertEquals(tAtomDsConfDO.getDbStatus(), prop.get(TAtomConfParser.GLOBA_DB_STATUS_KEY));
	}

	@Test
	public void parserTAtomDsConfDO_解析应用配置() throws IOException {
		String appFile = "conf/oracle/app.properties";
		String appStr = PropLoadTestUtil.loadPropFile2String(appFile);
		TAtomDsConfDO tAtomDsConfDO = TAtomConfParser.parserTAtomDsConfDO(null, appStr);
		Properties prop = PropLoadTestUtil.loadPropFromFile(appFile);
		Assert.assertEquals(tAtomDsConfDO.getUserName(), prop.get(TAtomConfParser.APP_USER_NAME_KEY));
		Assert.assertEquals(tAtomDsConfDO.getOracleConType(), prop.get(TAtomConfParser.APP_ORACLE_CON_TYPE_KEY));
		Assert.assertEquals(String.valueOf(tAtomDsConfDO.getMinPoolSize()), prop
				.get(TAtomConfParser.APP_MIN_POOL_SIZE_KEY));
		Assert.assertEquals(String.valueOf(tAtomDsConfDO.getMaxPoolSize()), prop
				.get(TAtomConfParser.APP_MAX_POOL_SIZE_KEY));
		Assert.assertEquals(String.valueOf(tAtomDsConfDO.getIdleTimeout()), prop
				.get(TAtomConfParser.APP_IDLE_TIMEOUT_KEY));
		Assert.assertEquals(String.valueOf(tAtomDsConfDO.getBlockingTimeout()), prop
				.get(TAtomConfParser.APP_BLOCKING_TIMEOUT_KEY));
		Assert.assertEquals(String.valueOf(tAtomDsConfDO.getPreparedStatementCacheSize()), prop
				.get(TAtomConfParser.APP_PREPARED_STATEMENT_CACHE_SIZE_KEY));
		Map<String, String> connectionProperties = TAtomConfParser.parserConPropStr2Map(prop
				.getProperty(TAtomConfParser.APP_CON_PROP_KEY));
		Assert.assertEquals(tAtomDsConfDO.getConnectionProperties(), connectionProperties);
	}

	@Test
	public void parserPasswd_解析密码() throws IOException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String passwdFile = "conf/oracle/psswd.properties";
		String passwdStr = PropLoadTestUtil.loadPropFile2String(passwdFile);
		String passwd = TAtomConfParser.parserPasswd(passwdStr);
		Properties prop = PropLoadTestUtil.loadPropFromFile(passwdFile);
		String encPasswd = prop.getProperty(TAtomConfParser.PASSWD_ENC_PASSWD_KEY);
		String encPasswdKey = prop.getProperty(TAtomConfParser.PASSWD_ENC_KEY_KEY);
		String tmpEncPsswd = SecureIdentityLoginModule.encode(encPasswdKey, passwd);
		Assert.assertEquals(encPasswd, tmpEncPsswd);
	}
}
