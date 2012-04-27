/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sequence;

import com.taobao.tddl.common.sequence.Config.Route;

/*
 * @author guangxia
 * @since 1.0, 2009-4-27 下午05:09:40
 */
public class IDParseImp implements IDParse<Long, Integer, Integer> {
	private Config config;
	
	public IDParseImp(Config config) {
		this.config = config;
	}
	
	/*
	 * @param id 带散库散表信息的id
	 * @param config 从配置文件中读取到的配置
	 * @return 把散库散表信息和原始id生成器生成生成的id分离后的一个bean
	 */
	public DetachID<Long, Integer, Integer> parse(Long id) {
		DetachID<Long, Integer, Integer> detachID = new DetachID<Long, Integer, Integer>();
		
		Route dbRoute = config.getDatabaseRoute();
		Route tableRoute = config.getTableRoute();
		int size = (dbRoute == null ? 0 : dbRoute.getSize()) + 
					(tableRoute == null ? 0 : tableRoute.getSize());
		int routeBits;
		if(config.isPositionRight()) {
			detachID.setId(id / pow10[size]);
			//routeBits不能超过8位（十进制位）
			routeBits = (int) (id % pow10[size]);
		} else {
			detachID.setId(size == 0 ? id : id % pow10[19 - size]);
			routeBits = (int) (size == 0 ? 0 : id / pow10[19 - size]);
		}
		if(dbRoute == null) {
			detachID.setDatabaseArg(null);
		} else {
			detachID.setDatabaseArg((int) (tableRoute == null ? routeBits : routeBits / pow10[tableRoute.getSize()]));
		}
		if(tableRoute == null) {
			detachID.setTableArg(null);
		} else {
			detachID.setTableArg((int) (routeBits % pow10[tableRoute.getSize()]));
		}
		return detachID;
	}

}
