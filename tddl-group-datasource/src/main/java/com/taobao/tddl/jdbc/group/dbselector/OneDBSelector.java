/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.dbselector;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.taobao.tddl.jdbc.group.DataSourceWrapper;

/**
 * 只有一个数据源的DBSelector
 * 
 * @author linxuan
 * @author yangzhu
 */
public class OneDBSelector extends AbstractDBSelector {
	private final DataSourceHolder dsHolder;
	private final Map<String, DataSource> dsMap;

	public OneDBSelector(DataSourceWrapper dsw) {
		this.dsHolder = new DataSourceHolder(dsw);
		dsMap = new LinkedHashMap<String, DataSource>();
		dsMap.put(dsw.getDataSourceKey(), dsw.getWrappedDataSource());
	}

	public DataSource select() {
		return dsHolder.dsw;
	}

	public Map<String, DataSource> getDataSources() {
		return dsMap;
	}

	protected <T> T tryExecuteInternal(Map<DataSource, SQLException> failedDataSources, DataSourceTryer<T> tryer,
			int times, Object... args) throws SQLException {

		List<SQLException> exceptions;

		if (failedDataSources != null && failedDataSources.containsKey(dsHolder.dsw)) {
			exceptions = new ArrayList<SQLException>(failedDataSources.size());
			exceptions.addAll(failedDataSources.values());

			return tryer.onSQLException(exceptions, this.exceptionSorter, args);
		}

		try {
			return tryOnDataSourceHolder(dsHolder, failedDataSources, tryer, times, args);
		} catch (SQLException e) {
			exceptions = new ArrayList<SQLException>(1);
			exceptions.add(e);
		}
		return tryer.onSQLException(exceptions, this.exceptionSorter, args);
	}

	protected <T> T tryExecuteInternal(DataSourceTryer<T> tryer, int times, Object... args) throws SQLException {
		return this.tryExecute(null, tryer, times, args);
	}

	public DataSourceWrapper get(String dsKey) {
		return dsHolder.dsw.getDataSourceKey().equals(dsKey) ? dsHolder.dsw : null;
	}

	protected DataSourceHolder findDataSourceWrapperByIndex(int dataSourceIndex) {
		return dsHolder.dsw.isMatchDataSourceIndex(dataSourceIndex) ? dsHolder : null;
	}
}
