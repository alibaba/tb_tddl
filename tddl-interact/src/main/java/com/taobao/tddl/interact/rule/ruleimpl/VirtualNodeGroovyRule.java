/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//Copyright(c) Taobao.com
package com.taobao.tddl.interact.rule.ruleimpl;

import com.taobao.tddl.interact.rule.virtualnode.VirtualNodeMap;

/**
 * @description
 * @author <a href="junyu@taobao.com">junyu</a> 
 * @version 1.0
 * @since 1.6
 * @date 2011-7-14ÏÂÎç02:08:56
 */
public abstract class VirtualNodeGroovyRule extends GroovyRule<String>{
	private final VirtualNodeMap vNodeMap;
	
	public VirtualNodeGroovyRule(String expression,VirtualNodeMap vNodeMap) {
		super(expression);
		this.vNodeMap=vNodeMap;
	}
	
	public VirtualNodeGroovyRule(String expression,VirtualNodeMap vNodeMap,String extraPackagesStr) {
		super(expression,extraPackagesStr);
		this.vNodeMap=vNodeMap;
	}
	
	protected String map(String key) {
		return this.vNodeMap.getValue(key);
	}
}
