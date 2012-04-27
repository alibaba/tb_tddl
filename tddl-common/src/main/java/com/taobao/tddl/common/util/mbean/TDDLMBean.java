/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//Copyright(c) Taobao.com
package com.taobao.tddl.common.util.mbean;

import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a> 
 * @version 1.0
 * @since 1.6
 * @date 2011-4-19ÏÂÎç04:23:35
 */
public class TDDLMBean implements DynamicMBean{
	private Map<String,Attribute> attributesMap=new HashMap<String,Attribute>();
	private String desc;
	public TDDLMBean(String desc){
		this.desc=desc;
	}
	
	@Override
	public Object getAttribute(String attribute)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException {
		return attributesMap.get(attribute).getValue();
	}

	public void setAttribute(String name,Object value){
		Attribute attr=new Attribute(name, value);
		this.attributesMap.put(name, attr);
	}

	@Override
	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {
		String name=attribute.getName();
		this.attributesMap.put(name, attribute);
	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		AttributeList list=new AttributeList();
		for(String attribute:attributes){
			Attribute attr=attributesMap.get(attribute);
			if(null!=attr){
				list.add(attr);
			}
		}
		return list;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		for(Attribute attribute:attributes.asList()){
			String name=attribute.getName();
			this.attributesMap.put(name, attribute);
		}
		
		AttributeList list=new AttributeList();
		for(Map.Entry<String, Attribute> attribute:attributesMap.entrySet()){
			list.add(attribute.getValue());
		}
		return list;
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		MBeanAttributeInfo[] fis=new MBeanAttributeInfo[attributesMap.size()];
		int i=0;
		for(Map.Entry<String, Attribute> attribute:attributesMap.entrySet()){
			MBeanAttributeInfo info = null;
			try {
				info = new MBeanAttributeInfo(attribute.getValue().getName(),"",attribute.getValue().getValue().getClass().getMethod("toString"),null);
			} catch (IntrospectionException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			fis[i++]=info;
		}
		
		MBeanInfo info =new MBeanInfo("TDDLMbean", desc, fis, null, null, null);
		return info;
	}
}
