/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//Copyright(c) Taobao.com
package com.taobao.tddl.interact.rule.ruleimpl;

import java.util.Map;

import com.taobao.tddl.interact.rule.virtualnode.VirtualNodeMap;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a> 
 * @version 1.0
 * @since 1.6
 * @date 2011-8-8ÏÂÎç07:44:24
 */
public class TableVirtualNodeRule extends VirtualNodeGroovyRule {
	public TableVirtualNodeRule(String expression, VirtualNodeMap vNodeMap) {
		super(expression, vNodeMap);
	}
	
	public TableVirtualNodeRule(String expression, VirtualNodeMap vNodeMap,String extraPackagesStr) {
		super(expression, vNodeMap,extraPackagesStr);
	}

	@Override
	public String eval(Map<String, Object> columnValues, Object outerContext) {
		String value=super.eval(columnValues, outerContext);
		return super.map(value);
	}
}
