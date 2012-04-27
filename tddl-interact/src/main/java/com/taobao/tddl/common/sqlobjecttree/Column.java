/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sqlobjecttree;

import java.util.Map;


/**
 * select |column| from
 * @author shenxun
 */
public interface Column extends SQLFragment{
	public String getColumn() ;
	public String getTable() ;
	public String getAlias() ;
	public void setAliasMap(Map<String, SQLFragment> aliasMap);
	public Class getNestClass();

}
