/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author jiechen.qzm
 */
public class GroupExtraConfig {
	/**
	 * when set this parameter is true,table not in tableDsIndexMap or sql not
	 * in sqlDsIndexMap, this sql will be forced go to main db, priority is
	 * low(compare to local seted dataSourceIndex,
	 * tableDsIndexMap,sqlDsIndexMap),higher than weight select.
	 *
	 * add by junyu,2011-11-01
	 */
	private boolean defaultMain = false;

	/**
	 * this map define the actual_table and dataSourceIndex relation
	 *
	 * add by junyu,2011-11-01
	 */
	private Map<String/* table */, Integer/* dataSourceIndex */> tableDsIndexMap = new HashMap<String, Integer>();

	/**
	 * this map define the sql and dataSourceIndex relation
	 *
	 * add by junyu,2011-11-01
	 */
	private Map<String/* sql */, Integer/* dataSourceIndex */> sqlDsIndexMap = new HashMap<String, Integer>();

	/**
	 * this list contain the sqls whitch are forbidden
	 * add by jiechen,2011-12-29
	 */
	private Set<String/* sql */> sqlForbidSet = new HashSet<String>();

	public boolean isDefaultMain() {
		return defaultMain;
	}

	public void setDefaultMain(boolean defaultMain) {
		this.defaultMain = defaultMain;
	}

	public Map<String, Integer> getTableDsIndexMap() {
		return tableDsIndexMap;
	}

	public void setTableDsIndexMap(Map<String, Integer> tableDsIndexMap) {
		this.tableDsIndexMap = tableDsIndexMap;
	}

	public Map<String, Integer> getSqlDsIndexMap() {
		return sqlDsIndexMap;
	}

	public void setSqlDsIndexMap(Map<String, Integer> sqlDsIndexMap) {
		this.sqlDsIndexMap = sqlDsIndexMap;
	}

	public Set<String> getSqlForbidSet() {
		return sqlForbidSet;
	}

	public void setSqlForbidSet(Set<String> sqlForbidSet) {
		this.sqlForbidSet = sqlForbidSet;
	}

	/*public Boolean getDefaultMain() {
		return defaultMain;
	}

	public void setDefaultMain(Boolean defaultMain) {
		this.defaultMain = defaultMain;
	}

	public void clearDefaultMain() {
		this.defaultMain = false;
	}

	public Map<String, Integer> getTableDsIndexMap() {
		return tableDsIndexMap;
	}

	public void setTableDsIndexMap(Map<String, Integer> tableDsIndexMap) {
		this.clearTableDsIndexMap();
		this.tableDsIndexMap.putAll(tableDsIndexMap);
	}

	public void clearTableDsIndexMap() {
		this.tableDsIndexMap.clear();
	}

	public Map<String, Integer> getSqlDsIndexMap() {
		return sqlDsIndexMap;
	}

	public void setSqlDsIndexMap(Map<String, Integer> sqlDsIndexMap) {
		this.clearSqlDsIndexMap();
		this.sqlDsIndexMap.putAll(sqlDsIndexMap);
	}

	public void clearSqlDsIndexMap() {
		this.sqlDsIndexMap.clear();
	}

	public Set<String> getSqlForbidSet() {
		return sqlForbidSet;
	}

	public void setSqlForbidSet(Set<String> sqlForbidSet) {
		this.clearSqlForbinSet();
		this.sqlForbidSet.addAll(sqlForbidSet);
	}

	public void clearSqlForbinSet() {
		this.sqlForbidSet.clear();
	}

	public void clearAll() {
		this.clearDefaultMain();
		this.clearTableDsIndexMap();
		this.clearSqlDsIndexMap();
		this.clearSqlForbinSet();
	}*/
}
