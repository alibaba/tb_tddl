/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.taobao.tddl.common.DataSourceChangeListener;
import com.taobao.tddl.common.TDDLConstant;
import com.taobao.tddl.common.util.DataSourceFetcher;
import com.taobao.tddl.common.util.TDDLMBeanServer;
import com.taobao.tddl.interact.rule.bean.DBType;
import com.taobao.tddl.jdbc.group.config.ConfigManager;
import com.taobao.tddl.jdbc.group.dbselector.DBSelector;
import com.taobao.tddl.jdbc.group.exception.TGroupDataSourceException;

/**
 * TGroupDataSource并不是像名字所暗示的那样有一组DataSource，
 * 而是指TGroupDataSource内部包含一组(>=1个)同构的数据库，
 * 这一组数据库的不同个体有不同的读写优先级和权重，
 * 当读写数据时也只是按读写优先级和权重对其中的一个数据库操作，
 * 如果第一个数据库读写失败了，再尝试下一个数据库，
 * 如果第一个数据库读写成功了，直接返回结果给应用层，
 * 其他数据库的同步更新由底层数据库内部完成，
 * TGroupDataSource不负责数据同步。
 *
 * 使用TGroupDataSource的步骤:
 * <pre>
 *      TGroupDataSource tGroupDataSource = new TGroupDataSource();
 *      tGroupDataSource.setDbGroupKey("myDbGroup");
 *      //......调用其他setter
 *      tGroupDataSource.init();
 *      tGroupDataSource.getConnection();
 * </pre>
 *
 *
 * @author yangzhu
 * @author linxuan
 *
 */
public class TGroupDataSource implements DataSource {
	private ConfigManager configManager;

	/**
	 * 下面三个为一组，支持本地配置
	 */
	private String dsKeyAndWeightCommaArray;
	private DataSourceFetcher dataSourceFetcher;
	private DBType dbType = DBType.MYSQL;

	public TGroupDataSource() {
	}

	public TGroupDataSource(String dbGroupKey, String appName) {
		this.dbGroupKey = dbGroupKey;
		this.appName = appName;
	}

	/**
	 * 基于dbGroupKey、appName来初始化多个TAtomDataSource
	 *
	 * @throws com.taobao.tddl.jdbc.group.exception.ConfigException
	 */
	public void init() {
		if (dsKeyAndWeightCommaArray != null) {
			//本地配置方式：dsKeyAndWeightCommaArray + dataSourceFetcher + dyType
			DataSourceFetcher wrapper = new DataSourceFetcher() {
				@Override
				public DataSource getDataSource(String key) {
					return dataSourceFetcher.getDataSource(key);
				}

				@Override
				public DBType getDataSourceDBType(String key) {
					DBType type = dataSourceFetcher.getDataSourceDBType(key);
					return type == null ? dbType : type; //如果dataSourceFetcher没dbType，用tgds的dbType
				}
			};
			List<DataSourceWrapper> dss = ConfigManager.buildDataSourceWrapper(dsKeyAndWeightCommaArray, wrapper);
			init(dss);
		} else {
			checkProperties();
			configManager = new ConfigManager(this);
			configManager.init();
		}
	}

	public void init(DataSourceWrapper... dataSourceWrappers) {
		init(Arrays.asList(dataSourceWrappers));
	}

	public void init(List<DataSourceWrapper> dataSourceWrappers) {
		configManager = new ConfigManager(this);
		configManager.init(dataSourceWrappers);
	}

	public static TGroupDataSource build(String groupKey, String dsWeights, DataSourceFetcher fetcher) {
		List<DataSourceWrapper> dss = ConfigManager.buildDataSourceWrapper(dsWeights, fetcher);
		TGroupDataSource tGroupDataSource = new TGroupDataSource();
		tGroupDataSource.setDbGroupKey(groupKey);
		tGroupDataSource.init(dss);
		return tGroupDataSource;
	}

	/**
	 * 如果构造的是TAtomDataSource，必须检查dbGroupKey、appName两个属性的值是否合法
	 */
	private void checkProperties() {
		if (dbGroupKey == null)
			throw new TGroupDataSourceException("dbGroupKey不能为null");
		dbGroupKey = dbGroupKey.trim();
		if (dbGroupKey.length() < 1)
			throw new TGroupDataSourceException("dbGroupKey的长度要大于0，前导空白和尾部空白不算在内");

		if (appName == null)
			throw new TGroupDataSourceException("appName不能为null");
		appName = appName.trim();
		if (appName.length() < 1)
			throw new TGroupDataSourceException("appName的长度要大于0，前导空白和尾部空白不算在内");
	}

	/**
	 * 危险接口。一般用于测试。应用也可以直接通过该接口重置数据源配置
	 */
	public void resetDbGroup(String configInfo) {
		configManager.resetDbGroup(configInfo);
	}

	//包访问级别，调用者不能缓存，否则会失去动态性
	DBSelector getDBSelector(boolean isRead) {
		return configManager.getDBSelector(isRead, this.autoSelectWriteDataSource);
	}

	/* ========================================================================
	 * 以下是保留当前写操作是在哪个库上执行的, 满足类似日志库插入的场景
	 * ======================================================================*/
	private static ThreadLocal<DataSourceWrapper> targetThreadLocal;

	/**
	 * 通过spring注入或直接调用该方法开启、关闭目标库记录
	 */
	public void setTracerWriteTarget(boolean isTraceTarget) {
		if (isTraceTarget) {
			if (targetThreadLocal == null) {
				targetThreadLocal = new ThreadLocal<DataSourceWrapper>();
			}
		} else {
			targetThreadLocal = null;
		}
	}

	/**
	 * 在执行完写操作后，调用改方法获得当前线程写操作是在哪个数据源执行的
	 * 获取完自动立即清空
	 */
	public DataSourceWrapper getCurrentTarget() {
		if (targetThreadLocal == null) {
			return null;
		}
		DataSourceWrapper dsw = targetThreadLocal.get();
		targetThreadLocal.remove();
		return dsw;
	}

	/**
	 * 下游调用该方法设置目标库
	 */
	void setWriteTarget(DataSourceWrapper dsw) {
		if (targetThreadLocal != null) {
			targetThreadLocal.set(dsw);
		}
	}

	/* ========================================================================
	 * 遍历需求API
	 * ======================================================================*/
//在ConfigManager中我们将配置信息最终封装为读写DBSelector，要得到从dbKey到DataSource的映射，将DBSelector中的信息方向输出。
	public Map<String, DataSource> getDataSourceMap() {
		Map<String, DataSource> dsMap = new LinkedHashMap<String, DataSource>();
		dsMap.putAll(this.getDBSelector(true).getDataSources());
		dsMap.putAll(this.getDBSelector(false).getDataSources());
		return dsMap;
	}

	public Map<String, DataSource> getDataSourcesMap(boolean isRead) {
		return this.getDBSelector(isRead).getDataSources();
	}

	public void setDataSourceChangeListener(DataSourceChangeListener dataSourceChangeListener) {
		this.configManager.setDataSourceChangeListener(dataSourceChangeListener);
	}

	/* ========================================================================
	 * 以下是javax.sql.DataSource的API实现
	 * ======================================================================*/

	public TGroupConnection getConnection() throws SQLException {
		return new TGroupConnection(this);
	}

	public TGroupConnection getConnection(String username, String password) throws SQLException {
		return new TGroupConnection(this, username, password);
	}

	//下面两个字段当建立实际的DataSource时必须传递过去

	//jdbc规范: DataSource刚建立时LogWriter为null
	private PrintWriter out = null;

	public PrintWriter getLogWriter() throws SQLException {
		return out;
	}

	public void setLogWriter(PrintWriter out) throws SQLException {
		this.out = out;
	}

	//jdbc规范: DataSource刚建立时LoginTimeout为0
	private int seconds = 0;

	public int getLoginTimeout() throws SQLException {
		return seconds;
	}

	public void setLoginTimeout(int seconds) throws SQLException {
		this.seconds = seconds;
	}

	public static void setShutDownMBean(boolean shutDownMBean) {
		TDDLMBeanServer.shutDownMBean=shutDownMBean;
	}

	////////////////////////////////////////////////////////////////////////////
	/**
	 * 无逻辑的getter/setter
	 */

	private String appName;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	private String dbGroupKey;

	public String getDbGroupKey() {
		return dbGroupKey;
	}

	private String fullDbGroupKey = null;

	public String getFullDbGroupKey() {
		if (fullDbGroupKey == null)
			fullDbGroupKey = PREFIX + getDbGroupKey();
		return fullDbGroupKey;
	}

	public String getDbGroupExtraConfigKey(){
		return EXTRA_PREFIX+getDbGroupKey()+"."+getAppName();
	}

	public void setDbGroupKey(String dbGroupKey) {
		this.dbGroupKey = dbGroupKey;
	}

	private int retryingTimes = 3; //默认读写失败时重试3次

	public int getRetryingTimes() {
		return retryingTimes;
	}

	public void setRetryingTimes(int retryingTimes) {
		this.retryingTimes = retryingTimes;
	}

	private long configReceiveTimeout = TDDLConstant.DIAMOND_GET_DATA_TIMEOUT; //取配置信息的默认超时时间为30秒

	public long getConfigReceiveTimeout() {
		return configReceiveTimeout;
	}

	public void setConfigReceiveTimeout(long configReceiveTimeout) {
		this.configReceiveTimeout = configReceiveTimeout;
	}

	public void setDsKeyAndWeightCommaArray(String dsKeyAndWeightCommaArray) {
		this.dsKeyAndWeightCommaArray = dsKeyAndWeightCommaArray;
	}

	//当运行期间主备发生切换时是否需要查找第一个可写的库
	private boolean autoSelectWriteDataSource = false;

	public boolean getAutoSelectWriteDataSource() {
		return autoSelectWriteDataSource;
	}

	public void setAutoSelectWriteDataSource(boolean autoSelectWriteDataSource) {
		this.autoSelectWriteDataSource = autoSelectWriteDataSource;
	}

	public void setDataSourceFetcher(DataSourceFetcher dataSourceFetcher) {
		this.dataSourceFetcher = dataSourceFetcher;
	}

	public void setDbType(DBType dbType) {
		this.dbType = dbType;
	}

	private static String VERSION = "2.4.1";
	private static String PREFIX = "com.taobao.tddl.jdbc.group_V" + VERSION + "_";
	private static String EXTRA_PREFIX = "com.taobao.tddl.jdbc.extra_config.group_V" + VERSION + "_";

	public static String getFullDbGroupKey(String dbGroupKey) {
		return PREFIX + dbGroupKey;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return this.getClass().isAssignableFrom(iface);
	}

	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		try {
			return (T) this;
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}
}
