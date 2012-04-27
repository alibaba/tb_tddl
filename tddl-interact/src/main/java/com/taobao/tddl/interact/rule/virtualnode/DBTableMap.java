/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//Copyright(c) Taobao.com
package com.taobao.tddl.interact.rule.virtualnode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.taobao.tddl.interact.rule.util.VirturalNodeUtil;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a> 
 * @version 1.0
 * @since 1.6
 * @date 2011-6-2ÏÂÎç03:12:39
 */
public class DBTableMap extends WrappedLogic implements VirtualNodeMap{
    private ConcurrentHashMap<String/*1,2,3... math like integer,long*/,String/*group_0*/> dbContext=new ConcurrentHashMap<String,String>();
    private Map<String,String> dbTableMap=new HashMap<String, String>();
    private String logicTable;
    
    private volatile boolean isInit=false;
    public synchronized void init(){
      	if(isInit){
    		return;
    	}
    	
    	isInit=true;
    	
    	if(null!=dbTableMap&&dbTableMap.size()>0){
    		dbContext=(ConcurrentHashMap<String, String>) VirturalNodeUtil.extraReverseMap(dbTableMap);
    		addLogicTableAndSplitorToKey();
    	}else{
    		throw new IllegalArgumentException("no dbTableMap config at all");
    	}
    }
    
    public void addLogicTableAndSplitorToKey(){
        if(tableSlotKeyFormat!=null){
        	ConcurrentHashMap<String,String> reKeyMap=new ConcurrentHashMap<String,String>();
    		for(Map.Entry<String, String> entry:dbContext.entrySet()){
    			String newKey=super.wrapValue(entry.getKey());
    			reKeyMap.put(newKey, entry.getValue());
    		}
    		dbContext.clear();
    		dbContext=reKeyMap;
        }else if(this.logicTable!=null){
    		ConcurrentHashMap<String,String> reKeyMap=new ConcurrentHashMap<String,String>();
    		for(Map.Entry<String, String> entry:dbContext.entrySet()){
    			StringBuilder sb=new StringBuilder(this.logicTable);
    			sb.append(tableSplitor);
    			sb.append(entry.getKey());
    			reKeyMap.put(sb.toString(), entry.getValue());
    		}
    		dbContext.clear();
    		dbContext=reKeyMap;
    	}else{
    	    throw new RuntimeException("TableRule no tableSlotKeyFormat property and logicTable is null");
    	}
    }

	public String getValue(String tableSuffix){
		return dbContext.get(tableSuffix);
    }

	public void setDbTableMap(Map<String, String> dbTableMap) {
		this.dbTableMap = dbTableMap;
	}

	public void setLogicTable(String logicTable) {
		this.logicTable = logicTable;
	}
}
