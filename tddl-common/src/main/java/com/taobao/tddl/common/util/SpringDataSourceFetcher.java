/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.util;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.taobao.tddl.interact.rule.bean.DBType;

/**
 * 
 * @author linxuan
 *
 */
public class SpringDataSourceFetcher implements DataSourceFetcher, ApplicationContextAware {

	private ApplicationContext springContext; // 拿到上下文
	private DBType dbType = DBType.MYSQL;

	@Override
	public DataSource getDataSource(String key) {
		return (DataSource) this.springContext.getBean(key);
	}

	@Override
	public DBType getDataSourceDBType(String key) {
		return dbType;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.springContext = applicationContext;
	}

	public void setDBType(String type) {
		if ("oracle".equalsIgnoreCase(type))
			dbType = DBType.ORACLE;
		else if ("mysql".equalsIgnoreCase(type))
			dbType = DBType.MYSQL;
		else
			throw new IllegalArgumentException(type + " 不是有效的数据库类型，只能是mysql或oracle(不区分大小写)");
	}
}
