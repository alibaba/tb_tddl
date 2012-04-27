/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.jdbc.conurl;

import com.taobao.tddl.common.util.TStringUtil;
/**数据源连接地址解析类
 * @author qihao
 *
 */
public abstract class ConnectionURLParser {
	
	static String ORACLE_FIX="jdbc:oracle:";
	static String ORACLE_THIN_FIX="jdbc:oracle:thin:@";
	static String ORACLE_OCI_FIX="jdbc:oracle:oci:@";
	static String MYSQL_FIX="jdbc:mysql://";
	
	public static ConnectionURL parserConnectionURL(String url){
		ConnectionURL connectionURL=null;
		if(TStringUtil.isBlank(url)){
			return connectionURL;
		}
		if(TStringUtil.contains(url, ORACLE_FIX)){
			OracleConnectionURL oracleConUrl=new OracleConnectionURL();
			if(TStringUtil.contains(url, ORACLE_THIN_FIX)){
				//jdbc:oracle:thin:@IP:Port:SID
				String dbinfo=TStringUtil.substringAfterLast(url, "@");
				String[] ipPortAndSid=TStringUtil.splitm(dbinfo,":");
				oracleConUrl.setIp(ipPortAndSid[0]);
				oracleConUrl.setPort(ipPortAndSid[1]);
				oracleConUrl.setDbName(ipPortAndSid[2]);
				oracleConUrl.setConType(OracleConnectionURL.THIN_TYPE);
			}else if(TStringUtil.contains(url, ORACLE_OCI_FIX)){
				if(TStringUtil.contains(url, "(")){
					String ip=TStringUtil.substringBefore(TStringUtil.substringAfter(url, "HOST="), ")");
					String port=TStringUtil.substringBefore(TStringUtil.substringAfter(url, "PORT="), ")");
					oracleConUrl.setIp(ip);
					oracleConUrl.setPort(port);
					if(TStringUtil.contains(url, "SID=")){
						oracleConUrl.setConType(OracleConnectionURL.OCI_IP_PORT_SID_TYPE);
						oracleConUrl.setDbName(TStringUtil.substringBefore(TStringUtil.substringAfter(url, "SID="), ")"));
					}else{
						oracleConUrl.setConType(OracleConnectionURL.OCI_IP_PORT_NAME_TYPE);
						oracleConUrl.setDbName(TStringUtil.substringBefore(TStringUtil.substringAfter(url, "SERVICE_NAME="), ")"));
					}
				}else{
					//jdbc:oracle:oci:@SID，这种方式无法获得IP和端口
					oracleConUrl.setDbName(TStringUtil.substringAfterLast(url, "@"));
					oracleConUrl.setConType(OracleConnectionURL.OCI_SID_TYPE);
				}
			}
			connectionURL=oracleConUrl;
		}else if(TStringUtil.contains(url, MYSQL_FIX)){
			//jdbc:mysql://hostname:port/dbname?param1=value1&m2=value2
			MySqlConnectionURL mySqlConURL=new MySqlConnectionURL();
			mySqlConURL.setPramStr(TStringUtil.substringAfter(url, "?"));
			//截取DBName
			String dbInfoString=TStringUtil.substringBefore(url, "?");
			String dbName=TStringUtil.substringAfterLast(dbInfoString, "/");
			mySqlConURL.setDbName(dbName);
			//截取IP和PORT
			String hostString=TStringUtil.substringBeforeLast(dbInfoString, "/");
			hostString=TStringUtil.substringAfterLast(hostString, MYSQL_FIX);
			String[] ipAndPort=TStringUtil.splitm(hostString,":");
			mySqlConURL.setIp(ipAndPort[0]);
			mySqlConURL.setPort(ipAndPort[1]);
			connectionURL=mySqlConURL;
		}
		return connectionURL;
	}
}
