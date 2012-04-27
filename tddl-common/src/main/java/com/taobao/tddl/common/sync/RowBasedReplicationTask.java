/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sync;

public class RowBasedReplicationTask implements Runnable {
	//private final boolean isUpdateLogRecord;
	private final RowBasedReplicationContext context;
	private final ReplicationTaskListener taskListener;

	public RowBasedReplicationTask(RowBasedReplicationContext context, ReplicationTaskListener taskListener) {
		this.context = context;
		this.taskListener = taskListener;
	}

	public RowBasedReplicationTask(RowBasedReplicationContext context) {
		this(context, null);
	}

	public void run() {

		long asyncThreadRunTime = System.currentTimeMillis();
		context.setReplicationStartTime(asyncThreadRunTime);

		//如果taskListener不为null，则不立即删除synclog，而是交给taskListener做批量操作
		boolean success = RowBasedReplicationExecutor.execute(context, taskListener == null);

		if (this.taskListener != null) {
			this.taskListener.onTaskCompleted(context, success);
		}
	}
}
