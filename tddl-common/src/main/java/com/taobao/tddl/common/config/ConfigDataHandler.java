/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//Copyrigh(c) Taobao.com
package com.taobao.tddl.common.config;

import java.util.List;import java.util.Map;import java.util.concurrent.Executor;
/**
 * @author <a href="zylicfc@gmail.com">junyu</a>
 * @version 1.0
 * @since 1.6
 * @date 2011-1-11上午11:22:29
 * @desc 获取配置的处理器
 */
public interface ConfigDataHandler {
	public static final String FIRST_SERVER_STRATEGY = "firstServer";
	public static final String FIRST_CACHE_THEN_SERVER_STRATEGY="firstCache";

	/**
	 * DefaultConfigDataHandler会在 实例化具体的Handler之后调用此方法 给予Handler相关信息
	 * @param dataId             数据在配置平台上注册的id
	 * @param listenerList       数据监听器列表
	 * @param prop               全局配置和运行时
	 */
	void init(String dataId, List<ConfigDataListener> listenerList,
			Map<String, Object> prop);

	/**
	 * 从配置中心拉取数据
	 * @param timeout    获取配置信息超时时间
	 * @param strategy   获取配置策略
	 * @return 
	 */
	String getData(long timeout, String strategy);

	/**
	 * 为推送过来的数据注册处理的监听器
	 * @param configDataListener    监听器
	 * @param executor              执行的executor
	 */
	void addListener(ConfigDataListener configDataListener, Executor executor);

	/**
	 * 为推送过来的数据注册多个处理监听器
	 * @param configDataListenerList  监听器列表
	 * @param executor                执行的executor
	 */
	void addListeners(List<ConfigDataListener> configDataListenerList,
			Executor executor);

	/**
	 * 停止底层配置管理器
	 */
	void closeUnderManager();
}
