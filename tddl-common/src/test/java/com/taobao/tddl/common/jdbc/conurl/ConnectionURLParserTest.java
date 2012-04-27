/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.jdbc.conurl;

import junit.framework.Assert;

import org.junit.Test;

import com.taobao.tddl.interact.rule.bean.DBType;

public class ConnectionURLParserTest {
	
	@Test
	public void testPaserNullConurl(){
		Assert.assertEquals(null, ConnectionURLParser.parserConnectionURL(null));
		Assert.assertEquals(null, ConnectionURLParser.parserConnectionURL(""));
		Assert.assertEquals(null, ConnectionURLParser.parserConnectionURL("  "));
	}
	
	@Test
	public void testParserMsqlConUrl(){
		String mySqlurl="jdbc:mysql://127.0.0.1:3308/tfs_back?characterEncoding=gbk";
		ConnectionURL connectionURL=ConnectionURLParser.parserConnectionURL(mySqlurl);
		Assert.assertEquals(DBType.MYSQL, connectionURL.getDbType());
		Assert.assertEquals("127.0.0.1", connectionURL.getIp());
		Assert.assertEquals("3308", connectionURL.getPort());
		Assert.assertEquals("tfs_back", connectionURL.getDbName());
		Assert.assertEquals(mySqlurl, connectionURL.renderURL());
		//变更IP,PORT,DBNAME相关参数
		connectionURL.setDbName("tfs ");
		connectionURL.setIp("127.0.0.1 ");
		connectionURL.setPort("3309 ");
		String changeMySqlurl="jdbc:mysql://127.0.0.1:3309/tfs?characterEncoding=gbk";
		Assert.assertEquals(changeMySqlurl, connectionURL.renderURL());
		
		//不带参数的连接地址解析
		String mySqlurl1="jdbc:mysql://127.0.0.1:3305/test";
		ConnectionURL connectionURL1=ConnectionURLParser.parserConnectionURL(mySqlurl1);
		Assert.assertEquals(DBType.MYSQL, connectionURL1.getDbType());
		Assert.assertEquals("127.0.0.1", connectionURL1.getIp());
		Assert.assertEquals("3305", connectionURL1.getPort());
		Assert.assertEquals("test", connectionURL1.getDbName());
		Assert.assertEquals(mySqlurl1, connectionURL1.renderURL());
		//变更IP,PORT,DBNAME相关参数
		connectionURL1.setDbName("test1");
		connectionURL1.setIp("127.0.0.1");
		connectionURL1.setPort("3306");
		String changeMySqlurl1="jdbc:mysql://127.0.0.1:3306/test1";
		Assert.assertEquals(changeMySqlurl1, connectionURL1.renderURL());
	}
	
	@Test
	public void testParserOracle_Thin_ConUrl(){
		String oralceUrl="jdbc:oracle:thin:@127.0.0.1:1521:dev_ark";
		ConnectionURL connectionURL=ConnectionURLParser.parserConnectionURL(oralceUrl);
		Assert.assertEquals(DBType.ORACLE, connectionURL.getDbType());
		Assert.assertEquals("127.0.0.1", connectionURL.getIp());
		Assert.assertEquals("1521", connectionURL.getPort());
		Assert.assertEquals("dev_ark", connectionURL.getDbName());
		Assert.assertEquals(oralceUrl, connectionURL.renderURL());
		//变更IP,PORT,SID相关参数
		connectionURL.setIp("127.0.0.1");
		connectionURL.setPort("1522");
		connectionURL.setDbName("bak");
		String changeOracleUrl1="jdbc:oracle:thin:@127.0.0.1:1522:bak";
		Assert.assertEquals(changeOracleUrl1, connectionURL.renderURL()); 
	}
	
	@Test
	public void testParserOracle_Oci_Ip_Port_Sid_ConUrl(){
		String oralceUrl="jdbc:oracle:oci:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=127.0.0.1)(PORT=1521)))(CONNECT_DATA=(SERVER=DEDICAT)(SID=test)))";
		ConnectionURL connectionURL=ConnectionURLParser.parserConnectionURL(oralceUrl);
		Assert.assertEquals(DBType.ORACLE, connectionURL.getDbType());
		Assert.assertEquals("127.0.0.1", connectionURL.getIp());
		Assert.assertEquals("1521", connectionURL.getPort());
		Assert.assertEquals("test", connectionURL.getDbName());
		Assert.assertEquals(oralceUrl, connectionURL.renderURL());
		//变更IP,PORT,SID相关参数
		connectionURL.setIp(" 127.0.0.1");
		connectionURL.setPort(" 1522");
		connectionURL.setDbName(" bak");
		String changeOracleUrl1="jdbc:oracle:oci:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=127.0.0.1)(PORT=1522)))(CONNECT_DATA=(SERVER=DEDICAT)(SID=bak)))";
		Assert.assertEquals(changeOracleUrl1, connectionURL.renderURL()); 
	}
	
	@Test
	public void testParserOracle_Oci_Ip_Port_Name_ConUrl(){
		String oralceUrl="jdbc:oracle:oci:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=127.0.0.1)(PORT=1521)))(CONNECT_DATA=(SERVER=DEDICAT)(SERVICE_NAME=test)))";
		ConnectionURL connectionURL=ConnectionURLParser.parserConnectionURL(oralceUrl);
		Assert.assertEquals(DBType.ORACLE, connectionURL.getDbType());
		Assert.assertEquals("127.0.0.1", connectionURL.getIp());
		Assert.assertEquals("1521", connectionURL.getPort());
		Assert.assertEquals("test", connectionURL.getDbName());
		Assert.assertEquals(oralceUrl, connectionURL.renderURL());
		//变更IP,PORT,SID相关参数
		connectionURL.setIp(" 127.0.0.1");
		connectionURL.setPort(" 1522");
		connectionURL.setDbName(" bak");
		String changeOracleUrl1="jdbc:oracle:oci:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=127.0.0.1)(PORT=1522)))(CONNECT_DATA=(SERVER=DEDICAT)(SERVICE_NAME=bak)))";
		Assert.assertEquals(changeOracleUrl1, connectionURL.renderURL()); 
	}
	
	@Test
	public void testParserOracle_Oci_Sid_ConUrl(){
		String oralceUrl="jdbc:oracle:oci:@test";
		ConnectionURL connectionURL=ConnectionURLParser.parserConnectionURL(oralceUrl);
		Assert.assertEquals(DBType.ORACLE, connectionURL.getDbType());
		Assert.assertEquals(null, connectionURL.getIp());
		Assert.assertEquals(null, connectionURL.getPort());
		Assert.assertEquals("test", connectionURL.getDbName());
		Assert.assertEquals(oralceUrl, connectionURL.renderURL());
		//变更SID相关参数
		connectionURL.setDbName(" test1");
		String changeOracleUrl1="jdbc:oracle:oci:@test1";
		Assert.assertEquals(changeOracleUrl1, connectionURL.renderURL()); 
	}
}
