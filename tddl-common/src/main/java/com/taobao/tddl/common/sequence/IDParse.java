/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sequence;


public interface IDParse<IDType, DatabaseArgType, TableArgType> {
	
	long[] pow10 = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000,
		10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L, 10000000000000000L,
		100000000000000000L, 1000000000000000000L};

	class DetachID<IDType, DatabaseArgType, TableArgType> {
		private IDType id;
		private DatabaseArgType databaseArg;
		private TableArgType tableArg;

		public IDType getId() {
			return id;
		}

		public void setId(IDType id) {
			this.id = id;
		}

		public DatabaseArgType getDatabaseArg() {
			return databaseArg;
		}

		public void setDatabaseArg(DatabaseArgType databaseArg) {
			this.databaseArg = databaseArg;
		}

		public TableArgType getTableArg() {
			return tableArg;
		}

		public void setTableArg(TableArgType tableArg) {
			this.tableArg = tableArg;
		}
	}
	
	DetachID<IDType, DatabaseArgType, TableArgType> parse(IDType id);

}
