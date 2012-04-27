/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import net.sf.json.JSONArray;import net.sf.json.JSONException;import net.sf.json.JSONObject;import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.tddl.common.DataSourceChangeListener;
import com.taobao.tddl.common.config.ConfigDataHandler;
import com.taobao.tddl.common.config.ConfigDataHandlerFactory;
import com.taobao.tddl.common.config.ConfigDataListener;
import com.taobao.tddl.common.config.impl.DefaultConfigDataHandlerFactory;
import com.taobao.tddl.common.util.DataSourceFetcher;
import com.taobao.tddl.common.util.TStringUtil;
import com.taobao.tddl.interact.rule.bean.DBType;
import com.taobao.tddl.jdbc.atom.TAtomDataSource;
import com.taobao.tddl.jdbc.atom.config.object.AtomDbStatusEnum;
import com.taobao.tddl.jdbc.group.DataSourceWrapper;
import com.taobao.tddl.jdbc.group.TGroupDataSource;
import com.taobao.tddl.jdbc.group.dbselector.AbstractDBSelector;
import com.taobao.tddl.jdbc.group.dbselector.DBSelector;
import com.taobao.tddl.jdbc.group.dbselector.EquityDbManager;
import com.taobao.tddl.jdbc.group.dbselector.OneDBSelector;
import com.taobao.tddl.jdbc.group.dbselector.PriorityDbGroupSelector;
import com.taobao.tddl.jdbc.group.dbselector.RuntimeWritableAtomDBSelector;
import com.taobao.tddl.jdbc.group.exception.ConfigException;
import com.taobao.tddl.jdbc.group.exception.TAtomDataSourceException;

/**
 * 一个ConfigManager对应一个TGroupDataSource，
 * 主要用于将根据Group的dataID取得的对应配置字符串信（比如db0:rwp1q1i0, db1:rwp0q0i1），
 * 转化为真正的Group层的配置体系结构：一个Group层挂着两个Atom db0 与 db1 ， 则我们使用一个 Map<String,
 * DataSourceWrapper> 来表示 其中的String 为每个Atom DS 的dbKey ，DataSourceWrapper
 * 为经过封装的TAtomDataSource
 * ---这里需要解释一下，为什么不直接使用AtomDataSource？因为每个AtomDataSource还有相应的权重和优先级信息 因此，需要***方法
 *
 *
 * 其中，配置的每一个Atom DataSource也只是用Atom
 * 的dbKey表示，因此，我们还需要根据此dbKey取得Atom的配置信息，并且将它封装成一个AtomDataSource对象。 因此需要***方法
 *
 * 有了这个map能根据dbKey迅速的找到对应的Datasource也是不够的，我们的Group层应该是对应用透明的，
 * 因此，当我们的读写请求进来时，Group层应该能够根据配置的权重和优先级，自动的选择一个合适的DB上进行读写，
 * 所以，我们还需要将配置信息生成一个DBSelector来自动的完成根据权重、优先级选择合适的目标库 因此，需要***方法
 *
 *
 *
 * @author yangzhu
 * @author linxuan refactor
 *
 */
public class ConfigManager {
	private static final Log logger = LogFactory.getLog(ConfigManager.class);

	private final ConfigDataListener configReceiver; // //动态接收Diamond推送过来的信息
	private ConfigDataHandlerFactory configFactory;
	private ConfigDataHandler globalHandler;

	//add by junyu
	private final ConfigDataListener extraGroupConfigReceiver;
	private ConfigDataHandler extraHandler;
	private ConfigDataHandlerFactory extraFactory;

	private final TGroupDataSource tGroupDataSource;

	private boolean createTAtomDataSource = true;

	private Map<String/* Atom dbIndex */, DataSourceWrapper/* Wrapper过的Atom DS */> dataSourceWrapperMap = new HashMap<String, DataSourceWrapper>();

	private volatile GroupExtraConfig groupExtraConfig = new GroupExtraConfig();

	public ConfigManager(TGroupDataSource tGroupDataSource) {
		this.tGroupDataSource = tGroupDataSource;
		this.configReceiver = new ConfigReceiver();
		this.extraGroupConfigReceiver=new ExtraGroupConfigReceiver();
	}

	/**
	 * 从Diamond配置中心提取信息，构造TAtomDataSource、构造有优先级信息的读写DBSelector ---add by
	 * mazhidan.pt
	 */
	public void init() {
		// 警告: 不要在构造DefaultDiamondManager时就注册ManagerListener(比如:configReceiver)
		// 也就是说，不要这样用: new DefaultDiamondManager(dbGroupKey, configReceiver)，
		// 而是要设成null，等第一次取得信息并解析完成后再注册，这样可以不用同步，避免任何与并发相关的问题，
		// 因为有可能在第一次刚取回信息后，Diamond配置中心那边马上修改了记录，导致ManagerListener这个线程立刻收到信息，
		// 造成初始化线程和ManagerListener线程同时解析信息。
		configFactory = new DefaultConfigDataHandlerFactory();
		globalHandler = configFactory.getConfigDataHandler(
				tGroupDataSource.getFullDbGroupKey(), null);

		String dsWeightCommaStr = globalHandler.getData(
				tGroupDataSource.getConfigReceiveTimeout(),
				ConfigDataHandler.FIRST_CACHE_THEN_SERVER_STRATEGY);

		//extra config
		extraFactory=new DefaultConfigDataHandlerFactory();
		extraHandler=extraFactory.getConfigDataHandler(
				tGroupDataSource.getDbGroupExtraConfigKey(), null);
		String extraConfig=extraHandler.getData(tGroupDataSource.getConfigReceiveTimeout(),
				ConfigDataHandler.FIRST_CACHE_THEN_SERVER_STRATEGY);

		if(extraConfig!=null){
			parseExtraConfig(extraConfig);
			extraHandler.addListener(extraGroupConfigReceiver, null);
		}

		List<DataSourceWrapper> dswList = parse2DataSourceWrapperList(dsWeightCommaStr);
		resetByDataSourceWrapper(dswList);
		globalHandler.addListener(configReceiver, null);
	}

	/**
	 * 根据普通的DataSource构造读写DBSelector
	 */
	public void init(List<DataSourceWrapper> dataSourceWrappers) {
		if ((dataSourceWrappers == null) || dataSourceWrappers.size() < 1) {
			throw new ConfigException("dataSourceWrappers不能为null且长度要大于0");
		}
		createTAtomDataSource = false;
		// update(createDBSelectors2(dataSourceWrappers));
		resetByDataSourceWrapper(dataSourceWrappers);
	}

	private class MyDataSourceFetcher implements DataSourceFetcher {
		private DBType dbType = DBType.MYSQL;

		@Override
		public DataSource getDataSource(String dsKey) {
			DataSourceWrapper dsw = dataSourceWrapperMap.get(dsKey);
			if (dsw != null) {
				dbType = dsw.getDBType();
				return dsw.getWrappedDataSource();
			} else {
				if (createTAtomDataSource) {
					TAtomDataSource atom = createTAtomDataSource(dsKey);
					dbType = DBType.valueOf(atom.getDbType().name());
					return atom;
				} else {
					throw new IllegalArgumentException(dsKey + " not exist!");
				}
			}
		}

		@Override
		public DBType getDataSourceDBType(String key) {
			return dbType;
		}
	};

	// configInfo样例: db1:rw, db2:r, db3:r
	private void parse(String dsWeightCommaStr) {
		List<DataSourceWrapper> dswList = parse2DataSourceWrapperList(dsWeightCommaStr);
		resetByDataSourceWrapper(dswList);
	}

	/**
	 * extraConfig is a json format string,include table dataSourceIndex
	 * relation or sql dataSourceIndex relation or default go main db config.
	 * example: {sqlDsIndex: { 0:[sql1,sql2,sql3], 1:[sql0], 2:[sql4]
	 * }, tabDsIndex: { 0:[table1,table2] 1:[table3,table4] },
	 * defaultMain:true}
	 *
	 * @throws JSONException
	 *
	 **/
	@SuppressWarnings("rawtypes")	private void parseExtraConfig(String extraConfig) {
		if(extraConfig==null){
			this.groupExtraConfig.getSqlForbidSet().clear();
			this.groupExtraConfig.getSqlDsIndexMap().clear();
			this.groupExtraConfig.getTableDsIndexMap().clear();
			this.groupExtraConfig.setDefaultMain(false);
		}
		try {
			JSONObject obj = JSONObject.fromObject(extraConfig);
			if(obj.has("sqlForbid")) {
				Set<String> tempSqlForbidSet = new HashSet<String>();
				JSONArray array = obj.getJSONArray("sqlForbid");
				for(int i=0; i<array.size(); i++) {
					String sql=array.getString(i);
					String nomalSql = TStringUtil.fillTabWithSpace(sql
							.trim().toLowerCase());
					if(nomalSql != null && !nomalSql.trim().isEmpty()){
						tempSqlForbidSet.add(nomalSql);
					}
				}
				this.groupExtraConfig.setSqlForbidSet(tempSqlForbidSet);
			}
			else {
				this.groupExtraConfig.getSqlForbidSet().clear();
			}

			if (obj.has("sqlDsIndex")) {
				Map<String, Integer> tempSqlDsIndexMap = new HashMap<String, Integer>();
				JSONObject sqlDsIndex = obj.getJSONObject("sqlDsIndex");
				Iterator it=sqlDsIndex.keys();
				while(it.hasNext()){
					String key=String.valueOf(it.next()).trim();
					Integer index=Integer.valueOf(key);
					JSONArray array=sqlDsIndex.getJSONArray(key);
					for (int i=0;i<array.size();i++) {
						String sql=array.getString(i);
						String nomalSql = TStringUtil.fillTabWithSpace(sql
								.trim().toLowerCase());
						if (tempSqlDsIndexMap.get(nomalSql) == null) {
							tempSqlDsIndexMap.put(nomalSql,index);
						} else {
							// have a nice log
							throw new ConfigException(
									"sql can not be route to different dataSourceIndex:"
											+ sql);
						}
					}
				}
				this.groupExtraConfig.setSqlDsIndexMap(tempSqlDsIndexMap);
			}
			else {
				this.groupExtraConfig.getSqlDsIndexMap().clear();
			}

			if (obj.has("tabDsIndex")) {
				Map<String, Integer> tempTabDsIndexMap = new HashMap<String, Integer>();
				JSONObject sqlDsIndex = obj.getJSONObject("tabDsIndex");
				Iterator it=sqlDsIndex.keys();
				while(it.hasNext()){
					String key=String.valueOf(it.next()).trim();
					Integer index=Integer.valueOf(key);
					JSONArray array=sqlDsIndex.getJSONArray(key);
					for (int i=0;i<array.size();i++) {
						String table=array.getString(i);
						String nomalTable = table.trim().toLowerCase();
						if (tempTabDsIndexMap.get(nomalTable) == null) {
							tempTabDsIndexMap.put(nomalTable, index);
						} else {
							// have a nice log
							throw new ConfigException(
									"table can not be route to different dataSourceIndex:"
											+ table);
						}
					}
				}
				this.groupExtraConfig.setTableDsIndexMap(tempTabDsIndexMap);
			}
			else {
				this.groupExtraConfig.getTableDsIndexMap().clear();
			}

			if (obj.has("defaultMain")) {
				this.groupExtraConfig.setDefaultMain(obj.getBoolean("defaultMain"));
			}
			else {
				this.groupExtraConfig.setDefaultMain(false);
			}

		} catch (JSONException e) {
			throw new ConfigException(
					"group extraConfig is not json valid string:" + extraConfig,
					e);
		}
	}

	/**
	 * 警告: 逗号的位置很重要，要是有连续的两个逗号也不要人为的省略掉， 数据库的个数 =
	 * 逗号的个数+1，用0、1、2...编号，比如"db1,,db3"，实际上有3个数据库，
	 * 业务层通过传一个ThreadLocal进来，ThreadLocal中就是这种索引编号。
	 */
	private List<DataSourceWrapper> parse2DataSourceWrapperList(
			String dsWeightCommaStr) {
		logger.info("[parse2DataSourceWrapperList]dsWeightCommaStr="
				+ dsWeightCommaStr);
		if ((dsWeightCommaStr == null)
				|| (dsWeightCommaStr = dsWeightCommaStr.trim()).length() == 0) {
			throw new ConfigException("与dbGroupKey:'"
					+ tGroupDataSource.getFullDbGroupKey()
					+ "'对应的配置信息不能为null且长度要大于0");
		}
		return buildDataSourceWrapper(dsWeightCommaStr,
				new MyDataSourceFetcher());
	}

	/**
	 * 将封装好的AtomDataSource的列表，进一步封装为可以根据权重优先级随机选择模板库的DBSelector ---add by
	 * mazhidan.pt
	 *
	 * @param dswList
	 */
	private void resetByDataSourceWrapper(List<DataSourceWrapper> dswList) {
		// 删掉已经不存在的DataSourceWrapper
		Map<String, DataSourceWrapper> newDataSourceWrapperMap = new HashMap<String, DataSourceWrapper>(
				dswList.size());
		for (DataSourceWrapper dsw : dswList) {
			newDataSourceWrapperMap.put(dsw.getDataSourceKey(), dsw);
		}
		Map<String, DataSourceWrapper> old = this.dataSourceWrapperMap;
		this.dataSourceWrapperMap = newDataSourceWrapperMap;
		old.clear();
		old = null;

		DBSelector r_DBSelector = null;
		DBSelector w_DBSelector = null;

		// 如果只有一个db，则用OneDBSelector
		if (dswList.size() == 1) {
			DataSourceWrapper dsw2 = dswList.get(0);
			r_DBSelector = new OneDBSelector(dsw2);
			r_DBSelector.setDbType(dsw2.getDBType());
			w_DBSelector = r_DBSelector;
		} else {
			// 读写优先级Map
			Map<Integer/* 优先级 */, List<DataSourceWrapper>/* 优先级为key的DS 列表 */> rPriority2DswList = new HashMap<Integer, List<DataSourceWrapper>>();
			Map<Integer, List<DataSourceWrapper>> wPriority2DswList = new HashMap<Integer, List<DataSourceWrapper>>();
			for (DataSourceWrapper dsw1 : dswList) {
				add2LinkedListMap(rPriority2DswList, dsw1.getWeight().p, dsw1);
				add2LinkedListMap(wPriority2DswList, dsw1.getWeight().q, dsw1);
			}
			r_DBSelector = createDBSelector(rPriority2DswList, true);
			w_DBSelector = createDBSelector(wPriority2DswList, false);
		}

		r_DBSelector.setReadable(true);
		w_DBSelector.setReadable(false);

		this.readDBSelectorWrapper = r_DBSelector;
		this.writeDBSelectorWrapper = w_DBSelector;

		if (tGroupDataSource.getAutoSelectWriteDataSource())
			runtimeWritableAtomDBSelectorWrapper = new RuntimeWritableAtomDBSelector(
					dataSourceWrapperMap, groupExtraConfig);

		// System.out.println("dataSourceWrapperMap=" + dataSourceWrapperMap);
		if (this.dataSourceChangeListener != null) {
			dataSourceChangeListener.onDataSourceChanged(null);// 业务通过getDataSource()获取更新后的结果
		}
	}

	private DataSourceChangeListener dataSourceChangeListener;

	public void setDataSourceChangeListener(
			DataSourceChangeListener dataSourceChangeListener) {
		this.dataSourceChangeListener = dataSourceChangeListener;
	}

	/*
	 * //返回的数组元素个数固定是2，第1个是read，第二个是write private DBSelector[]
	 * createDBSelectors(List<String> dbKeyAndWeightList) { DBSelector
	 * r_DBSelector = null; DBSelector w_DBSelector = null;
	 *
	 * //如果只有一个db，则用OneDBSelector if (dbKeyAndWeightList.size() == 1) { String[]
	 * dbKeyAndWeight = split(dbKeyAndWeightList.get(0), ":"); DataSourceWrapper
	 * dsw = createDataSourceWrapper(dbKeyAndWeight[0], (dbKeyAndWeight.length
	 * == 2 ? dbKeyAndWeight[1] : null), 0); //只有一个数据源时，数据源索引为0
	 *
	 * r_DBSelector = new OneDBSelector(dsw);
	 * r_DBSelector.setDbType(dsw.getDBType()); w_DBSelector = r_DBSelector; }
	 * else { List<List<DataSourceWrapper>> rDataSourceWrappers = new
	 * ArrayList<List<DataSourceWrapper>>(); List<List<DataSourceWrapper>>
	 * wDataSourceWrappers = new ArrayList<List<DataSourceWrapper>>();
	 *
	 * for (int i = 0; i < dbKeyAndWeightList.size(); i++) { String[]
	 * dbKeyAndWeight = split(dbKeyAndWeightList.get(i), ":");
	 *
	 * DataSourceWrapper dsw = createDataSourceWrapper(dbKeyAndWeight[0],
	 * (dbKeyAndWeight.length == 2 ? dbKeyAndWeight[1] : null), i);
	 *
	 * insertSort(rDataSourceWrappers, dsw, true);
	 * insertSort(wDataSourceWrappers, dsw, false); }
	 *
	 * r_DBSelector = createDBSelector(rDataSourceWrappers, true); w_DBSelector
	 * = createDBSelector(wDataSourceWrappers, false); }
	 *
	 * r_DBSelector.setReadable(true); w_DBSelector.setReadable(false);
	 *
	 * return new DBSelector[] { r_DBSelector, w_DBSelector }; }
	 */

	/**
	 * 将给定的k 优先级 加入这个优先级对应的V list 里面。 ----因为可能有多个DS具有相同的优先级 ---add by
	 * mazhidan.pt
	 */
	private static <K, V> void add2LinkedListMap(Map<K, List<V>> m, K key,
			V value) {
		// 从Map中先取出这个优先级的List
		List<V> c = (List<V>) m.get(key);
		// 如果为空，则new一个
		if (c == null) {
			c = new LinkedList<V>();
			m.put(key, c);
		}
		// 不为空，在后面add()
		c.add(value);
	}

	/**
	 * @param dsWeightCommaStr
	 *            : 例如 db0:rwp1q1i0, db1:rwp0q0i1
	 */
	public static List<DataSourceWrapper> buildDataSourceWrapper(
			String dsWeightCommaStr, DataSourceFetcher fetcher) {
		String[] dsWeightArray = dsWeightCommaStr.split(","); // 逗号分隔：db0:rwp1q1i0,
																// db1:rwp0q0i1
		List<DataSourceWrapper> dss = new ArrayList<DataSourceWrapper>(
				dsWeightArray.length);
		for (int i = 0; i < dsWeightArray.length; i++) {
			String[] dsAndWeight = dsWeightArray[i].split(":"); // 冒号分隔：db0:rwp1q1i0
			String dsKey = dsAndWeight[0].trim();
			String weightStr = dsAndWeight.length == 2 ? dsAndWeight[1] : null;

			// 如果多个group复用一个真实dataSource，会造成所有group引用
			// 这个dataSource的配置 会以最后一个dataSource的配置为准
			DataSource dataSource = fetcher.getDataSource(dsKey);
			DBType fetcherDbType = fetcher.getDataSourceDBType(dsKey);
			// dbType = fetcherDbType == null ? dbType : fetcherDbType;
			DataSourceWrapper dsw = new DataSourceWrapper(dsKey, weightStr,
					dataSource, fetcherDbType, i);
			dss.add(dsw);
		}
		return dss;
	}

	/**
	 * 根据给定的具有读写优先级及每个优先级对应的DataSource链表的Map，构造DBSelector---add by mazhidan.pt
	 *
	 * @param priority2DswList
	 * @param isRead
	 * @return
	 */
	private DBSelector createDBSelector(
			Map<Integer/* 优先级 */, List<DataSourceWrapper>> priority2DswList,
			boolean isRead) {
		if (priority2DswList.size() == 1) { // 只有一个优先级直接使用EquityDbManager
			return createDBSelector2(priority2DswList.entrySet().iterator()
					.next().getValue(), isRead);
		} else {
			List<Integer> priorityKeys = new LinkedList<Integer>();
			priorityKeys.addAll(priority2DswList.keySet());
			Collections.sort(priorityKeys); // 优先级从小到大排序
			EquityDbManager[] priorityGroups = new EquityDbManager[priorityKeys
					.size()];
			for (int i = 0; i < priorityGroups.length; i++) { // 最大的优先级放到最前面
				List<DataSourceWrapper> dswList = priority2DswList
						.get(priorityGroups.length - 1 - i); // 倒序
				// PriorityDbGroupSelector依赖EquityDbManager抛出的NoMoreDataSourceException来实现，
				// 所以这里即使只有一个ds也只能仍然用EquityDbManager
				priorityGroups[i] = createEquityDbManager(dswList, isRead, groupExtraConfig);


			}
			return new PriorityDbGroupSelector(priorityGroups);
		}
	}

	private AbstractDBSelector createDBSelector2(
			List<DataSourceWrapper> dswList, boolean isRead) {
		AbstractDBSelector dbSelector;
		if (dswList.size() == 1) {
			DataSourceWrapper dsw = dswList.get(0);
			dbSelector = new OneDBSelector(dsw);
			dbSelector.setDbType(dsw.getDBType());
		} else {
			dbSelector = createEquityDbManager(dswList, isRead,
					groupExtraConfig);
		}
		return dbSelector;
	}

	/*
	 * private DBSelector createDBSelector(List<List<DataSourceWrapper>> list,
	 * boolean isRead) {
	 *
	 * int size = list.size(); //优先级别个数 if (size == 1) {
	 * //只有一个优先级直接使用EquityDbManager return createEquityDbManager(list.get(0),
	 * isRead); } else { EquityDbManager[] priorityGroups = new
	 * EquityDbManager[size]; for (int i = 0; i < size; i++) { priorityGroups[i]
	 * = createEquityDbManager(list.get(i), isRead); } return new
	 * PriorityDbGroupSelector(priorityGroups); } }
	 */

	private static EquityDbManager createEquityDbManager(
			List<DataSourceWrapper> list, boolean isRead,
			GroupExtraConfig groupExtraConfig) {
		Map<String, DataSourceWrapper> dataSourceMap = new HashMap<String, DataSourceWrapper>(
				list.size());
		Map<String, Integer> weightMap = new HashMap<String, Integer>(
				list.size());

		DBType dbType = null;
		for (DataSourceWrapper dsw : list) {
			String dsKey = dsw.getDataSourceKey();
			dataSourceMap.put(dsKey, dsw);
			weightMap
					.put(dsKey, isRead ? dsw.getWeight().r : dsw.getWeight().w);

			if (dbType == null) {
				dbType = dsw.getDBType();
			}
		}
		EquityDbManager equityDbManager = new EquityDbManager(dataSourceMap,
				weightMap, groupExtraConfig);
		equityDbManager.setDbType(dbType);
		return equityDbManager;
	}

	/**
	 * 因为数据源个数通常在3个左右，所以这里使用了简单的插入排序算法，
	 * 两级List(如:List<List<DataSourceWrapper>>)类似下面这种结构，
	 * 第一级List(纵列)代表优先级，优先级最高的排在List的0号位置处，其次是1号位置，依此类推，
	 * 第二级List(横列)表示相同优先级的多个数据源。 ---- |p9|-->|db0|-->|db2| ---- |p8|-->|db1|
	 * ---- |p7|-->|db3| ----
	 *
	 * @param priorityList
	 *            上一次已排好序的数据源列表，由调用者确保不为null。
	 * @param dsw
	 *            当前需要插入到列表的数据源
	 * @param isRead
	 *            因为优先级分读写优先级，读用p表示，写用q表示，如果isRead为true就使用P，否则用q。
	 */
	/*
	 * public static void insertSort(List<List<DataSourceWrapper>> priorityList,
	 * DataSourceWrapper dsw, boolean isRead) {
	 * //如果读或写的权重为0，那么就表示此数据源对应的数据库不可读或不可写，此时什么都不做，忽略此数据源。 // if ((isRead &&
	 * dsw.getWeight().r == 0) || (!isRead && dsw.getWeight().w == 0)) //
	 * return;
	 *
	 * List<DataSourceWrapper> samePriorityDataSourceWrappers;
	 *
	 * int newPriority = isRead ? dsw.getWeight().p : dsw.getWeight().q;
	 *
	 * int index = 0; int size = priorityList.size();
	 *
	 * while (index < size) { samePriorityDataSourceWrappers =
	 * priorityList.get(index);
	 *
	 * //去除priorityList中的无效元素，防止空指针异常和下标越界异常。 if (samePriorityDataSourceWrappers
	 * == null || samePriorityDataSourceWrappers.size() == 0) {
	 * priorityList.remove(index); size--; continue; }
	 *
	 * Weight oldWeight = samePriorityDataSourceWrappers.get(0).getWeight(); int
	 * oldPriority = isRead ? oldWeight.p : oldWeight.q;
	 *
	 * if (newPriority == oldPriority) { //这里不用按权重排序了
	 * samePriorityDataSourceWrappers.add(dsw); return; } else if (newPriority >
	 * oldPriority) { break; } else { index++; } }
	 *
	 * //没有找到相同优先级时新插入一个优先级 (当size=0时也会走到这里) samePriorityDataSourceWrappers =
	 * new ArrayList<DataSourceWrapper>();
	 * samePriorityDataSourceWrappers.add(dsw); priorityList.add(index,
	 * samePriorityDataSourceWrappers); }
	 */
	/*
	 * private DataSourceWrapper createDataSourceWrapper(String dsKey, String
	 * weightStr, int dataSourceIndex) { aliveDataSourceKeys.add(dsKey);
	 *
	 * DataSourceWrapper dsw = dataSourceWrapperMap.get(dsKey); if (dsw != null)
	 * { //dsw.setWeightStr(weightStr);
	 * //dsw.setDataSourceIndex(dataSourceIndex); } else { if
	 * (createTAtomDataSource) { TAtomDataSource ads =
	 * createTAtomDataSource(dsKey); dsw = new DataSourceWrapper(dsKey,
	 * weightStr, ads, getDBTypeFrom(ads), dataSourceIndex);
	 * dataSourceWrapperMap.put(dsKey, dsw); } else { throw new
	 * IllegalArgumentException(dsKey + " not exist!"); } } return dsw; }
	 */

	/**
	 * 因为我们的的Group配置中，配置的AtomDataSource都是用这个DataSource的dbkey表示
	 * 所以，我们需要将这个dbkey从Diamond上取得我们对应的配置信息，并且生成真正的AtomDataSource ---add by
	 * mazhidan.pt
	 */
	private TAtomDataSource createTAtomDataSource(String dsKey) {
		TAtomDataSource ads = null;
		try {
			ads = new TAtomDataSource();
			ads.setAppName(tGroupDataSource.getAppName());
			ads.setDbKey(dsKey);

			ads.init(); // TAtomDataSource的init()会throws Exception

			ads.setLogWriter(tGroupDataSource.getLogWriter());
			ads.setLoginTimeout(tGroupDataSource.getLoginTimeout());
		} catch (Exception e) {
			throw new TAtomDataSourceException("TAtomDataSource无法初始化: dsKey="
					+ dsKey, e);
		}
		return ads;
	}

	/**
	 * 根据当前的读写状态，检查数据源是否可用，数据源分两种：TAtomDataSource和普通的数据源(如DBCP数据源)
	 *
	 * @param ds
	 *            要检查的数据源
	 * @param isRead
	 *            是对数据源进行读操作(isRead=true)，还是写操作(isRead=false)
	 * @return 普通的数据源不管当前的读写状态是什么，总是可用的，返回true。
	 *         TAtomDataSource如果当前的状态是NA返回false, 否则根据WR状态以及isRead的值决定
	 */
	public static boolean isDataSourceAvailable(DataSource ds, boolean isRead) {
		if (ds instanceof DataSourceWrapper)
			ds = ((DataSourceWrapper) ds).getWrappedDataSource();

		if (!(ds instanceof TAtomDataSource))
			return true;

		AtomDbStatusEnum status = ((TAtomDataSource) ds).getDbStatus();
		if (status.isNaStatus())
			return false;

		if (status.isRstatus() && isRead)
			return true;
		if (status.isWstatus() && !isRead)
			return true;

		return false;
	}

	/**
	 * 不能在TGroupDataSource或TGroupConnection或其他地方把DBSelector做为一个字段保存下来，
	 * 否则db权重配置变了之后无法使用最新的权重配置
	 */
	private volatile DBSelector readDBSelectorWrapper;
	private volatile DBSelector writeDBSelectorWrapper;
	private volatile DBSelector runtimeWritableAtomDBSelectorWrapper;

	/**
	 * 根据是读还是写来选择对应的DBSelector---add by mazhidan.pt
	 */
	public DBSelector getDBSelector(boolean isRead,
			boolean autoSelectWriteDataSource) {
		DBSelector dbSelector = isRead ? readDBSelectorWrapper
				: writeDBSelectorWrapper;
		if (!isRead && autoSelectWriteDataSource) {
			// 因为所有dbSelector内部的TAtomDataSource都是指向同一个实例，如果某一个TAtomDataSource的状态改了，
			// 那么所有包含这个TAtomDataSource的dbSelector都会知道状态改变了，
			// 所以只要有一个TAtomDataSource的状态变成W，
			// 那么不管这个dbSelector是专门用于读的，还是专门用于写的，也不管是不是runtimeWritableAtomDBSelector，
			// 只要调用了hasWritableDataSource()都会返回true

			// if(!dbSelector.hasWritableDataSource())
			dbSelector = runtimeWritableAtomDBSelectorWrapper;
		}
		return dbSelector;
	}

	private class ConfigReceiver implements ConfigDataListener {
		public void onDataRecieved(String dataId, String data) {
			try {
				parse(data);
			} catch (Throwable t) {
				logger.error("动态解析配置信息时出现错误:" + data, t);
			}
		}
	}

	private class ExtraGroupConfigReceiver implements ConfigDataListener{
		@Override
		public void onDataRecieved(String dataId, String data) {
			logger.info("receive group extra data:"+data);
			parseExtraConfig(data);
		}
	}

	// 仅用于测试
	public void receiveConfigInfo(String configInfo) {
		configReceiver.onDataRecieved(null, configInfo);
	}

	// 仅用于测试
	public void resetDbGroup(String configInfo) {
		try {
			parse(configInfo);
		} catch (Throwable t) {
			logger.error("resetDbGroup failed:" + configInfo, t);
		}
	}
}
