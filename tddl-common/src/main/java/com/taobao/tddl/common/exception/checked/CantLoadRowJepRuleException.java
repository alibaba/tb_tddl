/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.exception.checked;

public class CantLoadRowJepRuleException extends TDLCheckedExcption{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1765363763147779906L;
	public CantLoadRowJepRuleException(String expression,String vtable,String parameter) {
		super("无法通过param:"+parameter+"|virtualTableName:"+vtable+"|expression:"+expression+"找到指定的规则判断引擎");
	}

}
