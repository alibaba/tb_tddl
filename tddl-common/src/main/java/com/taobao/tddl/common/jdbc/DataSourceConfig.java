/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.jdbc;

import javax.sql.DataSource;

import com.taobao.datasource.LocalTxDataSourceDO;
import com.taobao.datasource.resource.adapter.jdbc.local.LocalTxDataSource;

public class DataSourceConfig implements Cloneable {

	/**
	 * 数据源的信息DO
	 */
	private LocalTxDataSourceDO dsConfig;

	/**
	 * 数据源对象，可能是直接实现DataSource的对象，也可能是剥离JBOOS数据源的LocalTxDataSource对象
	 */
	private Object dsObject;
	
	/**
	 * 类型 jndi或者其他
	 */
	private String type;
	
	private boolean alive=true;

	public DataSourceConfig(){
		
	}
	
	public  DataSourceConfig(LocalTxDataSourceDO dsConfig,Object dsObject,String type){
		this.dsConfig=dsConfig;
		this.dsObject=dsObject;
		this.type=type;
	}
	
	public LocalTxDataSourceDO getDsConfig() {
		return dsConfig;
	}

	public void setDsConfig(LocalTxDataSourceDO dsConfig) {
		this.dsConfig = dsConfig;
	}

	public Object getDsObject() {
		return dsObject;
	}

	public void setDsObject(Object dsObject) {
		this.dsObject = dsObject;
	}


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public boolean isContainsDsObj(){
		return null!=dsObject;
	}
	
	public boolean isContainsJbossDsObj(){
		return this.dsObject instanceof LocalTxDataSource; 
	}
	
	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public DataSourceConfig clone() {
		DataSourceConfig  dsConfig=new DataSourceConfig();
		LocalTxDataSourceDO config=null;
		if(null!=this.getDsConfig()){
			config = this.getDsConfig().clone();
			dsConfig.setDsConfig(config);
		}
		dsConfig.setAlive(this.alive);
		dsConfig.setType(this.getType());
		return dsConfig;  
    }  
	
	public DataSource getDataSource(){
		DataSource dataSource=null;
		if(this.dsObject instanceof DataSource){
			dataSource= (DataSource)this.dsObject;
		}else if(this.dsObject instanceof LocalTxDataSource){
			dataSource= ((LocalTxDataSource)this.dsObject).getDatasource();
		}
		return dataSource;
	}
}
