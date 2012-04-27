/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common;

import com.taobao.tddl.client.ThreadLocalString;
import com.taobao.tddl.client.util.ThreadLocalMap;

/**
 * 提供给单独使用GroupDataSource的用户指定数据源以及相关执行信息
 * 
 * @author junyu
 *
 */
public class GroupDataSourceRouteHelper {
	/**
	 * 从一组数据源中选择一个指定序号上的数据源执行SQL。
	 * 
	 * 如：groupKey=ExampleGroup 对应的content为  db1:rw,db2:r,db3:r
	 * <pre>
	 *    RouteHelper.executeByGroupDataSourceIndex(2);
	 *    jdbcTemplate.queryForList(sql);
	 * </pre>
	 * 
	 * 最终查询肯定会在第三个数据源上执行（db3）
	 * 注意，指定db的读写特性需要满足要求，如不可在
	 * 指定只读数据源上进行写操作，否则抛错。
	 * 
	 * @author junyu
	 * @param dataSourceIndex 在指定Group中，所需要执行的db序号
	 */
	public static void executeByGroupDataSourceIndex(int dataSourceIndex) {
		ThreadLocalMap.put(ThreadLocalString.DATASOURCE_INDEX, dataSourceIndex);
	}

	/**
	 * 为了保证一个线程执行多个操作不造成混乱(例如事务中作多个操作)，
	 * 请对每个业务方法做try-finally，并在finally中调用该方法清除index:
	 * 
	 * try{
	 *   GroupDataSourceRouteHelper.executeByGroupDataSourceIndex(0);
	 *   xxxDao.bizOperationxxx();
	 * }finally{
	 *   GroupDataSourceRouteHelper.removeGroupDataSourceIndex();
	 * }
	 * 
	 */
	public static void removeGroupDataSourceIndex() {
		ThreadLocalMap.remove(ThreadLocalString.DATASOURCE_INDEX);
	}
}
