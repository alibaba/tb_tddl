/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.exception.runtime;

public class CantFindTargetTabRuleTypeException extends TDLRunTimeException{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7179888759169646552L;
	public CantFindTargetTabRuleTypeException(String msg) {
		super("无法根据输入的tableRule:"+msg+"找到对应的处理方法。");
	}
}
