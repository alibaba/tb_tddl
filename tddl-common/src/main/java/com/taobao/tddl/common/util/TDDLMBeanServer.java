/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.util;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dogun
 * 
 */
public final class TDDLMBeanServer {
	private static final Log log = LogFactory.getLog(TDDLMBeanServer.class);
	private static final String LogPrefix = "[TDDLMBeanServer]";

	private MBeanServer mbs = null;

	private ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>> idMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>();
	private ReentrantLock lock = new ReentrantLock();
	
	public static boolean shutDownMBean = true;

	private static class Holder {
		private static final TDDLMBeanServer instance = new TDDLMBeanServer();
	}

	// private static MyMBeanServer me = new MyMBeanServer();

	private TDDLMBeanServer() {
		// ´´½¨MBServer
		String hostName = null;
		try {
			InetAddress addr = InetAddress.getLocalHost();

			hostName = addr.getHostName();
		} catch (IOException e) {
			log.error(LogPrefix + "Get HostName Error", e);
			hostName = "localhost";
		}
		String host = System.getProperty("hostName", hostName);
		try {
			boolean useJmx = Boolean.parseBoolean(System.getProperty(
					"tddl.useJMX", "true"));
			if (useJmx) {
				mbs = ManagementFactory.getPlatformMBeanServer();
				int port = Integer.parseInt(System.getProperty("tddl.rmi.port",
						"6679"));
				String rmiName = System.getProperty("tddl.rmi.name",
						"tddlJmxServer");
				Registry reg = null;
				try {
					reg = LocateRegistry.getRegistry(port);
					reg.list();
				} catch (Exception e) {
					reg = null;
				}
				if (null == reg) {
					reg = LocateRegistry.createRegistry(port);
				}
				reg.list();
				String serverURL = "service:jmx:rmi:///jndi/rmi://" + host
						+ ":" + port + "/" + rmiName;
				JMXServiceURL url = new JMXServiceURL(serverURL);
				final JMXConnectorServer connectorServer = JMXConnectorServerFactory
						.newJMXConnectorServer(url, null, mbs);
				connectorServer.start();
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						try {
							System.err.println("JMXConnector stop");
							connectorServer.stop();
						} catch (IOException e) {
							log.error(LogPrefix + e);
						}
					}
				});
				log.warn(LogPrefix + "jmx url: " + serverURL);
			}
		} catch (Exception e) {
			log.error(LogPrefix + "create MBServer error", e);
		}
	}

	/*
	 * public static TDDLMBeanServer getInstance() { return Holder.instance; }
	 */
	public static void registerMBean(Object o, String name) {
		if (!shutDownMBean) {
			Holder.instance.registerMBean0(o, name);
		}
	}

	public static void registerMBeanWithId(Object o, String id) {
		if (!shutDownMBean) {
			Holder.instance.registerMBeanWithId0(o, id);
		}
	}

	public static void registerMBeanWithIdPrefix(Object o, String idPrefix) {
		if (!shutDownMBean) {
			Holder.instance.registerMBeanWithIdPrefix0(o, idPrefix);
		}
	}

	private void registerMBean0(Object o, String name) {
		// ×¢²áMBean
		if (null != mbs) {
			try {
				mbs.registerMBean(o, new ObjectName(o.getClass().getPackage()
						.getName()
						+ ":type="
						+ o.getClass().getSimpleName()
						+ (null == name ? (",id=" + o.hashCode()) : (",name="
								+ name + "-" + o.hashCode()))));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void registerMBeanWithId0(Object o, String id) {
		// ×¢²áMBean
		if (null == id || id.length() == 0) {
			throw new IllegalArgumentException("must set id");
		}
		if (null != mbs) {
			try {
				mbs.registerMBean(o,
						new ObjectName(o.getClass().getPackage().getName()
								+ ":type=" + o.getClass().getSimpleName()
								+ ",id=" + id));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private String getId(String name, String idPrefix) {
		ConcurrentHashMap<String, AtomicLong> subMap = idMap.get(name);
		if (null == subMap) {
			lock.lock();
			try {
				subMap = idMap.get(name);
				if (null == subMap) {
					subMap = new ConcurrentHashMap<String, AtomicLong>();
					idMap.put(name, subMap);
				}
			} finally {
				lock.unlock();
			}
		}

		AtomicLong indexValue = subMap.get(idPrefix);
		if (null == indexValue) {
			lock.lock();
			try {
				indexValue = subMap.get(idPrefix);
				if (null == indexValue) {
					indexValue = new AtomicLong(0);
					subMap.put(idPrefix, indexValue);
				}
			} finally {
				lock.unlock();
			}
		}
		long value = indexValue.incrementAndGet();
		String result = idPrefix + "-" + value;
		return result;
	}

	private void registerMBeanWithIdPrefix0(Object o, String idPrefix) {
		// ×¢²áMBean
		if (null != mbs) {
			if (null == idPrefix || idPrefix.length() == 0) {
				idPrefix = "default";
			}
			idPrefix = idPrefix.replace(":", "-");

			try {
				String id = this.getId(o.getClass().getName(), idPrefix);

				mbs.registerMBean(o,
						new ObjectName(o.getClass().getPackage().getName()
								+ ":type=" + o.getClass().getSimpleName()
								+ ",id=" + id));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
