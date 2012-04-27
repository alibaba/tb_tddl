/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group;

import static com.taobao.tddl.jdbc.group.util.StringUtils.split;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.taobao.tddl.interact.rule.bean.DBType;

@Deprecated //TGroupDataSource中已经包含了相同的功能
public class SpringTGroupDataSource extends TGroupDataSource implements ApplicationContextAware {
	private ApplicationContext springContext;

	public SpringTGroupDataSource() {
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.springContext = applicationContext;
	}

	public void init() {
		List<DataSourceWrapper> dataSourceWrappers = new ArrayList<DataSourceWrapper>();
		String[] dbKeyAndWeights = split(dataSources, ",");

		int index = 0;
		for (String dbKeyAndWeightStr : dbKeyAndWeights) {
			String[] dbKeyAndWeight = split(dbKeyAndWeightStr, ":");

			String weightStr = dbKeyAndWeight.length == 2 ? dbKeyAndWeight[1] : null;

			DataSourceWrapper dsw = new DataSourceWrapper(dbKeyAndWeight[0], weightStr, (DataSource) springContext
					.getBean(dbKeyAndWeight[0]), dbType, index++);
			
			dataSourceWrappers.add(dsw);
		}
		
		super.init(dataSourceWrappers);
	}

	private DBType dbType = DBType.MYSQL;

	public void setType(String type) {
		if ("oracle".equalsIgnoreCase(type))
			dbType = DBType.ORACLE;
		else if ("mysql".equalsIgnoreCase(type))
			dbType = DBType.MYSQL;
		else
			throw new IllegalArgumentException(type + " 不是有效的数据库类型，只能是mysql或oracle(不区分大小写)");
	}

	private String dataSources;

	public void setDataSources(String dataSources) {
		this.dataSources = dataSources;
	}
}
