/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.diamond.mockserver.MockServer;

public class DynamicLogTest {
	private static Log log = LogFactory.getLog(DynamicLogTest.class);

	@Before
	public void beforeClass() {
		MockServer.setUpMockServer();
	}

	@After
	public void after() {
		MockServer.tearDownMockServer();
	}

	@Test
	public void test() {

	}

	private static String getSpringXmlString(String key, String script) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"gb2312\"?>");
		sb.append("<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\"");
		sb.append(" \"http://www.springframework.org/dtd/spring-beans.dtd\">");
		sb.append("<beans>");
		sb.append("	<bean id=\"test\" class=\"java.lang.String\">");
		sb.append("		<constructor-arg><value>").append(script).append("</value></constructor-arg>");
		sb.append("	</bean>");
		sb.append("</beans>");
		return sb.toString();
	}

	public static void main(String[] args) throws InterruptedException {
		MockServer.setUpMockServer();

		DynamicLog dynamicLog = DynamicLog.getInstance("test");
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 300; i++) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					MockServer.setConfigInfo("com.taobao.tddl.v1_test_buryPoints", getSpringXmlString("test", //
							i + "+ 'th:date='+(java.util.Date)args[0]"));
				}
			}
		}).start();

		for (int i = 0; i < 300; i++) {
			dynamicLog.warn("test", new Object[] { new Date() }, "defaultLog", log);
			Thread.sleep(1000);
		}

		MockServer.tearDownMockServer();
	}
}
