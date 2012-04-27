/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//Copyright(c) Taobao.com
package com.taobao.tddl.interact.rule.virtualnode;
/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a> 
 * @version 1.0
 * @since 1.6
 * @date 2011-8-20上午02:58:34
 */
public class WrappedLogic {
	protected String valuePrefix; //无getter/setter
	protected String valueSuffix; //无getter/setter
	protected int valueAlignLen = 0; //无getter/setter
	protected String tableSlotKeyFormat=null;
    
	public void setTableSlotKeyFormat(String tableSlotKeyFormat) {
		if(tableSlotKeyFormat==null){
			return;
		}
		
		this.tableSlotKeyFormat=tableSlotKeyFormat;
		
		int index0 = tableSlotKeyFormat.indexOf('{');
		if (index0 == -1) {
			this.valuePrefix = tableSlotKeyFormat;
			return;
		}
		int index1 = tableSlotKeyFormat.indexOf('}', index0);
		if (index1 == -1) {
			this.valuePrefix = tableSlotKeyFormat;
			return;
		}
		this.valuePrefix = tableSlotKeyFormat.substring(0, index0);
		this.valueSuffix = tableSlotKeyFormat.substring(index1 + 1);
		this.valueAlignLen = index1 - index0 - 1; //{0000}中0的个数
	}
	
	protected String wrapValue(String value) {
		StringBuilder sb = new StringBuilder();
		if (valuePrefix != null) {
			sb.append(valuePrefix);
		}
		
		if (valueAlignLen > 1) {
			int k = valueAlignLen - value.length();
			for (int i = 0; i < k; i++) {
				sb.append("0");
			}
		}
		sb.append(value);
		if (valueSuffix != null) {
			sb.append(valueSuffix);
		}
		return sb.toString();
	}
}
