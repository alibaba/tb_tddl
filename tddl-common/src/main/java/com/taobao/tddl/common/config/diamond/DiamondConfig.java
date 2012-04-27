/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//Copyright(c) Taobao.com
package com.taobao.tddl.common.config.diamond;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.taobao.diamond.client.DiamondConfigure;
import com.taobao.diamond.client.impl.DiamondClientFactory;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a>
 * @version 1.0
 * @since 1.6
 * @date 2011-1-17ÏÂÎç07:21:41
 */
public class DiamondConfig {
	private volatile static DiamondConfigure configure = DiamondClientFactory
			.getSingletonDiamondSubscriber().getDiamondConfigure();

	protected static void handleConfig(Map<String,Object> prop) {
		if(null==prop){
			return;
		}
		
		Object interval = prop.get("pollingIntervalTime");
		if (interval != null) {
			configure.setPollingIntervalTime((Integer) interval);
		}

		Object connectTimeOut = prop.get("connectTimeOut");
		if (connectTimeOut != null) {
			configure.setConnectionTimeout((Integer) connectTimeOut);
		}

		Object onceTimeout = prop.get("onceTimeout");
		if (onceTimeout != null) {
			configure.setOnceTimeout((Integer) onceTimeout);
		}

		Object receiveWaitTime = prop.get("receiveWaitTime");
		if (receiveWaitTime != null) {
			configure.setReceiveWaitTime((Integer) receiveWaitTime);
		}

		Object domainNames = prop.get("domainNames");
		if (domainNames != null) {
			String[] domains = String.valueOf(domainNames).split(",");
			List<String> domainNameList = Arrays.asList(domains);
			configure.setDomainNameList(domainNameList);
		}

		Object maxHostConns = prop.get("maxHostConns");
		if (maxHostConns != null) {
			configure.setMaxHostConnections((Integer) maxHostConns);
		}

		Object connStateCheckEnable = prop.get("connStateCheckEnable");
		if (connStateCheckEnable != null) {
			configure.setConnectionStaleCheckingEnabled(Boolean.valueOf(String
					.valueOf(connStateCheckEnable)));
		}
		
		Object maxTotalConns=prop.get("maxTotalConns");
		if (maxTotalConns != null) {
			configure.setMaxTotalConnections((Integer)maxTotalConns);
		}
		
		Object filePath=prop.get("filePath");
		if (filePath != null) {
			configure.setFilePath((String)filePath);
		}
		
		Object port=prop.get("port");
		if (port != null) {
			configure.setFilePath((String)port);
		}
	}
}
