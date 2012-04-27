/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.client;

public class DataSourceIndexCacheType {
	public static final int CLEAR_DATASOURCE_INDEX_AT_CONNECTION_CLOSE = 1;
	public static final int CLEAR_DATASOURCE_INDEX_AT_STATEMENT_CLOSE = 2;

	private static int type = CLEAR_DATASOURCE_INDEX_AT_CONNECTION_CLOSE;


	public static void setType(int type) {
		DataSourceIndexCacheType.type = type;
	}

	public static boolean clearAtConnectionClose() {
		return type == CLEAR_DATASOURCE_INDEX_AT_CONNECTION_CLOSE;
	}

	public static boolean clearAtStatementClose() {
		return type == CLEAR_DATASOURCE_INDEX_AT_CONNECTION_CLOSE;
	}
}
