/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.tddl.interact.rule.bean.DBType;

/**
 * @author junyu
 * 
 */
public class VirtualTableRoot {
	final Log log = LogFactory.getLog(VirtualTableRoot.class);
	protected String dbType = "MYSQL";
	protected Map<String/* 小写key */, VirtualTable> virtualTableMap;
	protected String defaultDbIndex;
	protected boolean needIdInGroup=false;
	protected boolean completeDistinct=false;

	public DBType getDbTypeEnumObj() {
		return DBType.valueOf(this.dbType);
	}
	
	public String getDbType() {
		return this.dbType;
	}

	public void setDbType(String dbType) {
	    this.dbType=dbType;
	}

	public void init(){
	    for(Map.Entry<String, VirtualTable> entry:virtualTableMap.entrySet()){
	    	log.warn("virtual table start to init :" + entry.getKey());
	    	VirtualTable vtab = entry.getValue();
			if (vtab.getDbType() == null) {
		    	//如果虚拟表中dbType为null,那指定全局dbType
				vtab.setDbType(this.getDbTypeEnumObj());
			}
			if (vtab.getVirtualTbName() == null) {
				vtab.setVirtualTbName(entry.getKey());
			}
			vtab.init();
			log.warn("virtual table inited :" + entry.getKey());
	    }	
	}
	
	/**
	 * 此处有个问题是Map中key对应的VirtualTableRule为null;
	 * 
	 * @param virtualTableName
	 * @return
	 */
	public VirtualTable getVirtualTable(String virtualTableName) {
		if (null != virtualTableName) {
			return virtualTableMap.get(virtualTableName.toLowerCase());
		} else {
			throw new IllegalArgumentException("virtual table name is null");
		}
	}

	public Map<String, VirtualTable> getVirtualTableMap() {
		return virtualTableMap;
	}

	public void setVirtualTableMap(Map<String, VirtualTable> virtualTableMap) {
		this.virtualTableMap = virtualTableMap;
	}

	public Map<String, VirtualTable> getTableRules() {
		return virtualTableMap;
	}

	public void setTableRules(Map<String, VirtualTable> virtualTableMap) {
		//this.virtualTableMap = virtualTableMap;
		Map<String, VirtualTable> lowerKeysLogicTableMap = new HashMap<String, VirtualTable>(virtualTableMap.size());
		for (Entry<String, VirtualTable> entry : virtualTableMap.entrySet()) {
			lowerKeysLogicTableMap.put(entry.getKey().toLowerCase(), entry.getValue());
		}
		this.virtualTableMap = lowerKeysLogicTableMap;
	}

	public String getDefaultDbIndex() {
		return defaultDbIndex;
	}

	public void setDefaultDbIndex(String defaultDbIndex) {
		this.defaultDbIndex = defaultDbIndex;
	}

	public boolean isNeedIdInGroup() {
		return needIdInGroup;
	}

	public void setNeedIdInGroup(boolean needIdInGroup) {
		this.needIdInGroup = needIdInGroup;
	}

	public boolean isCompleteDistinct() {
		return completeDistinct;
	}

	public void setCompleteDistinct(boolean completeDistinct) {
		this.completeDistinct = completeDistinct;
	}
}
