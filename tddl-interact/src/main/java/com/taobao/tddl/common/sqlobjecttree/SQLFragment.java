/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sqlobjecttree;

import java.util.List;
import java.util.Set;

public interface SQLFragment extends Cloneable{
	
	 public void appendSQL(StringBuilder sb);
	 /**
	  * 将一个sql中不变的StringToken缓存到第二个参数那个list中，token之间有可能会有一些可变的
	  * 东西，比如limit m,n中的m,n.还有表名等
	 * @param logicTableNames
	 * @param list
	 * @param sb
	 * @return
	 */
	public StringBuilder regTableModifiable(Set<String> logicTableNames,List<Object> list,StringBuilder sb);
}
