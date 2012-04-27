/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.config;

import com.taobao.tddl.common.config.ConfigDataListener;

/**
 * TAtom数据源全局和应用的配置管理接口定义
 * 
 * @author qihao
 *
 */
public interface DbConfManager {
	/**获取全局配置
	 * 
	 * @return
	 */
	public String getGlobalDbConf();

	/**获取应用配置
	 * 
	 * @return
	 */
	public String getAppDbDbConf();

	/**
	 * 注册全局配置监听
	 * 
	 * @param Listener
	 */
	public void registerGlobaDbConfListener(ConfigDataListener Listener);

	/**注册应用配置监听
	 * 
	 * @param Listener
	 */
	public void registerAppDbConfListener(ConfigDataListener Listener);

	/**
	 * 停止DbConfManager
	 */
	public void stopDbConfManager();
}
