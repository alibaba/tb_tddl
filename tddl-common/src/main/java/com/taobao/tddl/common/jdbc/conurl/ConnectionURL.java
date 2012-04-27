/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.jdbc.conurl;

import com.taobao.tddl.common.util.TStringUtil;import com.taobao.tddl.interact.rule.bean.DBType;
/**连接地址抽象类需要通过ConnectionURLParser 解析后获得具体的对象
 * @author qihao
 *
 */
public abstract class ConnectionURL {
	private String ip;

	private String port;

	private String dbName;

	public abstract DBType getDbType();

	public abstract String renderURL();

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = TStringUtil.trim(ip);
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = TStringUtil.trim(port);
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName =  TStringUtil.trim(dbName);
	}
}
