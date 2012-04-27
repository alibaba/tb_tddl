/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author whisper
 * @author <a href="zylicfc@gmail.com">junyu</a>
 * @version 1.0
 * @since 1.6
 * @date 2011-1-11上午11:22:29
 * @desc 得到具体的配置处理器实例
 */
public interface ConfigDataHandlerFactory {
	/**
	 * 对某一个dataId进行监听
	 * @param dataId   数据在配置中心注册的id
	 * @return         返回配置数据处理器实例
	 */
	ConfigDataHandler getConfigDataHandler(String dataId);
	
	/**
	 * 对某一个dataId进行监听，使用者提供回调监听器
	 * @param dataId                数据在p诶值中心注册的id
	 * @param configDataListener    数据回调监听器
	 * @return                      返回配置数据处理器实例
	 */
	ConfigDataHandler getConfigDataHandler(String dataId,
			ConfigDataListener configDataListener);

	/**
	 * 对某一个dataId进行监听，使用者提供回调监听器列表，处理器收到配置信息时
	 * ，逐个调用监听器的回调方法
	 * @param dataId                 数据在配置中心注册的id
	 * @param configDataListenerList 数据回调监听器列表
	 * @return                       返回配置数据处理器实例
	 */
	ConfigDataHandler getConfigDataHandlerWithListenerList(String dataId,
			List<ConfigDataListener> configDataListenerList);

	/**
	 * 对某一个dataId进行监听，使用者提供回调监听器，并且提供内部一些配置(可能被handler忽视)
	 * @param dataId              数据在配置中心注册的id
   	 * @param configDataListener  数据回调监听器
   	 * @param config              TDDL内部对handler提供的一些配置
	 * @return                    返回配置数据处理器实例
	 */
	ConfigDataHandler getConfigDataHandlerC(String dataId,
			ConfigDataListener configDataListener,
			Map<String, String> config);

	/**
	 * 对某一个dataId进行监听,使用者提供回调监听器列表，并且提供内部一些配置(可能被handler忽视)
	 * @param dataId                  数据在配置中心注册的id
	 * @param configDataListenerList  数据回调监听器列表
	 * @param config                  TDDL内部对handler提供的一些配置
	 * @return                        返回配置数据处理器实例
	 */
	ConfigDataHandler getConfigDataHandlerWithListenerListC(String dataId,
			List<ConfigDataListener> configDataListenerList,
			Map<String, String> config);

	/**
	 * 对某一个dataId进行监听，使用者提供回调监听器，并且提供执行线程池
	 * @param dataId                  数据在配置中心注册的id
	 * @param configDataListener      数据回调监听器
	 * @param executor                数据接收处理线程池
	 * @return                        返回配置数据处理器实例
	 */
	ConfigDataHandler getConfigDataHandlerE(String dataId,
			ConfigDataListener configDataListener, Executor executor);

	/**
	 * 对某一个dataId进行监听，使用者提供回调监听器列表，并且提供执行线程池
	 * @param dataId                  数据在配置中心注册的id
	 * @param configDataListenerList  数据回调监听器列表
	 * @param executor                数据接收处理线程池
	 * @return                        返回配置数据处理器实例
	 */
	ConfigDataHandler getConfigDataHandlerWithListenerListE(String dataId,
			List<ConfigDataListener> configDataListenerList, Executor executor);

	/**
	 * 对某一个dataId进行监听，使用者提供回调监听器，
	 * 并且提供执行线程池和内部一些配置(可能被handler忽视)
	 * @param dataId                数据在配置中心注册的id
	 * @param configDataListener    数据回调监听器
	 * @param executor              数据接收处理线程池
	 * @param config                TDDL内部对handler提供的一些配置
	 * @return                      返回配置数据处理器实例
	 */
	ConfigDataHandler getConfigDataHandlerCE(String dataId,
			ConfigDataListener configDataListener, Executor executor,
			Map<String, String> config);

	/**
	 * 对某一个dataId进行监听，使用者提供回调监听器列表，
	 * 并且提供执行线程池和内部一些配置(可能被handler忽视)
	 * @param dataId                  数据在配置中心注册的id
	 * @param configDataListenerList  数据回调监听器列表
	 * @param executor                数据接收处理线程池
	 * @param config                  TDDL内部对handler提供的一些配置
	 * @return                        返回配置数据处理器实例
	 */
	ConfigDataHandler getConfigDataHandlerWithListenerListCE(String dataId,
			List<ConfigDataListener> configDataListenerList, Executor executor,
			Map<String, String> config);
}
