/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common;

import groovy.lang.GroovyClassLoader;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;

import com.taobao.tddl.common.ConfigServerHelper.AbstractDataListener;
import com.taobao.tddl.common.ConfigServerHelper.DataListener;
import com.taobao.tddl.common.util.StringXmlApplicationContext;

/**
 * 动态埋点/Log扩展点
 * 
 * @author linxuan
 *
 */
public class DynamicLog {
	public static interface LogBuilder {
		String build(Object[] args);
	}

	private static final Log log = LogFactory.getLog(DynamicLog.class);
	private static final Map<String, DynamicLog> multiInstance = new HashMap<String, DynamicLog>();

	/**
	 * 多例模式
	 */
	public static DynamicLog getInstance(String appName) {
		DynamicLog instance = multiInstance.get(appName);
		if (instance == null) {
			synchronized (multiInstance) {
				instance = multiInstance.get(appName);
				if (instance == null) {
					instance = new DynamicLog(appName);
					multiInstance.put(appName, instance);
				}
			}
		}
		return instance;
	}

	private String appName; //不同的appName订阅不同的埋点脚本
	private final Map<String, LogBuilder> buryPoints = new HashMap<String, LogBuilder>(0);

	public DynamicLog(String appName) {
		this.appName = appName;
		init();
	}

	public String build(String key, Object[] args) {
		return build(key, args, null);
	}

	public String build(String key, Object[] args, String defaultLog) {
		try {
			LogBuilder builer = buryPoints.get(key);
			if (builer == null) {
				return defaultLog;
			} else {
				return builer.build(args);
			}
		} catch (Exception e) {
			log.error("LogBuilder.build() failed for key:" + key, e);
			return e.getMessage();
		}
	}

	/* ========================================================================
	 * 动态订阅
	 * ======================================================================*/
	private static final String buryPointsDataId = ConfigServerHelper.DATA_ID_PREFIX + "{0}_buryPoints";
	private DataListener listener = new AbstractDataListener() {
		public void onDataReceive(Object data) {
			if (data != null) {
				updateBuryPoints((String) data);
			}
		}
	};

	private void init() {
		if (appName == null || "".equals(appName)) {
			log.warn("不指定appName则不订阅");
			return;
		}
		String dataId = new MessageFormat(buryPointsDataId).format(new Object[] { appName });
		Object first = ConfigServerHelper.subscribePersistentData(dataId, listener);
		if (first == null) {
			log.warn(dataId + "'s first data is null");
		}
	}

	@SuppressWarnings("unchecked")
	private void updateBuryPoints(String springXml) {
		StringXmlApplicationContext ctx = null;
		try {
			ctx = new StringXmlApplicationContext(springXml);
			Map<String, String> scripts = ctx.getBeansOfType(String.class);
			for (Map.Entry<String, String> e : scripts.entrySet()) {
				/*LogBuilder old = buryPoints.get(e.getKey());
				if (old != null) {
					((GroovyObject)old).getClass().getClassLoader().
				}*/
				buryPoints.put(e.getKey(), createBuilder(e.getValue()));
				log.warn("Set LogBuilder for buryPoint " + e.getKey() + ":" + e.getValue());
			}
		} catch (Exception e) {
			log.error("Set LogBuilder failed. springXml=" + springXml, e);
		} finally {
			if (ctx != null) {
				ctx.destroy();
				ctx = null;
			}
		}
	}

	private static LogBuilder createBuilder(String script) {
		GroovyClassLoader loader = new GroovyClassLoader(DynamicLog.class.getClassLoader());
		String groovyScript = completeGroovy(script);
		Class<?> c_groovy;
		try {
			c_groovy = loader.parseClass(groovyScript);
		} catch (CompilationFailedException e) {
			throw new IllegalArgumentException(groovyScript, e);
		}

		try {
			// 新建类实例
			return (LogBuilder) c_groovy.newInstance();
		} catch (Throwable t) {
			throw new IllegalArgumentException("实例化规则对象失败", t);
		}
	}

	private static String completeGroovy(String script) {
		StringBuffer sb = new StringBuffer();
		sb.append("public class GroovyLogBuilder implements com.taobao.tddl.common.DynamicLog.LogBuilder{");
		sb.append("public String build(Object[] args){");
		sb.append(script);
		sb.append("}}");
		return sb.toString();
	}

	/* ========================================================================
	 * log方法
	 * ======================================================================*/
	public void info(String key, Object[] args, String defaultLog, Log log) {
		String content = build(key, args, defaultLog);
		if (content != null && !"".equals(content)) {
			log.info(content);
		}
	}

	public void debug(String key, Object[] args, String defaultLog, Log log) {
		String content = build(key, args, defaultLog);
		if (content != null && !"".equals(content)) {
			log.debug(content);
		}
	}

	public void warn(String key, Object[] args, String defaultLog, Log log) {
		String content = build(key, args, defaultLog);
		if (content != null && !"".equals(content)) {
			log.warn(content);
		}
	}

	public void info(String key, Object[] args, String defaultLog, Logger log) {
		String content = build(key, args, defaultLog);
		if (content != null && !"".equals(content)) {
			log.info(content);
		}
	}

	public void debug(String key, Object[] args, String defaultLog, Logger log) {
		String content = build(key, args, defaultLog);
		if (content != null && !"".equals(content)) {
			log.debug(content);
		}
	}

	public void warn(String key, Object[] args, String defaultLog, Logger log) {
		String content = build(key, args, defaultLog);
		if (content != null && !"".equals(content)) {
			log.warn(content);
		}
	}
}
