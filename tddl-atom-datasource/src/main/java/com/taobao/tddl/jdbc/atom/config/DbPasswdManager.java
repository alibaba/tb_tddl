/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.atom.config;

import com.taobao.tddl.common.config.ConfigDataListener;

public interface DbPasswdManager {
	
	/**获取数据库密码
	 * @return
	 */
	public String getPasswd();
	
	/**注册应用配置监听
	 * 
	 * @param Listener
	 */
	public void registerPasswdConfListener(ConfigDataListener Listener);
	
	/**
	 * 停止DbPasswdManager
	 */
	public void stopDbPasswdManager();
}
