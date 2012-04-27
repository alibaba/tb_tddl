/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.sample.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Comment for DateUtil
 * <p/>
 * Author By: zhuoxue.yll
 * Created Date: 2012-2-29 下午02:26:02 
 */
public class DateUtil {

	/** yyyy-MM-dd */
	public static final String DATE_FULLHYPHEN = "yyyy-MM-dd";

	/**
	 * 将日期转换成指定格式的字符串
	 *
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String formatDate(Date date, String pattern) {
		if (date == null) {
			return null;
		}
		if (StringUtils.isBlank(pattern)) {
			return null;
		}
		SimpleDateFormat fmt = new SimpleDateFormat(pattern);
		String convStr = fmt.format(date);
		return convStr;
	}

	/**
	 * 日期相减/相加
	 *
	 * @param intervals
	 * @param format
	 * @return
	 */
	public static String getDiffDate(int intervals, String format) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, intervals);
		Date intervalDay = cal.getTime();
		return formatDate(intervalDay, format);
	}

}
