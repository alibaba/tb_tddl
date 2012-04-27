/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sequence;

public class IDParseFactory {
	
	private static IDParseFactory instance = new IDParseFactory();
	private IDParseFactory(){}
	public static IDParseFactory newInstance() {
		return instance;
	}
	
	public IDParse<?, ?, ?> createIDParse(Config config) {
		if(config.getType() == Config.DEFAULT) {
			return new IDParseImp(config);
		} else if(config.getType() == Config.Long2DATE) {
			return new IDParse4CTU3(config);
		} else {
			//You can't arrive here!
			return null;
		}
	}
	
}
