/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.tddl.common.Monitor;
import com.taobao.tddl.common.sync.BucketSwitcher.BucketTaker;
import com.taobao.tddl.common.util.TDDLMBeanServer;
import com.taobao.tddl.interact.rule.bean.SqlType;

/**
 * TODO 删除日志时，按日志库、日志表两级分组，每库每表一个BucketSwitcher执行。
 * 
 * @author linxuan
 *
 */
public class RowBasedReplicater implements ReplicationTaskListener, RowBasedReplicaterMBean {
	private static final Log logger = LogFactory.getLog(RowBasedReplicater.class);
	private static final int DEFAULT_THREAD_POOL_SIZE = 16;
	private static final int DEFAULT_WORK_QUEUE_SIZE = 4096;
	public static final int DEFAULT_BATCH_DELETE_SIZE = 1280;
	public static final int DEFAULT_BATCH_UPDATE_SIZE = 512;

	/**
	 * 意义参加SyncServer的相同字段
	 */
	//private long temporaryExtraPlusTime;
	/**
	 * 是否恢复next_sync_time, 当temporaryExtraPlusTime设置较小，在下次处理可接受的范围内时，
	 * 可以设为这个属性为false，以节省更新日志的成本
	 */
	//private boolean isRevertNextSyncTime = false;
	private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
	private int workQueueSize = DEFAULT_WORK_QUEUE_SIZE;

	private ThreadPoolExecutor replicationExecutor; //数据复制线程池
	protected ThreadPoolExecutor deleteSyncLogExecutor; //删除日志线程池
	protected ThreadPoolExecutor updateSyncLogExecutor; //更新日志线程池
	private NoStrictBucketSwitcher<RowBasedReplicationContext> deleteBucketSwitcher;
	private NoStrictBucketSwitcher<RowBasedReplicationContext> updateBucketSwitcher;

	public RowBasedReplicater() {

	}

	public void init() {
		/**
		 * 复制线程池：CallerRunsPolicy: 队列满则execute阻塞
		 */
		replicationExecutor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(workQueueSize), new ThreadPoolExecutor.CallerRunsPolicy());

		/**
		 * 删除和更新线程池：加了Log的DiscardPolicy
		 */
		deleteSyncLogExecutor = new ThreadPoolExecutor(1, 2, 0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(10), new RejectedExecutionHandler() {
					public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
						logger.warn("A DeleteSyncLogTask discarded");
					}
				});
		updateSyncLogExecutor = new ThreadPoolExecutor(1, 2, 0L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(10), new RejectedExecutionHandler() {
					public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
						logger.warn("A UpdateSyncLogTask discarded");
					}
				});

		/**
		 * 删除和更新日志的积累批量操作水桶切换器
		 */
		final BucketTaker<RowBasedReplicationContext> deleteBucketTaker = new BucketTaker<RowBasedReplicationContext>(deleteSyncLogExecutor) {
			@Override
			public Runnable createTakeAwayTask(Collection<RowBasedReplicationContext> list) {
				return new DeleteSyncLogTask(list);
			}

		};
		final BucketTaker<RowBasedReplicationContext> updateBucketTaker = new BucketTaker<RowBasedReplicationContext>(updateSyncLogExecutor) {

			@Override
			public Runnable createTakeAwayTask(Collection<RowBasedReplicationContext> list) {
				return new UpdateSyncLogTask(list);
			}
			
		};
		deleteBucketSwitcher = new NoStrictBucketSwitcher<RowBasedReplicationContext>(deleteBucketTaker,
				DEFAULT_BATCH_DELETE_SIZE);
		updateBucketSwitcher = new NoStrictBucketSwitcher<RowBasedReplicationContext>(updateBucketTaker,
				DEFAULT_BATCH_UPDATE_SIZE);

		TDDLMBeanServer.registerMBean(this, "Replicater"); //注册JMX
	}

	public static class DeleteSyncLogTask implements Runnable {
		private final Collection<RowBasedReplicationContext> contexts;

		public DeleteSyncLogTask(Collection<RowBasedReplicationContext> contexts) {
			this.contexts = contexts;
		}

		public void run() {
			/**
			 * 真正调用删除日志逻辑的地方
			 */
			RowBasedReplicationExecutor.batchDeleteSyncLog(contexts);
		}
	}

	static class UpdateSyncLogTask implements Runnable {
		private final Collection<RowBasedReplicationContext> contexts;

		public UpdateSyncLogTask(Collection<RowBasedReplicationContext> contexts) {
			this.contexts = contexts;
		}

		public void run() {
			/**
			 * 真正调用更新日志逻辑的地方
			 * 处理完成后，若没有成功，则批量更新next_sync_time
			 */
			//RowBasedReplicationExecutor.batchUpdateSyncLog(contexts, -temporaryExtraPlusTime);
			RowBasedReplicationExecutor.batchUpdateSyncLog(contexts, 0);
		}
	}
	
	public static class InDeleteSyncLogTask implements Runnable {
		private final Collection<RowBasedReplicationContext> contexts;
		private final int onceSize;

		public InDeleteSyncLogTask(Collection<RowBasedReplicationContext> contexts, int size) {
			this.contexts = contexts;
			this.onceSize = size;
		}

		public void run() {
			/**
			 * 真正调用删除日志逻辑的地方
			 */
			RowBasedReplicationExecutor.inDeleteSyncLog(contexts, onceSize);
		}
	}

	static class InUpdateSyncLogTask implements Runnable {
		private final Collection<RowBasedReplicationContext> contexts;
		private final int onceSize;

		public InUpdateSyncLogTask(Collection<RowBasedReplicationContext> contexts, int size) {
			this.contexts = contexts;
			this.onceSize = size;
		}

		public void run() {
			/**
			 * 真正调用更新日志逻辑的地方
			 * 处理完成后，若没有成功，则批量更新next_sync_time
			 */
			//RowBasedReplicationExecutor.batchUpdateSyncLog(contexts, -temporaryExtraPlusTime);
			RowBasedReplicationExecutor.inUpdateSyncLog(contexts, 0, onceSize);
		}
	}


	/**
	 * 放入线程池执行。线程池满则当前线程执行(ThreadPoolExecutor.CallerRunsPolicy)
	 * 传入本对象作为每个任务处理完成的ReplicationTaskListener
	 * @see onTaskCompleted
	 */
	/*public void replicate(RowBasedReplicationContext context) {
		replicationExecutor.execute(new RowBasedReplicationTask(context, this));
	}*/

	/**
	 * 不抛出任何异常
	 * 放入线程池执行。线程池满则当前线程执行(ThreadPoolExecutor.CallerRunsPolicy)
	 * 传入本对象作为每个任务处理完成的ReplicationTaskListener
	 * @see onTaskCompleted
	 */
	public void replicate(Collection<RowBasedReplicationContext> contexts) {
		contexts = mergeAndReduce(contexts);
		long timeused, time0 = System.currentTimeMillis();
		for (RowBasedReplicationContext context : contexts) {
			try {
				//replicater.replicate(context);
				replicationExecutor.execute(new RowBasedReplicationTask(context, this));
			} catch (Throwable t) {
				logger.warn("[SyncServer]replicate failed", t);
			}
		}
		timeused = System.currentTimeMillis() - time0;
		logger.warn(contexts.size() + " replication logs processe tasks accepted, time used:" + timeused);
		Monitor.add(Monitor.KEY1, Monitor.KEY2_SYNC, Monitor.KEY3_ReplicationTasksAccepted, contexts.size(), timeused);
	}

	/**
	 * 日志合并策略：对于重复的日志，只保留一条去做补偿，其余的直接删掉
	 * 1. 只对update日志做合并
	 * 2. 主库逻辑表和主键列名，主键列值相同的更新日志，视为对同一条数据操作的日志
	 * 3. 以gmt_create最大的日志为准，其他日志丢弃（不做操作，直接当做成功）。
	 * 4. failedTargets：因为合并策略是多条中选一条，忽略其他条。所以失败列表只关注单条即可
	 *    日志合并的处理和失败列表的处理完全独立，不相耦合。
	 */
	private Collection<RowBasedReplicationContext> mergeAndReduce(Collection<RowBasedReplicationContext> contexts) {
		Map<String, RowBasedReplicationContext> sortMap = new HashMap<String, RowBasedReplicationContext>(contexts.size());
		List<RowBasedReplicationContext> noMergeList = new ArrayList<RowBasedReplicationContext>(contexts.size());
		for (RowBasedReplicationContext context : contexts) {
			if (SqlType.INSERT.equals(context.getSqlType())) {
				noMergeList.add(context); //对insert不做合并
			} else {
				String key = new StringBuilder(context.getMasterLogicTableName()).append("#").append(
						context.getPrimaryKeyValue()).append("#").append(context.getPrimaryKeyColumn()).toString();
				RowBasedReplicationContext last = sortMap.get(key);
				if (last == null) {
					sortMap.put(key, context);
				} else if (context.getCreateTime().equals(last.getCreateTime())) {
					noMergeList.add(context); //创建时间相同，不做合并。防止两个syncServer互相删除对方的记录
				} else if (context.getCreateTime().after(last.getCreateTime())) {
					sortMap.put(key, context); //保留最新的
				} else {
					logger.warn(new StringBuilder("Dropping a log:id=").append(context.getSyncLogId()).append(
							",LogicTableName=").append(context.getMasterLogicTableName()).append(",").append(
							context.getPrimaryKeyColumn()).append("=").append(context.getPrimaryKeyValue()));
					this.deleteBucketSwitcher.pourin(context);
				}
			}
		}
		noMergeList.addAll(sortMap.values());
		return noMergeList;
	}

	public void onTaskCompleted(RowBasedReplicationContext context, boolean success) {
		if (success) {
			//如果复制成功，放入deleteBucketSwitcher等待批量删除
			this.deleteBucketSwitcher.pourin(context);
		} else {
			//如果复制不成功，放入updateBucketSwitcher等待批量更新next_sync_time
			this.updateBucketSwitcher.pourin(context);
		}
	}

	public void destroy() {
		/**
		 * 据说某个业务在JBoss下会很诡异的一启动就先后调用start和destroy
		 */
		/*if (replicationExecutor != null) {
			replicationExecutor.shutdown();
			try {
				replicationExecutor.awaitTermination(8L, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// ignore
			}
		}*/
	}

	/**
	 * JMX Exporting
	 * executorService.getTaskCount(); //已完成数+正在执行数+等待执行数
	 * executorService.getCompletedTaskCount(); //已完成数
	 * executorService.getQueue().size(); //等待执行数
	 */
	public int getReplicationQueueSize() {
		return replicationExecutor.getQueue().size();
	}

	public int getDeleteSyncLogQueueSize() {
		return deleteSyncLogExecutor.getQueue().size();
	}

	public int getUpdateSyncLogQueueSize() {
		return updateSyncLogExecutor.getQueue().size();
	}

	public long getCompletedReplicationCount() {
		return replicationExecutor.getCompletedTaskCount();
	}

	public long getCompletedDeleteSyncLogCount() {
		return deleteSyncLogExecutor.getCompletedTaskCount();
	}

	public long getCompletedUpdateSyncLogCount() {
		return updateSyncLogExecutor.getCompletedTaskCount();
	}

	public int getDeleteBatchSize() {
		return this.deleteBucketSwitcher.getBucketSize();
	}

	public void setDeleteBatchSize(int bucketSize) {
		this.deleteBucketSwitcher.setBucketSize(bucketSize);
	}

	public int getUpdateBatchSize() {
		return this.updateBucketSwitcher.getBucketSize();
	}

	public void setUpdateBatchSize(int bucketSize) {
		this.updateBucketSwitcher.setBucketSize(bucketSize);
	}

	/**
	 * 无逻辑的getter/setter
	 */
	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	public void setWorkQueueSize(int workQueueSize) {
		this.workQueueSize = workQueueSize;
	}

}
