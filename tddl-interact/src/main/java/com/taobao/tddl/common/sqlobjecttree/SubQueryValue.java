/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sqlobjecttree;

import java.util.Map;

/**
 * 为了解决这样一个问题：
 * 规则引擎不能支持一个=的comparative中包含多个Or的规则匹配。
 * 所以有些特殊的内嵌可直接求出值的subSelect会继承这个接口，在
 * Comparative中有一个hook来专门处理类型之下的数据
 * @author shenxun
 *
 */
public interface SubQueryValue extends Value {
	public void setAliasMap(Map<String, SQLFragment>  map);
	
}
