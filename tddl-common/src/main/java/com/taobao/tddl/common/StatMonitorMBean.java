/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common;


/*
 * @author guangxia
 * @since 1.0, 2010-2-9 下午03:40:20
 */
public interface StatMonitorMBean {
	
    /**
     * 重新开始实时统计
     */
    void resetStat();
    /**
     * 最新统计的时间点
     * 
     * @return
     */
    long getStatDuration();
    /**
     * 获取实时统计结果
     * 
     * @param key1
     * @param key2
     * @param key3
     * @return
     */
    String getStatResult(String key1, String key2, String key3);
    long getDuration();

}
