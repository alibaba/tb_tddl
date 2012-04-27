/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sequence;

import java.util.Date;

import com.taobao.tddl.common.sequence.Config.Route;

public class IDParse4CTU3 implements IDParse<Long, Integer, Integer> {
	public static final long Year2000Time = 949385234218L;

	private Config config;
	
	public IDParse4CTU3(Config config) {
		this.config = config;
	}
	
	@SuppressWarnings("unchecked")
	public DetachID<Long, Integer, Integer> parse(Long id) {
		DetachID<Long, Integer, Integer> detachID = new DetachID<Long, Integer, Integer>();

		Route dbRoute = config.getDatabaseRoute();
		Route tableRoute = config.getTableRoute();
		int size = config.getTotalSize();
		long routeBits;
		if(config.isPositionRight()) {
			detachID.setId(id / pow10[size]);
			routeBits = id % pow10[size];
		} else {
			detachID.setId(id % pow10[19 - size]);
			routeBits =  id / pow10[19 - size];
		}
		
		routeBits += Year2000Time;
		Date date = new Date(routeBits);
		
		if(dbRoute == null) {
			detachID.setDatabaseArg(null);
		} else {
			detachID.setDatabaseArg(((Route.AbstractExpression<Date>)dbRoute.getExpression()).execute(date));
		}
		
		if(tableRoute == null) {
			detachID.setTableArg(null);
		} else {
			detachID.setTableArg(((Route.AbstractExpression<Date>)tableRoute.getExpression()).execute(date));
		}
		
		return detachID;

	}

}
