/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.monitor;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 执行定时器
 * 
 * @author junyu
 * 
 */
public class CycleExecuteTimer {
	private Log logger = LogFactory.getLog(CycleExecuteTimer.class);
	private ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor();
	private long time;
	private TimeUnit timeUnit;
	private Runnable task;
	private String taskName;
	private volatile boolean isRun;
	private TimeComputer timeComputer;
	private Thread waitThread;

	/**
	 * 不允许调用无参构造函数
	 */
	@SuppressWarnings("unused")
	private CycleExecuteTimer() {
	}

	/**
	 *  初始化一个任务
	 *  
	 * @param taskName 任务名，用于日志记录
	 * 
	 * @param task
	 *            需要执行的任务
	 * @param time
	 *            间隔时间
	 * @param timeUnit
	 *            间隔时间单位
	 * @param timeComputer
	 *            定时器。用来计算任务何时开始。
	 */
	public CycleExecuteTimer(String taskName, Runnable task, long time,
			TimeUnit timeUnit, TimeComputer timeComputer) {
		this.time = time;
		this.timeUnit = timeUnit;
		this.task = task;
		this.taskName = taskName;
		this.timeComputer = timeComputer;
	}

	public void start() {
		if (isRun) {
			logger.warn(taskName + "任务已经在运行");
			return;
		}

		this.isRun = true;

		rotateExecute();
	}

	private void rotateExecute() {
		long interval=-1L;
		if (this.timeComputer != null) {
			Date startTime = this.timeComputer.getMostNearTime();
			interval= this.timeComputer.getMostNearTimeInterval();

			logger.warn(taskName + "任务将在" + startTime + "开始，距离开始时间还有："
					+ interval + "毫秒");
		}

		/**
		 * 启动固定周期的任务
		 */
		executor.scheduleAtFixedRate(new Runnable() {
			public void run() {
				if (!isRun) {
					logger.warn(taskName + "任务已停止。");
					return;
				}

				try {
					task.run();
				} catch (Exception e) {
					logger.error(taskName + "任务执行异常！",e);
				}
			}
		}, interval==-1L?0L:interval, this.time, this.timeUnit);
	}

	public void stop() {
		if (this.waitThread != null) {
			waitThread.interrupt();
			waitThread = null;
		}
		this.isRun = false;
	}
}
