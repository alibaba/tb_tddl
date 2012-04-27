/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.util;

/**
 * @author yangzhu
 * 
 */
public class Pair<F,S> {
	public F first;
	public S second;

	public Pair(F f,S s) {
		first = f;
		second = s;
	}
}
