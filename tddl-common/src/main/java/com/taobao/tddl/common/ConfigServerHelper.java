/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common;

import java.io.ByteArrayInputStream;import java.io.IOException;import java.io.Serializable;import java.text.MessageFormat;import java.util.Properties;import org.apache.commons.logging.Log;import org.apache.commons.logging.LogFactory;import com.taobao.tddl.common.config.ConfigDataHandler;import com.taobao.tddl.common.config.ConfigDataHandlerFactory;import com.taobao.tddl.common.config.ConfigDataListener;import com.taobao.tddl.common.config.impl.DefaultConfigDataHandlerFactory;
/**
 * 订阅持久化数据的辅助类
 * 
 * @author linxuan
 * 
 */
public final class ConfigServerHelper {
	private static final Log log = LogFactory.getLog(ConfigServerHelper.class);

	public static final int SUBSCRIBE_REGISTER_WAIT_TIME = 30000;

	public static final String DATA_ID_PREFIX = "com.taobao.tddl.v1_";

	public static final String DATA_ID_TDDL_SHARD_RULE = DATA_ID_PREFIX + "{0}_shardrule";

	public static final String DATA_ID_REPLICATION_SWITCH = DATA_ID_PREFIX + "{0}_replication.switch";

	public static final String DATA_ID_DBINFO_DBROLE = "com.taobao.dbinfo.dbrole.v1";

	public static final String DATA_ID_DB_GROUP_KEYS = DATA_ID_PREFIX + "{0}_dbgroups";

	/**
	 * syncServer遍历业务列表，订阅该配置，并且针对每个业务ID，本地配置一个TDatasource，用其初始化replication
	 */
	public static final String DATA_ID_REPLICATION = DATA_ID_PREFIX + "{0}_replication";

	/**
	 * 日志库的占位符不是业务ID而是日志库或syncServer的ID，因为多个业务可能共用一套日志库
	 */
	public static final String DATA_ID_SYNCLOG_DBSET = DATA_ID_PREFIX + "{0}_synclog.dbset";

	public static final String DATA_ID_SYNCLOG_DBWEIGHT = DATA_ID_PREFIX + "{0}_synclog.dbweight";

	/**
	 * 整体和局部的关系，权重有三种策略：
	 * 1. 每个dbIndex一个权重dataId，内容为该dbIndex下辖库的权重配置
	 * 2. 一个TDataSource(一个应用)一个权重dataId；包含所有dbIndex的权重配置
	 * 3. 两者都有，同时使用，dbIndex的权重配置，覆盖应用的大而全的权重配置
	 * 采用策略2
	 * 
	 * 一个TDataSource(一个应用)一个权重dataId； {0}:应用名称appName,如IC
	 * 内容格式：
	 *   slave_1=R10W10,R20W0
	 *   slave_3=R10W20,R20W10
	 *   slave_5=RW
	 *   master_0=RW
	 */
	public static final String DATA_ID_APP_DBWEIGHT = DATA_ID_PREFIX + "{0}.dbweight";

	/**
	 * 每个dbIndex一个权重dataId
	 * {0} 应用名称appName,如IC
	 * {1} dbIndex：TDataSource中dataSourcePool的key
	 * 
	 * 内容格式：
	 *   dbindex_0=R10W10,R20W0,R10W0 
	 */
	public static final String DATA_ID_GLOBAL_DBINFO = DATA_ID_PREFIX + "{0}.global.dbinfo";

	public static final String DATA_ID_TDDL_CLIENT_CONFIG = DATA_ID_PREFIX + "{0}_tddlconfig";

	public enum TDDLConfigKey {		statKeyRecordType, statKeyLeftCutLen, statKeyRightCutLen, statKeyExcludes, StatRealDbInWrapperDs, //		StatChannelMask, statDumpInterval/*秒*/, statCacheSize, statAtomSql, statKeyIncludes, //		SmoothValveProperties,CountPunisherProperties,//		//add by junyu		sqlExecTimeOutMilli/*sql超时时间*/,atomSqlSamplingRate/*atom层sql统计的采样率*/;	}

	/**
	 * 订阅应用的分库分表配置
	 */
	public static Object subscribeShardRuleConfig(String appName, DataListener listener) {
		if (appName == null || appName.length() == 0) {
			throw new IllegalStateException("没有指定应用名称appName");
		}
		String dataId = new MessageFormat(DATA_ID_TDDL_SHARD_RULE).format(new Object[] { appName });
		return ConfigServerHelper.subscribePersistentData(getCallerClassName(), dataId, listener);
	}

	/**
	 * 订阅应用的数据库权重配置
	 */
	public static Object subscribeAppDbWeight(String appName, DataListener listener) {
		if (appName == null || appName.length() == 0) {
			throw new IllegalStateException("没有指定应用名称appName");
		}
		String dataId = new MessageFormat(DATA_ID_APP_DBWEIGHT).format(new Object[] { appName });
		return ConfigServerHelper.subscribePersistentData(getCallerClassName(), dataId, listener);
	}

	/**
	 * 订阅应用的行复制配置
	 */
	public static Object subscribeReplicationConfig(String appName, DataListener listener) {
		if (appName == null || appName.length() == 0) {
			throw new IllegalStateException("没有指定应用名称appName");
		}
		String dataId = new MessageFormat(DATA_ID_REPLICATION).format(new Object[] { appName });
		return ConfigServerHelper.subscribePersistentData(getCallerClassName(), dataId, listener);
	}

	/**
	 * 订阅应用的行复制开关
	 */
	public static Object subscribeReplicationSwitch(String appName, DataListener listener) {
		if (appName == null || appName.length() == 0) {
			throw new IllegalStateException("没有指定应用名称appName");
		}
		String dataId = new MessageFormat(DATA_ID_REPLICATION_SWITCH).format(new Object[] { appName });
		return ConfigServerHelper.subscribePersistentData(getCallerClassName(), dataId, listener);
	}

	/**
	 * 订阅日志库配置
	 */
	public static Object subscribeSyncLogDbConfig(String syncServerID, DataListener listener) {
		if (syncServerID == null || syncServerID.length() == 0) {
			throw new IllegalStateException("没有指定补偿服务器ID：syncServerID");
		}
		String dataId = new MessageFormat(DATA_ID_SYNCLOG_DBSET).format(new Object[] { syncServerID });
		return ConfigServerHelper.subscribePersistentData(getCallerClassName(), dataId, listener);
	}

	/**
	 * 订阅日志库权重配置
	 */
	public static Object subscribeSyncLogDbWeight(String syncServerID, DataListener listener) {
		if (syncServerID == null || syncServerID.length() == 0) {
			throw new IllegalStateException("没有指定补偿服务器ID：syncServerID");
		}
		String dataId = new MessageFormat(DATA_ID_SYNCLOG_DBWEIGHT).format(new Object[] { syncServerID });
		return ConfigServerHelper.subscribePersistentData(getCallerClassName(), dataId, listener);
	}

	/**
	 * 订阅其他TDDL客户端配置
	 */
	public static Object subscribeTDDLConfig(String appName, DataListener listener) {
		if (appName == null || appName.length() == 0) {
			throw new IllegalStateException("没有指定应用名称appName");
		}
		String dataId = new MessageFormat(DATA_ID_TDDL_CLIENT_CONFIG).format(new Object[] { appName });
		return ConfigServerHelper.subscribePersistentData(getCallerClassName(), dataId, listener);
	}

	private static String getCallerClassName() {
		StackTraceElement[] stes = Thread.currentThread().getStackTrace();
		return stes[stes.length - 1].getClassName();
	}

	public static Object subscribeDbInofDbRoleDb(DataListener listener) {
		return ConfigServerHelper.subscribePersistentData(getCallerClassName(), DATA_ID_DBINFO_DBROLE, listener);
	}

	/**
	 * @return 第一次获取的data；返回时onDataReceiveAtRegister已经调用过一次
	 */
	public static Object subscribePersistentData(String dataId, final DataListener listener) {
		return subscribePersistentData(getCallerClassName(), dataId, listener);
	}

	public static Object subscribeData(String dataId, final DataListener listener) {
		//订阅非持久数据，和订阅持久数据的接口是完全相同的
		return subscribePersistentData(getCallerClassName(), dataId, listener);
	}

	private volatile static ConfigDataHandlerFactory cdhf;
	private static final long DIAMOND_FIRST_DATA_TIMEOUT=15*1000;
	
	public static Object subscribePersistentData(String subscriberName, String dataId, final DataListener listener) {
		cdhf = new DefaultConfigDataHandlerFactory();
		ConfigDataHandler matrixHandler = cdhf.getConfigDataHandler(dataId, null);
		String datas = matrixHandler.getData(DIAMOND_FIRST_DATA_TIMEOUT, ConfigDataHandler.FIRST_CACHE_THEN_SERVER_STRATEGY); //取配置信息的默认超时时间为30秒

		//尝试去拿最新的本地数据
		log.warn(dataId + "'s firstData=" + datas);
		if (datas != null) {
			try {
				listener.onDataReceiveAtRegister(datas);
			} catch (Throwable t) {
				//保证即使首次处理dataId发生异常，listener也一样会被注册，业务仍然能收到后续推送
				log.error("onDataReceiveAtRegister抛出异常，dataId:" + dataId, t);
			}
		}
		matrixHandler.addListener(new ConfigDataListener() {
			@Override
			public void onDataRecieved(String dataId, String data) {
				log.info("recieve data,data id:"+dataId+" data:"+data);
				listener.onDataReceive(data);
			}
		},null);

		return datas;
	}

	/**
	 * 一个Util方法，不想再搞个Util类，故放到这里：
	 * 将Properties对象或Properties字符串解析为Properties对象 
	 */
	public static Properties parseProperties(Object data, String msg) {
		Properties p;
		if (data == null) {
			log.warn(msg + "data == null");
			return null;
		} else if (data instanceof Properties) {
			p = (Properties) data;
		} else if (data instanceof String) {
			p = new Properties();
			try {
				p.load(new ByteArrayInputStream(((String) data).getBytes()));
			} catch (IOException e) {
				log.error(msg + "无法解析推送的配置：" + data, e);
				return null;
			}
		} else {
			log.warn(msg + "类型无法识别" + data);
			return null;
		}
		return p;
	}

	//测试辅助方法，发布一个持久数据
	public static void publish(String dataId, Serializable data) {
		publish(dataId, data, null);
	}

	public static void publish(String dataId, Serializable data, String group) {
		return;
		//Diamond要用额外的sdk发布数据。如果测试还是用mock
		//TODO 改成sdk方式，支持测试
	}

	/*private static Object fetchConfig(Subscriber subscriber) {
		try {
			Subscription subscription = subscriber.getSubscription();
			List<Object> data = subscription.waitNext(10);
			if (data == null || data.size() == 0) {
				data = subscription.waitNext(SUBSCRIBE_REGISTER_WAIT_TIME);
			} else {
				List<Object> data2 = subscription.waitNext(SUBSCRIBE_REGISTER_WAIT_TIME);
				if (data2 != null && data2.size() != 0) {
					data = data2;
				}
			}
			return data == null || data.size() == 0 ? null : data.get(0);
		} catch (CancellationException e) {
			log.error("", e);
			return null;
		} catch (InterruptedException e) {
			log.error("", e);
			return null;
		}
	}*/

	public static interface DataListener {
		/**
		 * 注册之后，设置DataObserver之前，fetchConfig接受到推送时，调用该方法。一般用在业务初始化时
		 */
		void onDataReceiveAtRegister(Object data);

		/**
		 * 注册之后，第一次接收到推送调用onDataReceiveAtRegister处理完毕，
		 * 设置DataObserver之后，再接到动态推送，调用该方法。一般用在业务运行时
		 */
		void onDataReceive(Object data);
	}

	public static abstract class AbstractDataListener implements DataListener {
		public void onDataReceiveAtRegister(Object data) {
			this.onDataReceive(data);
		}
	}

	public static String getDBGroupsConfig(String appName) {
		if (appName == null || appName.length() == 0) {
			throw new IllegalStateException("没有指定应用名称appName");
		}
		String dataId = new MessageFormat(DATA_ID_DB_GROUP_KEYS).format(new Object[] { appName });
		return dataId;
	}

	public static String getShardRuleConfig(String appName) {
		if (appName == null || appName.length() == 0) {
			throw new IllegalStateException("没有指定应用名称appName");
		}
		String dataId = new MessageFormat(DATA_ID_TDDL_SHARD_RULE).format(new Object[] { appName });
		return dataId;
	}
}
