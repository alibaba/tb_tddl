/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.sample.base;

import java.util.Date;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import com.taobao.tddl.sample.util.DateUtil;

/**
 * Comment for BaseSampleCase
 * <p/>
 * Author By: zhuoxue.yll
 * Created Date: 2012-2-29 下午02:29:10 
 */

public class BaseSampleCase {
	protected static final String APPNAME = "tddl_sample";
	protected static final String DBKEY_0 = "qatest_normal_0";
	protected static final String GROUP_KEY = "group_sample";
	protected static final int RANDOM_ID = Integer.valueOf(RandomStringUtils.randomNumeric(8));
	protected static String time = DateUtil.formatDate(new Date(), DateUtil.DATE_FULLHYPHEN);
	protected static String nextDay = DateUtil.getDiffDate(1, DateUtil.DATE_FULLHYPHEN);

	protected static void clearData(JdbcTemplate tddlJTX, String sql, Object[] args) {
		if (args == null) {
			args = new Object[] {};
		}
		// 确保数据清除成功
		try {
			tddlJTX.update(sql, args);
		} catch (Exception e) {
			tddlJTX.update(sql, args);
		}
	}

	protected static void prepareData(JdbcTemplate tddlJTX, String sql, Object[] args) {
		if (args == null) {
			args = new Object[] {};
		}

		// 确保数据准备成功
		try {
			int rs = tddlJTX.update(sql, args);
			if (rs <= 0) {
				tddlJTX.update(sql, args);
			}
		} catch (Exception e) {
			int rs = tddlJTX.update(sql, args);
			if (rs <= 0) {
				tddlJTX.update(sql, args);
			}
		}
	}

}
