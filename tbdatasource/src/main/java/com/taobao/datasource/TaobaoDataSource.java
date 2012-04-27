/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.datasource;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.taobao.datasource.resource.adapter.jdbc.local.LocalTxDataSource;

public class TaobaoDataSource extends AbstractTaobaoDataSource {

	private static final Logger logger = Logger.getLogger(TaobaoDataSource.class);

	private static final DataSourceConfigFinder dataSourceConfigFinder = new DataSourceConfigFinder();

	private String name;

	private LocalTxDataSource localTxDataSource;

	public void init() throws Exception {
		LocalTxDataSourceDO dataSourceDO = dataSourceConfigFinder.get(name);

		if (dataSourceDO == null) {
			throw new Exception("Cannot find datasource config: " + name);
		}
		if (logger.isDebugEnabled()) {
			logger.debug(dataSourceDO);
		}
		localTxDataSource = TaobaoDataSourceFactory.createLocalTxDataSource(dataSourceDO);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void destroy() throws Exception {
		TaobaoDataSourceFactory.destroy(localTxDataSource);
	}
	
	protected DataSource getDatasource() throws SQLException {
		return localTxDataSource.getDatasource();
	}
}
