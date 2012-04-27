/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 经过笛卡尔积以后的一组值，因为有多个参数，每个参数如果都是有范围的情况下，
 * 要覆盖所有情况只有进行笛卡尔积，枚举出所有可能的值，进行运算。
 * 这个就是枚举值中的一个。
 * columns是共享的列。无论samplingField复制几次，都会共享同一组列名。
 * 而enumFields则表示按照列名的顺序通过笛卡尔积的形式枚举出的一组值。
 * 
 * @author shenxun
 *
 */
public class SamplingField{
	/**
	 * 表示按照列名的顺序通过笛卡尔积的形式枚举出的一组值
	 */
	final List<Object> enumFields ;
	
	private String mappingTargetKey;

	private Object mappingValue;
	
	/**
	 * 一组列名
	 */
	private final  List<String> columns ;
	
	final int capacity ;
	
	public SamplingField(List<String> columns,int capacity) {
		this.enumFields = new ArrayList<Object>(capacity);
		this.capacity = capacity;
		this.columns =Collections.unmodifiableList(columns);
	}
	
	public void add(int index,Object value){
		enumFields.add(index,value);
	}
	
	public List<String> getColumns() {
		return columns;
	}

	public List<Object> getEnumFields() {
		return enumFields;
	}

	//final类型的enumFields,并且无setter,且初始化时肯定实例化,所以肯定不为null
	public void clear() {
		enumFields.clear();
	}


	public String getMappingTargetKey() {
		return mappingTargetKey;
	}
	public void setMappingTargetKey(String mappingTargetKey) {
		this.mappingTargetKey = mappingTargetKey;
	}
	@Override
	public String toString() {
		return "columns:"+columns+"enumedFileds:"+enumFields;
	}
	public Object getMappingValue() {
		return mappingValue;
	}
	public void setMappingValue(Object mappingValue) {
		this.mappingValue = mappingValue;
	}
	
}

