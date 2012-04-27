/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.tddl.common.TDDLConstant;
import com.taobao.tddl.common.config.ConfigDataHandler;
import com.taobao.tddl.common.config.ConfigDataHandlerFactory;
import com.taobao.tddl.common.config.ConfigDataListener;
import com.taobao.tddl.common.config.impl.DefaultConfigDataHandlerFactory;
import com.taobao.tddl.jdbc.atom.common.TAtomConfParser;
import com.taobao.tddl.jdbc.atom.common.TAtomConstants;

/**
 * 密码管理器Diamond实现
 * 
 * @author qihao
 * 
 */
public class DiamondDbPasswdManager implements DbPasswdManager {
	private static Log logger = LogFactory
			.getLog(DiamondDbPasswdManager.class);
	private String passwdConfDataId;
	private ConfigDataHandlerFactory configFactory;
	private ConfigDataHandler passwdHandler;
	private volatile List<ConfigDataListener> passwdConfListener = new ArrayList<ConfigDataListener>();

	public void init() {
		configFactory = new DefaultConfigDataHandlerFactory();
		Map<String, String> config = new HashMap<String, String>();
		config.put("group", TAtomConstants.DEFAULT_DIAMOND_GROUP);
		passwdHandler = configFactory.getConfigDataHandlerWithListenerListCE(
				passwdConfDataId, passwdConfListener,
				Executors.newSingleThreadScheduledExecutor(), config);
	}

	public String getPasswd() {
		if (null != passwdHandler) {
			String passwdStr = passwdHandler.getData(TDDLConstant.DIAMOND_GET_DATA_TIMEOUT,ConfigDataHandler.FIRST_CACHE_THEN_SERVER_STRATEGY);
			return TAtomConfParser.parserPasswd(passwdStr);
		}
		logger.error("[getDataError] passwdConfig not init !");
		return null;
	}

	public void registerPasswdConfListener(ConfigDataListener Listener) {
		passwdConfListener.add(Listener);
	}

	public void setPasswdConfDataId(String passwdConfDataId) {
		this.passwdConfDataId = passwdConfDataId;
	}

	public void stopDbPasswdManager() {
		if (null != this.passwdHandler) {
			this.passwdHandler.closeUnderManager();
		}
	}
}
