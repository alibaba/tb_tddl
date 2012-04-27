/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group;

import com.taobao.tddl.client.util.ThreadLocalMap;
import com.taobao.tddl.client.ThreadLocalString;
import com.taobao.tddl.jdbc.group.dbselector.DBSelector;

/**
 * @author yangzhu
 *
 */
public class ThreadLocalDataSourceIndex {
	public static boolean existsIndex() {
		return getIndexAsObject() != null;
	}

	public static Integer getIndexAsObject() {
		Integer indexObject = null;
		try {
			indexObject = (Integer) ThreadLocalMap.get(ThreadLocalString.DATASOURCE_INDEX);
			if (indexObject == null)
				return null;

			return indexObject;
		} catch (Exception e) {
			throw new IllegalArgumentException(msg(indexObject));
		}
	}

	public static int getIndex() {
		Integer indexObject = null;
		try {
			indexObject = (Integer) ThreadLocalMap.get(ThreadLocalString.DATASOURCE_INDEX);
			//不存在索引时返回-1，这样调用者只要知道返回值是-1就会认为业务层没有设置过索引
			if (indexObject == null)
				return DBSelector.NOT_EXIST_USER_SPECIFIED_INDEX;

			int index = indexObject.intValue();
			//如果业务层已设置了索引，此时索引不能为负值
			if (index < 0)
				throw new IllegalArgumentException(msg(indexObject));

			return index;
		} catch (Exception e) {
			throw new IllegalArgumentException(msg(indexObject));
		}
	}
	
	public static void clearIndex() {
		ThreadLocalMap.remove(ThreadLocalString.DATASOURCE_INDEX);
	}
	
	private static String msg(Integer indexObject) {
		return indexObject + " 不是一个有效的数据源索引，索引只能是大于0的数字";
	}
}
