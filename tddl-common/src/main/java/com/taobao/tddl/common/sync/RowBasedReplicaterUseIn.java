/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sync;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import javax.sql.DataSource;

import com.taobao.tddl.common.sync.BucketSwitcher.BucketTaker;

/*
 * @author guangxia
 * @since 1.0, 2010-1-21 下午04:36:13
 * 
 * 免责声明:
 * 由于支持动态改配置，所以使得这个类和这个类调用的类都到处充满了漏洞
 * 很多地方都有可能会在动态改配置的时候发现数据丢失
 * 所以这是一个不严格的实现
 * 幸好，丢数据的后果只会引起延迟的处理或者重复处理，不会错误地删除数据
 */
public class RowBasedReplicaterUseIn extends RowBasedReplicater {
	
	private volatile int deleteInSize = DEFAULT_BATCH_DELETE_SIZE;
	private volatile int updateInSize = 64;
	private volatile int syncDatabase = 4;
	private volatile int syncTable = 8;
			
	static class Switchers {
		final MatrixBucketSwitcher<RowBasedReplicationContext> deleteBucketSwitcher;
		final MatrixBucketSwitcher<RowBasedReplicationContext> updateBucketSwitcher;
		final int syncDatabase;
		final int syncTable;
		final Map<DataSource, Integer> databaseMap;
		volatile int actualMapSize;
		Switchers(ThreadPoolExecutor deleteSyncLogExecutor, final int deleteInSize, ThreadPoolExecutor updateSyncLogExecutor, final int updateInSize, int syncDatabase, int syncTable) {
			this.syncDatabase = syncDatabase;
			this.syncTable = syncTable;
			BucketTaker<RowBasedReplicationContext> deleteBucketTaker = new BucketTaker<RowBasedReplicationContext>(deleteSyncLogExecutor) {
				@Override
				public Runnable createTakeAwayTask(Collection<RowBasedReplicationContext> list) {
					return new InDeleteSyncLogTask(list, deleteInSize);
				}

			};
			BucketTaker<RowBasedReplicationContext> updateBucketTaker = new BucketTaker<RowBasedReplicationContext>(updateSyncLogExecutor) {
				@Override
				public Runnable createTakeAwayTask(Collection<RowBasedReplicationContext> list) {
					return new InUpdateSyncLogTask(list, updateInSize);
				}
				
			};
			this.deleteBucketSwitcher = new MatrixBucketSwitcher<RowBasedReplicationContext>(deleteBucketTaker,
					deleteInSize, syncDatabase, syncTable);
			this.updateBucketSwitcher = new MatrixBucketSwitcher<RowBasedReplicationContext>(updateBucketTaker,
					updateInSize, syncDatabase, syncTable);
			this.databaseMap = new HashMap<DataSource, Integer>(syncDatabase);
		}
		int getDbNum(DataSource dataSource) {
			Integer dbNum = null;
			if(actualMapSize == syncDatabase) {
				dbNum = databaseMap.get(dataSource);
			}
			if(dbNum == null) {
				synchronized (this) {
					dbNum = databaseMap.get(dataSource);
					if(dbNum == null) {
						dbNum = actualMapSize;
						databaseMap.put(dataSource, dbNum);
						++actualMapSize;
					}				
				}
			}
			return dbNum;
		}
	}
	
	private volatile Switchers switchers;

	public void init() {
		super.init();		
		switchers = new Switchers(deleteSyncLogExecutor, deleteInSize, updateSyncLogExecutor, updateInSize, syncDatabase, syncTable);		
	}
	
	@Override
	public void onTaskCompleted(RowBasedReplicationContext context, boolean success) {
		DataSource dataSource = context.getSyncLogJdbcTemplate().getDataSource();
		int tableNum = SyncUtils.getSyncLogTableSuffix(context.getSyncLogId());
		Switchers local_switchers = switchers;
		int dbNum = local_switchers.getDbNum(dataSource);
		if (success) {
			//如果复制成功，放入deleteBucketSwitcher等待批量删除
			local_switchers.deleteBucketSwitcher.pourin(context, dbNum, tableNum);
		} else {
			//如果复制不成功，放入updateBucketSwitcher等待批量更新next_sync_time
			local_switchers.updateBucketSwitcher.pourin(context, dbNum, tableNum);
		}
	}
	
	public synchronized void setSyncDatabase(int syncDatabase) {
		if(this.syncDatabase == syncDatabase) {
			return;
		}
		this.syncDatabase = syncDatabase;
		if(switchers != null) {
			switchers = new Switchers(deleteSyncLogExecutor, deleteInSize, updateSyncLogExecutor, updateInSize, syncDatabase, syncTable);
		}
	}

	public synchronized int getSyncDatabase() {
		return syncDatabase;
	}

	public synchronized void setSyncTable(int syncTable) {
		if(this.syncTable == syncTable) {
			return;
		}
		this.syncTable = syncTable;
		if(switchers != null) {
			switchers = new Switchers(deleteSyncLogExecutor, deleteInSize, updateSyncLogExecutor, updateInSize, syncDatabase, syncTable);
		}
	}

	public synchronized int getSyncTable() {
		return syncTable;
	}

	public synchronized void setUpdateInSize(int updateInSize) {
		this.updateInSize = updateInSize;
	}

	public synchronized int getUpdateInSize() {
		return updateInSize;
	}

	public synchronized void setDeleteInSize(int deleteInSize) {
		this.deleteInSize = deleteInSize;
	}

	public synchronized int getDeleteInSize() {
		return deleteInSize;
	}

}
