/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sync;

import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

public class BizTDDLContext{
	public BizTDDLContext(){/* 便于搜索new的地方 */};
	private JdbcTemplate masterJdbcTemplate; //主库组的TDataSource对应的JdbcTemplate
	private String masterColumns; //逗号分隔的主库列名 
	private List<String> uniqueKeys;
	private SlaveInfo[] slaveInfos;

	public void setUniqueKeys(String uniquekeys) {
		this.uniqueKeys = Arrays.asList(uniquekeys.split(","));
	}
	public List<String> getUniqueKey() {
		return uniqueKeys;
	}
	
	public SlaveInfo[] getSlaveInfos() {
		return slaveInfos;
	}

	public void setSlaveInfos(SlaveInfo[] slaveInfos) {
		this.slaveInfos = slaveInfos;
	}

	public JdbcTemplate getMasterJdbcTemplate() {
		return masterJdbcTemplate;
	}

	public void setMasterJdbcTemplate(JdbcTemplate masterJdbcTemplate) {
		this.masterJdbcTemplate = masterJdbcTemplate;
	}

	public String getMasterColumns() {
		return this.masterColumns;
	}

	public void setMasterColumns(String masterColumns) {
		this.masterColumns = masterColumns;
	}

	@Override
	public String toString() {
		return new StringBuilder("{class:").append(getClass().getSimpleName()).append(",masterJdbcTemplate:").append(
				this.masterJdbcTemplate).append(",slaveInfos:").append(Arrays.toString(this.slaveInfos)).toString();
	}
}
