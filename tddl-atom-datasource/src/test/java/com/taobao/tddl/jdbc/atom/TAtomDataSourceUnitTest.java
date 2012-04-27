/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.taobao.diamond.client.DiamondConfigure;
import com.taobao.diamond.client.impl.DiamondClientFactory;
import com.taobao.diamond.mockserver.MockServer;
import com.taobao.tddl.jdbc.atom.common.PropLoadTestUtil;
import com.taobao.tddl.jdbc.atom.common.TAtomConfParser;
import com.taobao.tddl.jdbc.atom.common.TAtomConstants;
import com.taobao.tddl.jdbc.atom.config.object.AtomDbStatusEnum;
import com.taobao.tddl.jdbc.atom.config.object.TAtomDsConfDO;
import com.taobao.tddl.jdbc.atom.exception.AtomAlreadyInitException;
import com.taobao.tddl.jdbc.atom.listener.TAtomDbStatusListener;

public class TAtomDataSourceUnitTest {

	private volatile static DiamondConfigure configure = DiamondClientFactory.getSingletonDiamondSubscriber().getDiamondConfigure();
	static String TEST_SQL = "select 1 from dual";

	@Before
	public void beforeClass() {
		MockServer.setUpMockServer();
		configure.setPollingIntervalTime(1);
	}

	@After
	public void after() {
		MockServer.tearDownMockServer();
	}

	@Test
	public void testInitTAtomDataSource_初始化() throws Exception {
		String appName = "unitTest";
		String dbKey = "unitTestDb";
		//Oracle测试
		TAtomDataSource tAtomDataSource = createTAtomDataSource(appName, dbKey, "oracle");
		JdbcTemplate jtp = new JdbcTemplate(tAtomDataSource);
		int actual = jtp.queryForInt(TAtomDataSourceUnitTest.TEST_SQL);
		Assert.assertEquals(actual, 1);
		tAtomDataSource.destroyDataSource();
		//mysql测试
		tAtomDataSource = createTAtomDataSource(appName, dbKey, "mysql");
		jtp.setDataSource(tAtomDataSource);
		actual = jtp.queryForInt(TAtomDataSourceUnitTest.TEST_SQL);
		Assert.assertEquals(actual, 1);
		tAtomDataSource.destroyDataSource();
	}

	@Test
	public void testOracleChange_切换Ip() throws IOException, AtomAlreadyInitException, Exception {
		testChange(new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.GLOBA_IP_KEY, "127.0.0.1");
				return prop;
			}
		}, new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.GLOBA_IP_KEY, prop.getProperty(TAtomConfParser.GLOBA_IP_KEY));
				return prop;
			}
		}, "globa", "oracle", "testOracleChange_切换Ip");
	}

	@Test
	public void testMysqlChange_切换Ip() throws IOException, AtomAlreadyInitException, Exception {
		testChange(new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.GLOBA_IP_KEY, "127.0.0.1");
				return prop;
			}
		}, new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.GLOBA_IP_KEY, prop.getProperty(TAtomConfParser.GLOBA_IP_KEY));
				return prop;
			}
		}, "globa", "mysql", "testMysqlChange_切换Ip");
	}

	@Test
	public void testOracleChange_切换Port() throws IOException, AtomAlreadyInitException, Exception {
		testChange(new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.GLOBA_PORT_KEY, "1234");
				return prop;
			}
		}, new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.GLOBA_PORT_KEY, prop.getProperty(TAtomConfParser.GLOBA_PORT_KEY));
				return prop;
			}
		}, "globa", "oracle", "testOracleChange_切换Port");
	}

	@Test
	public void testMysqlChange_切换Port() throws IOException, AtomAlreadyInitException, Exception {
		testChange(new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.GLOBA_PORT_KEY, "1234");
				return prop;
			}
		}, new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.GLOBA_PORT_KEY, prop.getProperty(TAtomConfParser.GLOBA_PORT_KEY));
				return prop;
			}
		}, "globa", "mysql", "testMysqlChange_切换Port");
	}

	@Test
	public void testOracleChange_切换DbName() throws IOException, AtomAlreadyInitException, Exception {
		testChange(new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.GLOBA_DB_NAME_KEY, "test");
				return prop;
			}
		}, new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				String dbName = prop.getProperty(TAtomConfParser.GLOBA_DB_NAME_KEY);
				prop.setProperty(TAtomConfParser.GLOBA_DB_NAME_KEY, dbName);
				return prop;
			}
		}, "globa", "oracle", "testOracleChange_切换DbName");
	}

	@Test
	public void testMsqlChange_切换DbName() throws IOException, AtomAlreadyInitException, Exception {
		testChange(new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.GLOBA_DB_NAME_KEY, "testWoKao");
				return prop;
			}
		}, new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				String dbName = prop.getProperty(TAtomConfParser.GLOBA_DB_NAME_KEY);
				prop.setProperty(TAtomConfParser.GLOBA_DB_NAME_KEY, dbName);
				return prop;
			}
		}, "globa", "mysql", "testMsqlChange_切换DbName");
	}

	@Test
	public void testOracleChange_切换userName() throws IOException, AtomAlreadyInitException, Exception {
		testChange(new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.APP_USER_NAME_KEY, "test1");
				return prop;
			}
		}, new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				String dbName = prop.getProperty(TAtomConfParser.APP_USER_NAME_KEY);
				prop.setProperty(TAtomConfParser.APP_USER_NAME_KEY, dbName);
				return prop;
			}
		}, "app", "oracle", "testOracleChange_切换userName");
	}

	@Test
	public void testMysqlChange_切换userName() throws IOException, AtomAlreadyInitException, Exception {
		testChange(new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.APP_USER_NAME_KEY, "test1");
				return prop;
			}
		}, new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				String dbName = prop.getProperty(TAtomConfParser.APP_USER_NAME_KEY);
				prop.setProperty(TAtomConfParser.APP_USER_NAME_KEY, dbName);
				return prop;
			}
		}, "app", "mysql", "testMysqlChange_切换userName");
	}

	@Test
	public void testChange_切换DBStatus() throws IOException, AtomAlreadyInitException, Exception {
		String appName = "unitTest1";
		String dbKey = "dev-db1";
		//初始化持久配置中心数据
		TAtomDataSource tAtomDataSource = createTAtomDataSource(appName, dbKey, "oracle");
		Assert.assertTrue(!tAtomDataSource.getDbStatus().isNaStatus());

		List<TAtomDbStatusListener> dbStatusListeners = new ArrayList<TAtomDbStatusListener>(1);
		final List<String> testList = Collections.synchronizedList(new ArrayList<String>(1));
		dbStatusListeners.add(new TAtomDbStatusListener() {
			public void handleData(AtomDbStatusEnum oldStatus, AtomDbStatusEnum newStatus) {
				testList.add("handleData");
			}
		});
		tAtomDataSource.setDbStatusListeners(dbStatusListeners);
		Properties prop = PropLoadTestUtil.loadPropFromFile("conf/" + "oracle" + "/globa.properties");
		prop.put(TAtomConfParser.GLOBA_DB_STATUS_KEY, AtomDbStatusEnum.NA_STATUS.getStatus());
		MockServer.setConfigInfo(TAtomConstants.getGlobalDataId(dbKey), PropLoadTestUtil.convertProp2Str(prop));
		Thread.sleep(1200);
		Assert.assertTrue(!testList.isEmpty());
		Assert.assertTrue(tAtomDataSource.getDbStatus().isNaStatus());
	}

	@Test
	public void testOracleChange_切换Passwd() throws IOException, AtomAlreadyInitException, Exception {
		testChangePasswd(new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.PASSWD_ENC_PASSWD_KEY, "-3e8955f636757c420baa8034f95c4c3a");
				return prop;
			}
		}, new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				String encPasswd = prop.getProperty(TAtomConfParser.PASSWD_ENC_PASSWD_KEY);
				prop.setProperty(TAtomConfParser.PASSWD_ENC_PASSWD_KEY, encPasswd);
				return prop;
			}
		}, "oracle");
	}

	@Test
	public void testMySqlChange_切换Passwd() throws IOException, AtomAlreadyInitException, Exception {
		testChangePasswd(new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				prop.setProperty(TAtomConfParser.PASSWD_ENC_PASSWD_KEY, "7c99c52f3e840742");
				return prop;
			}
		}, new ChangeTestConfig() {
			public Properties doChange(Properties prop) {
				String encPasswd = prop.getProperty(TAtomConfParser.PASSWD_ENC_PASSWD_KEY);
				prop.setProperty(TAtomConfParser.PASSWD_ENC_PASSWD_KEY, encPasswd);
				return prop;
			}
		}, "mysql");
	}

	private void testChangePasswd(ChangeTestConfig change, ChangeTestConfig restore, String dbType) throws IOException,
			AtomAlreadyInitException, Exception {
		String appName = "unitTest";
		String dbKey = "unitTestDb-" + dbType;
		String configName = "";
		String testSql = TAtomDataSourceUnitTest.TEST_SQL;
		configName = dbType;
		TAtomDataSource tAtomDataSource = createTAtomDataSource(appName, dbKey, configName);
		JdbcTemplate jtp = new JdbcTemplate(tAtomDataSource);
		int actual = jtp.queryForInt(testSql);
		Assert.assertEquals(actual, 1);
		//设置错误的IP进行推送
		//全局配置
		String globaStr = PropLoadTestUtil.loadPropFile2String("conf/" + configName + "/globa.properties");
		MockServer.setConfigInfo(TAtomConstants.getGlobalDataId(dbKey), globaStr);
		//应用配置
		String appStr = PropLoadTestUtil.loadPropFile2String("conf/" + configName + "/app.properties");
		MockServer.setConfigInfo(TAtomConstants.getAppDataId(appName, dbKey), appStr);
		//解析配置
		TAtomDsConfDO tAtomDsConfDO = TAtomConfParser.parserTAtomDsConfDO(globaStr, appStr);
		Properties passwdProp = PropLoadTestUtil.loadPropFromFile("conf/" + configName + "/psswd.properties");
		String passwdDataId = TAtomConstants.getPasswdDataId(tAtomDsConfDO.getDbName(), tAtomDsConfDO.getDbType(),
				tAtomDsConfDO.getUserName());
		MockServer.setConfigInfo(passwdDataId, PropLoadTestUtil.convertProp2Str(change.doChange(passwdProp)));
		Thread.sleep(1200);
		//期待出现错误
		boolean result = false;
		try {
			actual = jtp.queryForInt(testSql);
		} catch (Throwable e) {
			result = true;
		}
		Assert.assertTrue(result);
		MockServer.setConfigInfo(
				passwdDataId,
				PropLoadTestUtil.convertProp2Str(restore.doChange(PropLoadTestUtil.loadPropFromFile("conf/"
						+ configName + "/psswd.properties"))));
		Thread.sleep(1200);
		//期待结果正常
		actual = jtp.queryForInt(testSql);
		Assert.assertEquals(actual, 1);
		tAtomDataSource.destroyDataSource();

	}

	private void testChange(ChangeTestConfig change, ChangeTestConfig restore, String type, String dbType,
			String methodName) throws IOException, AtomAlreadyInitException, Exception {
		String appName = "unitTest";
		String dbKey = "unitTestDb-" + methodName;
		String configName = dbType;
		String testSql = TAtomDataSourceUnitTest.TEST_SQL;
		;
		TAtomDataSource tAtomDataSource = createTAtomDataSource(appName, dbKey, configName);
		JdbcTemplate jtp = new JdbcTemplate(tAtomDataSource);
		int actual = jtp.queryForInt(testSql);
		Assert.assertEquals(actual, 1);
		//设置错误的IP进行推送
		String fileName = "";
		String dataId = "";
		if (type.equals("globa")) {
			fileName = "/globa.properties";
			dataId = TAtomConstants.getGlobalDataId(dbKey);
		} else if (type.equals("app")) {
			fileName = "/app.properties";
			dataId = TAtomConstants.getAppDataId(appName, dbKey);
		}
		Properties prop = PropLoadTestUtil.loadPropFromFile("conf/" + configName + fileName);
		MockServer.setConfigInfo(dataId, PropLoadTestUtil.convertProp2Str(change.doChange(prop)));
		Thread.sleep(3000);
		//期待出现错误
		boolean result = false;
		try {
			actual = jtp.queryForInt(testSql);
		} catch (Throwable e) {
			result = true;
		}
		Assert.assertTrue(result);
		MockServer.setConfigInfo(
				dataId,
				PropLoadTestUtil.convertProp2Str(restore.doChange(PropLoadTestUtil.loadPropFromFile("conf/"
						+ configName + fileName))));
		Thread.sleep(3000);
		//期待结果正常
		actual = jtp.queryForInt(testSql);
		Assert.assertEquals(actual, 1);
		tAtomDataSource.destroyDataSource();
	}

	private TAtomDataSource createTAtomDataSource(String appName, String dbKey, String configName) throws IOException,
			AtomAlreadyInitException, Exception {
		//全局配置
		String globaStr = PropLoadTestUtil.loadPropFile2String("conf/" + configName + "/globa.properties");
		MockServer.setConfigInfo(TAtomConstants.getGlobalDataId(dbKey), globaStr);
		//应用配置
		String appStr = PropLoadTestUtil.loadPropFile2String("conf/" + configName + "/app.properties");
		MockServer.setConfigInfo(TAtomConstants.getAppDataId(appName, dbKey), appStr);
		//解析配置
		TAtomDsConfDO tAtomDsConfDO = TAtomConfParser.parserTAtomDsConfDO(globaStr, appStr);
		//密码配置
		String passwdStr = PropLoadTestUtil.loadPropFile2String("conf/" + configName + "/psswd.properties");
		MockServer.setConfigInfo(
				TAtomConstants.getPasswdDataId(tAtomDsConfDO.getDbName(), tAtomDsConfDO.getDbType(),
						tAtomDsConfDO.getUserName()), passwdStr);
		//进行初始化
		TAtomDataSource tAtomDataSource = new TAtomDataSource();
		tAtomDataSource.setAppName(appName);
		tAtomDataSource.setDbKey(dbKey);
		tAtomDataSource.init();
		return tAtomDataSource;
	}

	private interface ChangeTestConfig {
		Properties doChange(Properties prop);
	}
}
