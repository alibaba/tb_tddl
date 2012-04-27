/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.dbselector;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.taobao.tddl.client.jdbc.sorter.ExceptionSorter;
import com.taobao.tddl.interact.rule.bean.DBType;
import com.taobao.tddl.jdbc.group.DataSourceWrapper;
import com.taobao.tddl.jdbc.group.util.ExceptionUtils;

/**
 * 对等数据库选择器。
 * 在数据完全相同的一组库中选择一个库
 * 用于对HA/RAC情况,多个读库中取一个读的操作
 * 
 * @author linxuan
 */
public interface DBSelector {
	public static final int NOT_EXIST_USER_SPECIFIED_INDEX=-1;
	
	/**
	 * @return 返回该Selector的标识
	 */
	String getId();

	/**
	 * 对等数据库选择器。
	 * 在数据完全相同的一组库中选择一个库
	 * 用于对HA/RAC情况,多个读库中取一个读的操作
	 */
	DataSource select();

	/**
	 * 返回指定dsKey对应的数据源。若对应数据源的当前权重为0，则返回null
	 * 这个方法同时可以用来判断一个dsKey对应的库是否可读或可写：
	 *   rselector.get(wBaseDsKey) != null 则可读
	 *   wselector.get(rBaseDsKey) != null 则可写
	 * TGroupConnection读写连接复用的旧实现会用到这个功能
	 * 
	 * @param dsKey 内部和每一个物理DataSource对应的key, 在初始化dbSelector时指定
	 * @return 返回dsKey对应的数据源
	 */
	DataSource get(String dsKey);

	/**
	 * 设置数据库类型：目前只用来选择exceptionSorter 
	 */
	void setDbType(DBType dbType);

	/**
	 * 以选择到的DataSource和传入的args，重试执行
	 *    tryer.tryOnDataSource(String dsKey, DataSource ds, Object... args)
	 * 每次选择DataSource会排除上次重试失败的, 直到达到指定的重试次数，或期间抛出非数据库不可用异常
	 * 
	 * 抛出异常后，以历次重试异常列表，和最初的args，调用
	 *    tryer.onSQLException(List<SQLException> exceptions, Object... args)
	 * 
	 * @param tryer
	 * @param times
	 * @param args
	 * @throws SQLException
	 */
	<T> T tryExecute(DataSourceTryer<T> tryer, int times, Object... args) throws SQLException;

	/**
	 * @param failedDataSources: 在调用该方法前，已经得知试过失败的DataSource和对应的SQLException
	 * 存在这个参数的原因，是因为数据库操作割裂为getConnection/createStatement/execute几步，而并不是在一个大的try catch中
	 * failedDataSources == null 表示不需要重试，遇到任何异常直接抛出。如在写库上的操作
	 */
	<T> T tryExecute(Map<DataSource, SQLException> failedDataSources, DataSourceTryer<T> tryer, int times,
			Object... args) throws SQLException;

	/**
	 * 是否支持重试。
	 * 这个接口是冗余接口。如果重试功能足够稳定，可以去掉。保留不需要重试的场景提供双重保证
	 * @return 是否支持重试
	 */
	boolean isSupportRetry();

	void setReadable(boolean readable);

	Map<String, DataSource> getDataSources(); //直接获取对应的数据源

	/**
	 * 在DBSelector管理的数据源上重试执行操作的回调接口
	 */
	public static interface DataSourceTryer<T> {
		/**
		 * @param dsKey 内部和每一个物理DataSource对应的key, 在初始化dbSelector时指定
		 * @param ds
		 * @param args 用户调用tryExecute时传入的参数列表
		 * @return
		 * @throws SQLException
		 */
		//T tryOnDataSource(String dsKey, DataSource ds, Object... args) throws SQLException;
		/**
		 * tryExecute中重试调用tryOnDataSource遇到非数据库不可用异常，或用完重试次数时，会调用该方法
		 * @param exceptions 历次重试失败抛出的异常。
		 *    最后一个异常可能是数据库不可用的异常，也可能是普通的SQL异常
		 *    最后一个之前的异常是数据库不可用的异常
		 * @param exceptionSorter 当前用到的判断Exception类型的分类器
		 * @param args 与tryOnDataSource时的args相同，都是用户调用tryExecute时传入的arg
		 * @return 用户（实现者）觉得是否返回什么值
		 * @throws SQLException
		 */
		T onSQLException(List<SQLException> exceptions, ExceptionSorter exceptionSorter, Object... args)
				throws SQLException;

		T tryOnDataSource(DataSourceWrapper dsw, Object... args) throws SQLException;
	}

	/**
	 * DataSourceTryer.onSQLException 直接抛出异常
	 */
	public static abstract class AbstractDataSourceTryer<T> implements DataSourceTryer<T> {
		@SuppressWarnings("unchecked")
		public T onSQLException(List<SQLException> exceptions, ExceptionSorter exceptionSorter, Object... args)
				throws SQLException {
			ExceptionUtils.throwSQLException(exceptions, null, Collections.EMPTY_LIST);
			return null;
		}
	}

}
