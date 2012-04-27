/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.dbselector;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.tddl.client.jdbc.sorter.ExceptionSorter;
import com.taobao.tddl.client.jdbc.sorter.MySQLExceptionSorter;
import com.taobao.tddl.client.jdbc.sorter.OracleExceptionSorter;
import com.taobao.tddl.common.SQLPreParser;
import com.taobao.tddl.common.util.NagiosUtils;
import com.taobao.tddl.common.util.TStringUtil;
import com.taobao.tddl.interact.rule.bean.DBType;
import com.taobao.tddl.jdbc.group.DataSourceWrapper;
import com.taobao.tddl.jdbc.group.config.GroupExtraConfig;
import com.taobao.tddl.jdbc.group.exception.SqlForbidException;
import com.taobao.tddl.jdbc.group.util.StringUtils;

/**
 * @author linxuan
 * @author yangzhu
 *
 */
public abstract class AbstractDBSelector implements DBSelector {

	private static final Log logger = LogFactory
			.getLog(AbstractDBSelector.class);
	private static final Map<DBType, ExceptionSorter> exceptionSorters = new HashMap<DBType, ExceptionSorter>(
			2);
	static {
		exceptionSorters.put(DBType.ORACLE, new OracleExceptionSorter());
		exceptionSorters.put(DBType.MYSQL, new MySQLExceptionSorter());
	}
	private DBType dbType = DBType.MYSQL;
	protected ExceptionSorter exceptionSorter = exceptionSorters.get(dbType);
	private String id = "undefined"; // id值未使用

	private static final int default_retryBadDbInterval = 2000; // milliseconds
	protected static int retryBadDbInterval; // milliseconds
	static {
		int interval = default_retryBadDbInterval;
		String propvalue = System
				.getProperty("com.taobao.tddl.DBSelector.retryBadDbInterval");
		if (propvalue != null) {
			try {
				interval = Integer.valueOf(propvalue.trim());
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		retryBadDbInterval = interval;
	}

	protected boolean readable = false;

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

	protected boolean isSupportRetry = true; // 默认情况下支持重试

	public boolean isSupportRetry() {
		return isSupportRetry;
	}

	public void setSupportRetry(boolean isSupportRetry) {
		this.isSupportRetry = isSupportRetry;
	}

	public AbstractDBSelector() {
	}

	public AbstractDBSelector(String id) {
		this.id = id;
	}

	protected static class DataSourceHolder {
		public final DataSourceWrapper dsw;
		public final ReentrantLock lock = new ReentrantLock();
		public volatile boolean isNotAvailable = false;
		public volatile long lastRetryTime = 0;

		public DataSourceHolder(DataSourceWrapper dsw) {
			this.dsw = dsw;
		}
	}

	protected <T> T tryOnDataSourceHolder(DataSourceHolder dsHolder,
			Map<DataSource, SQLException> failedDataSources,
			DataSourceTryer<T> tryer, int times, Object... args)
			throws SQLException {
		List<SQLException> exceptions = new LinkedList<SQLException>();
		if (failedDataSources != null) {
			exceptions.addAll(failedDataSources.values());
		}
		if (failedDataSources != null
				&& failedDataSources.containsKey(dsHolder.dsw)) {
			return tryer.onSQLException(exceptions, exceptionSorter, args);
		}

		try {
			if (dsHolder.isNotAvailable) {
				boolean toTry = System.currentTimeMillis()
						- dsHolder.lastRetryTime > retryBadDbInterval;
				if (toTry && dsHolder.lock.tryLock()) {
					try {
						T t = tryer.tryOnDataSource(dsHolder.dsw, args); // 同一个时间只会有一个线程继续使用这个数据源。
						dsHolder.isNotAvailable = false; // 用一个线程重试，执行成功则标记为可用，自动恢复
						return t;
					} finally {
						dsHolder.lastRetryTime = System.currentTimeMillis();
						dsHolder.lock.unlock();
					}
				} else {
					exceptions.add(new NoMoreDataSourceException("dsKey:"
							+ dsHolder.dsw.getDataSourceKey()
							+ " not Available,toTry:" + toTry));
					return tryer.onSQLException(exceptions, exceptionSorter,
							args);
				}
			} else {
				return tryer.tryOnDataSource(dsHolder.dsw, args); // 有一次成功直接返回
			}
		} catch (SQLException e) {
			if (exceptionSorter.isExceptionFatal(e)) {
				NagiosUtils.addNagiosLog(NagiosUtils.KEY_DB_NOT_AVAILABLE + "|"
						+ dsHolder.dsw.getDataSourceKey(), e.getMessage());
				dsHolder.isNotAvailable = true;
			}
			exceptions.add(e);
			return tryer.onSQLException(exceptions, exceptionSorter, args);
		}
	}

	protected GroupExtraConfig groupExtraConfig ;

	public <T> T tryExecute(Map<DataSource, SQLException> failedDataSources,
			DataSourceTryer<T> tryer, int times, Object... args)
			throws SQLException {
		// dataSourceIndex放在args最后一个.以后改动要注意
		// local set dataSourceIndex was placed first
		Integer dataSourceIndex = null;
		if (args != null && args.length > 0) {
			dataSourceIndex = (Integer) args[args.length - 1];
		}


		if (groupExtraConfig != null) {
			Boolean defaultMain = groupExtraConfig.isDefaultMain();
			Map<String, Integer> tableDsIndexMap = groupExtraConfig
					.getTableDsIndexMap();
			Map<String, Integer> sqlDsIndexMap = groupExtraConfig
					.getSqlDsIndexMap();
			Set<String> sqlForbidSet = groupExtraConfig.getSqlForbidSet();

			// 1.when batch ,args have no sql parameter,so,should check
			// the args
			// 2.table dataSourceIndex relation have 2th priority
			// 3.sql dataSourceIndex relation have 3th priority
			if (args != null && args.length > 0 && args[0] instanceof String) {
				if (sqlForbidSet != null && sqlForbidSet.size() > 0) {
					String sql = (String) args[0];
					String nomalSql = TStringUtil.fillTabWithSpace(sql);
					boolean isForbidden = false;
					if (sqlForbidSet.contains(nomalSql)) {
						isForbidden = true;
					}
					if (!isForbidden) {
						String actualTable = SQLPreParser
								.findTableName(nomalSql);
						for (String configSql : sqlForbidSet) {
							String nomalConfigSql = TStringUtil
									.fillTabWithSpace(configSql);
							String actualConfigTable = SQLPreParser
									.findTableName(nomalConfigSql);
							if (StringUtils.isTableFatherAndSon(
									actualConfigTable, actualTable)) {
								nomalConfigSql = nomalConfigSql.replaceAll(
										actualConfigTable, actualTable);
							}
							if (nomalConfigSql.equals(nomalSql)) {
								isForbidden = true;
								break;
							}
						}
					}
					if (isForbidden) {
						String message = "sql : '" + sql
								+ "' is in forbidden set.";
						logger.error(message);
						throw new SqlForbidException(message);
					}
				}

				if (tableDsIndexMap != null
						&& tableDsIndexMap.size() > 0
						&& (dataSourceIndex == null || dataSourceIndex == NOT_EXIST_USER_SPECIFIED_INDEX)) {
					String sql = (String) args[0];
					String actualTable = SQLPreParser.findTableName(sql);
					dataSourceIndex = tableDsIndexMap.get(actualTable);
					if (dataSourceIndex == null
							|| dataSourceIndex == NOT_EXIST_USER_SPECIFIED_INDEX) {
						Set<String> tableSet = tableDsIndexMap.keySet();
						for (String configTable : tableSet) {
							if (StringUtils.isTableFatherAndSon(configTable,
									actualTable)) {
								dataSourceIndex = tableDsIndexMap
										.get(configTable);
								break;
							}
						}
					}
				}

				if (sqlDsIndexMap != null
						&& sqlDsIndexMap.size() > 0
						&& (dataSourceIndex == null || dataSourceIndex == NOT_EXIST_USER_SPECIFIED_INDEX)) {
					String sql = ((String) args[0]).toLowerCase();
					String nomalSql = TStringUtil.fillTabWithSpace(sql);
					dataSourceIndex = sqlDsIndexMap.get(nomalSql);
					if (dataSourceIndex == null
							|| dataSourceIndex == NOT_EXIST_USER_SPECIFIED_INDEX) {
						String actualTable = SQLPreParser
								.findTableName(nomalSql);
						Set<String> sqlSet = sqlDsIndexMap.keySet();
						for (String configSql : sqlSet) {
							String nomalConfigSql = TStringUtil
									.fillTabWithSpace(configSql);
							String actualConfigTable = SQLPreParser
									.findTableName(nomalConfigSql);
							if (StringUtils.isTableFatherAndSon(
									actualConfigTable, actualTable)) {
								nomalConfigSql = nomalConfigSql.replaceAll(
										actualConfigTable, actualTable);
							}
							if (nomalConfigSql.equals(nomalSql)) {
								dataSourceIndex = sqlDsIndexMap.get(configSql);
								break;
							}
						}
					}
				}
			}

			// 1.this case simple handled,just set dataSourceIndex=0
			// 2.default main have 4th priority
			if ((dataSourceIndex == null || dataSourceIndex == NOT_EXIST_USER_SPECIFIED_INDEX)
					&& defaultMain) {
				dataSourceIndex = 0;
			}
		}

		// 如果业务层直接指定了一个数据源，就直接在指定的数据源上进行查询更新操作，失败时不再重试。
		if (dataSourceIndex != null
				&& dataSourceIndex != NOT_EXIST_USER_SPECIFIED_INDEX) {
			DataSourceHolder dsHolder = findDataSourceWrapperByIndex(dataSourceIndex);
			if (dsHolder == null) {
				throw new IllegalArgumentException("找不到索引编号为 '"
						+ dataSourceIndex + "'的数据源");
			}
			return tryOnDataSourceHolder(dsHolder, failedDataSources, tryer,
					times, args);
		} else {
			return tryExecuteInternal(failedDataSources, tryer, times, args);
		}
	}

	public <T> T tryExecute(DataSourceTryer<T> tryer, int times, Object... args)
			throws SQLException {
		return this.tryExecute(new LinkedHashMap<DataSource, SQLException>(0),
				tryer, times, args);
	}

	public DBType getDbType() {
		return dbType;
	}

	public void setDbType(DBType dbType) {
		this.dbType = dbType;
		this.exceptionSorter = exceptionSorters.get(this.dbType);
	}

	public final void setExceptionSorter(ExceptionSorter exceptionSorter) {
		// add by shenxun:主要还是方便测试。。构造整个dbSelector结构太复杂
		this.exceptionSorter = exceptionSorter;
	}

	public String getId() {
		return id;
	}

	// public abstract DataSource findDataSourceByIndex(int dataSourceIndex);

	protected abstract DataSourceHolder findDataSourceWrapperByIndex(
			int dataSourceIndex);

	protected <T> T tryExecuteInternal(DataSourceTryer<T> tryer, int times,
			Object... args) throws SQLException {
		return this.tryExecuteInternal(
				new LinkedHashMap<DataSource, SQLException>(0), tryer, times,
				args);
	}

	protected abstract <T> T tryExecuteInternal(
			Map<DataSource, SQLException> failedDataSources,
			DataSourceTryer<T> tryer, int times, Object... args)
			throws SQLException;
}
