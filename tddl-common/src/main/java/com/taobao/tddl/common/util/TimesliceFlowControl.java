/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 将时间片分为多个槽，每个槽一个计数器。游标按时间循环遍历每个槽。游标移动时才清零并且只清零当前的槽；
 * 因为采用mod计算(cursor = currentTime % timeslice/aSlotTime)，游标到头会自动折回来，事实上是一个环
 * 
 *                     cursor
 *                       | 
 * +---------------------+-------------------------+
 * |   |   |   |   |   | C |   |   |   |   |   |   |
 * +-----------------------------------------------+
 * |                                               |
 *  \-----------------timeslice-------------------/
 * 
 * 
 * @author linxuan
 *
 */
public class TimesliceFlowControl {
	private final static int MAX_SLOT = 20; //最多20片
	private final static int MIN_SLOT_TIME = 500; //slot时间最少500毫秒

	private final String name;
	private final AtomicInteger[] slots; //槽数组，最小的时间粒度数组
	private final int aSlotTimeMillis; //一个槽的时间，最小的时间单位
	private final int timesliceMillis; //总的时间窗口（时间片）大小
	private final int timesliceMaxIns; //时间片内允许的最大访问次数(进入个数)

	private final AtomicInteger total = new AtomicInteger(); //总的计数
	private final AtomicInteger totalReject = new AtomicInteger(); //总的拒绝/超限计数
	private volatile int cursor = 0; //游标
	private volatile long cursorTimeMillis = System.currentTimeMillis(); //当前slot的开始时间

	/**
	 * @param name 流控的名称
	 * @param slotTimeMillis //一个槽的时间
	 * @param slotCount //槽的数目
	 * @param limit //时间窗口内最多允许执行的次数，设为0则不限制
	 */
	public TimesliceFlowControl(String name, int aSlotTimeMillis, int slotCount, int timesliceMaxIns) {
		if (slotCount < 2) {
			throw new IllegalArgumentException("slot至少要有两个");
		}
		this.name = name;
		this.aSlotTimeMillis = aSlotTimeMillis;
		this.timesliceMillis = aSlotTimeMillis * slotCount;
		this.timesliceMaxIns = timesliceMaxIns;

		slots = new AtomicInteger[slotCount];
		for (int i = 0; i < slotCount; i++) {
			slots[i] = new AtomicInteger(0);
		}
	}

	/**
	 * 最小的时间单位取默认的500毫秒
	 * @param name 流控的名称
	 * @param timesliceMillis 时间片; 传0表示使用默认值1分钟
	 * @param limit 时间片内最多允许执行多少次，设为0则不限制
	 */
	public TimesliceFlowControl(String name, int timesliceMillis, int timesliceMaxIns) {
		if (timesliceMillis == 0) {
			timesliceMillis = 60 * 1000; //时间片默认1分钟
		}
		if (timesliceMillis < 2 * MIN_SLOT_TIME) {
			throw new IllegalArgumentException("时间片最少" + (2 * MIN_SLOT_TIME));
		}

		//this(name, 500, timesliceMillis / 500, limit);
		int slotCount = MAX_SLOT; //默认分20个slot
		int slotTime = timesliceMillis / slotCount;
		if (slotTime < MIN_SLOT_TIME) {
			slotTime = MIN_SLOT_TIME; //如果slot时间小于MIN_SLOT_TIME，则最小半秒
			slotCount = timesliceMillis / slotTime;
		}

		this.name = name;
		this.aSlotTimeMillis = slotTime;
		//this.timesliceMillis = timesliceMillis; //直接赋值因为截余的关系，会数组越界
		this.timesliceMillis = aSlotTimeMillis * slotCount;
		this.timesliceMaxIns = timesliceMaxIns;

		slots = new AtomicInteger[slotCount];
		for (int i = 0; i < slotCount; i++) {
			slots[i] = new AtomicInteger(0);
		}
	}

	public void check() {
		if (!allow()) {
			throw new IllegalStateException(reportExceed());
		}
	}

	public String reportExceed() {
		return name + " exceed the limit " + timesliceMaxIns + " in timeslice " + timesliceMillis;
	}

	public boolean allow() {
		final long current = System.currentTimeMillis();
		final int index = (int) ((current % timesliceMillis) / aSlotTimeMillis);

		if (index != cursor) {
			int oldCursor = cursor;
			cursor = index; //尽快赋新值
			final long oldCursorTimeMillis = cursorTimeMillis;
			cursorTimeMillis = current; //尽快赋新值

			//多个线程会进入下面这里，但是每个线程计算的total会大致相同
			if (current - oldCursorTimeMillis > timesliceMillis) {
				//时间差大于timesliceMillis，则整个时间片都应该清零了
				for (int i = 0; i < slots.length; i++) {
					slots[i].set(0); //清零，忽略并发造成的计数出入
				}
				this.total.set(0);
			} else {
				do {
					//吃尾（尾清零），考虑跳跃的情况
					oldCursor++;
					if (oldCursor >= slots.length) {
						oldCursor = 0;
					}
					slots[oldCursor].set(0); //清零，忽略并发造成的计数出入
				} while (oldCursor != index);

				//int clearCount = slots[index].get();
				//slots[index].set(0); //清零，忽略并发造成的计数出入
				int newtotal = 0;
				for (int i = 0; i < slots.length; i++) {
					newtotal += slots[i].get(); //包括了新的当前槽
				}
				this.total.set(newtotal); //设置总数，忽略并发造成的计数出入
			}
		} else {
			if (current - cursorTimeMillis > aSlotTimeMillis) {
				//index相同但是时间差大于一个slot的时间，说明整个时间片都需要清零了
				cursorTimeMillis = current; //尽快赋新值
				for (int i = 0; i < slots.length; i++) {
					slots[i].set(0); //清零，忽略并发造成的计数出入
				}
				this.total.set(0);
			}
			//是否为了避免开销，不做上面的判断？
		}

		if (timesliceMaxIns == 0) {
			return true; //0为不限制
		}
		if (this.total.get() < timesliceMaxIns) {
			//放来的才计数，拒绝的不计数
			slots[index].incrementAndGet();
			total.incrementAndGet();
			return true;
		} else {
			totalReject.incrementAndGet();
			return false;
		}
	}

	/**
	 * @return 当前时间片内的总执行次数
	 */
	public int getCurrentCount() {
		return total.get();
	}

	/**
	 * @return 返回有史以来(对象创建以来)被拒绝/超限的次数
	 */
	public int getTotalRejectCount() {
		return totalReject.get();
	}
}
