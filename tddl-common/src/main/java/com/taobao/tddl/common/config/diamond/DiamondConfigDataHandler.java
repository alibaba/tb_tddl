/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.config.diamond;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.diamond.manager.DiamondManager;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.manager.impl.DefaultDiamondManager;
import com.taobao.tddl.common.config.ConfigDataHandler;
import com.taobao.tddl.common.config.ConfigDataListener;
import com.taobao.tddl.common.util.TDDLMBeanServer;
import com.taobao.tddl.common.util.mbean.TDDLMBean;

/**
 * @author shenxun
 * @author <a href="zylicfc@gmail.com">junyu</a>
 * @version 1.0
 * @since 1.6
 * @date 2011-1-11上午11:22:29
 * @desc 持久配置中心diamond实现
 */
public class DiamondConfigDataHandler implements ConfigDataHandler {
	private static final Log logger = LogFactory
			.getLog(DiamondConfigDataHandler.class);
	private DiamondManager diamondManager;
	private String dataId;
	private String mbeanId;
	private TDDLMBean mbean;

	public void init(final String dataId,
			final List<ConfigDataListener> configDataListenerList,
			final Map<String, Object> config) {
		mbean = new TDDLMBean("Diamond Config Info "
				+ System.currentTimeMillis());
		mbeanId = dataId + System.currentTimeMillis();
		DiamondConfig.handleConfig(config);
		DefaultDiamondManager.Builder builder = new DefaultDiamondManager.Builder(
				dataId, new ManagerListener() {
					public void receiveConfigInfo(String data) {
						if (configDataListenerList != null) {
							for (ConfigDataListener configDataListener : configDataListenerList) {
								configDataListener.onDataRecieved(dataId, data);
							}

							if (data != null) {
								mbean.setAttribute(dataId, data);
							} else {
								mbean.setAttribute(dataId, "");
							}
						}
					}

					public Executor getExecutor() {
						return (Executor) config.get("executor");
					}
				});
		String group = (String) config.get("group");
		if (null != group) {
			builder.setGroup(group);
		}
		this.diamondManager = builder.build();
		this.dataId = dataId;

		TDDLMBeanServer.registerMBeanWithId(mbean, mbeanId);
	}

	public String getData(long timeout, String strategy) {
		String data = null;
		if (strategy != null
				&& strategy
						.equals(ConfigDataHandler.FIRST_CACHE_THEN_SERVER_STRATEGY)) {
			data = diamondManager.getAvailableConfigureInfomation(timeout);
		} else if (strategy != null
				&& strategy.equals(ConfigDataHandler.FIRST_SERVER_STRATEGY)) {
			data = diamondManager.getConfigureInfomation(timeout);
		}

		if (data != null) {
			mbean.setAttribute(dataId, data);
		} else {
			mbean.setAttribute(dataId, "");
		}
		return data;
	}

	public void addListener(final ConfigDataListener configDataListener,
			final Executor executor) {
		if (configDataListener != null) {
			diamondManager.setManagerListener(new ManagerListener() {
				public void receiveConfigInfo(String data) {
					configDataListener.onDataRecieved(dataId, data);
					if (data != null) {
						mbean.setAttribute(dataId, data);
					} else {
						mbean.setAttribute(dataId, "");
					}
				}

				public Executor getExecutor() {
					return executor;
				}
			});
		}
	}

	public void addListeners(
			final List<ConfigDataListener> configDataListenerList,
			final Executor executor) {
		if (configDataListenerList != null) {
			diamondManager.setManagerListener(new ManagerListener() {
				public void receiveConfigInfo(String data) {
					for (ConfigDataListener configDataListener : configDataListenerList) {
						try {
							configDataListener.onDataRecieved(dataId, data);
						} catch (Exception e) {
							logger.error("one of listener failed", e);
							continue;
						}
					}

					if (data != null) {
						mbean.setAttribute(dataId, data);
					} else {
						mbean.setAttribute(dataId, "");
					}
				}

				public Executor getExecutor() {
					return executor;
				}
			});
		}
	}

	public void closeUnderManager() {
		diamondManager.close();
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}
}
