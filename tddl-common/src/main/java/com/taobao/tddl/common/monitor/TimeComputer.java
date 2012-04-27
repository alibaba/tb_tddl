/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.monitor;

import java.util.Date;
/**
 * 
 * @author junyu
 *
 */
public interface TimeComputer {
	/**
	 * 得到距离最近的某个时间的间隔
	 * 
	 * @return 毫秒计
	 */
    public long getMostNearTimeInterval();
    
    /**
     * 得到距离的最近的某个时间
     * 
     * @return Date
     */
    public Date getMostNearTime();
}
