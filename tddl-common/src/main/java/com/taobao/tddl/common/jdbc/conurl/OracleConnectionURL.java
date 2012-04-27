/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.jdbc.conurl;

import java.text.MessageFormat;

import com.taobao.tddl.interact.rule.bean.DBType;

/**ORACLE连接地址对象，需要设置IP,PORT,DBNAME,CONTYPE后调用renderURL即可生成连接的字符串
 * @author qihao
 *
 */
public class OracleConnectionURL extends  ConnectionURL{
	
	public final static int THIN_TYPE=0;
	public final static int OCI_IP_PORT_SID_TYPE=1;
	public final static int OCI_IP_PORT_NAME_TYPE=2;
	public final static int OCI_SID_TYPE=3;
	
	private static MessageFormat THIN_FORMAT=new MessageFormat("jdbc:oracle:thin:@{0}:{1}:{2}");
	private static MessageFormat OCI_IP_PORT_SID_FORMAT=new MessageFormat("jdbc:oracle:oci:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST={0})(PORT={1})))(CONNECT_DATA=(SERVER=DEDICAT)(SID={2})))");
	private static MessageFormat OCI_IP_PORT_NAME_FORMAT=new MessageFormat("jdbc:oracle:oci:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST={0})(PORT={1})))(CONNECT_DATA=(SERVER=DEDICAT)(SERVICE_NAME={2})))");
	private static MessageFormat OCI_SID_FORMAT=new MessageFormat("jdbc:oracle:oci:@{0}");
	
	private int conType;
	
	public String renderURL() {
		String url=null;
		switch (this.getConType()){
		case 0:
			url=THIN_FORMAT.format(new String[] {this.getIp(),this.getPort(),this.getDbName() });
			break;
		case 1:
			url=OCI_IP_PORT_SID_FORMAT.format(new String[] {this.getIp(),this.getPort(),this.getDbName() });
			break;
		case 2:
			url=OCI_IP_PORT_NAME_FORMAT.format(new String[] {this.getIp(),this.getPort(),this.getDbName() });
			break;
		case 3:
			url=OCI_SID_FORMAT.format(new String[] {this.getDbName()});
		}
		return url;
	}

	public DBType getDbType() {
		return DBType.ORACLE;
	}

	public int getConType() {
		return conType;
	}

	public void setConType(int conType) {
		this.conType = conType;
	}
}
