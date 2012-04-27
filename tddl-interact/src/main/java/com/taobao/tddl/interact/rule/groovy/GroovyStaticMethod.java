/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule.groovy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.taobao.tddl.interact.rule.util.NestThreadLocalMap;

/**
 * 
 * 可直接用于groovy规则中的便捷方法
 * 
 * @author shenxun
 * @author linxuan
 */
public class GroovyStaticMethod {
	public static final String GROOVY_STATIC_METHOD_CALENDAR = "GROOVY_STATIC_METHOD_CALENDAR";
	private final static long[] pow10 = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000,
			10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L,
			10000000000000000L, 100000000000000000L, 1000000000000000000L };

	/**
	 * @return 返回4位年份
	 */
	public static int yyyy(Date date) {
		Calendar cal = getCalendar(date);
		return cal.get(Calendar.YEAR);
	}

	public static int yyyy(Calendar cal) {
		return cal.get(Calendar.YEAR);
	}

	/**
	 * @return 返回2位年份（年份的后两位）
	 */
	public static int yy(Date date) {
		Calendar cal = getCalendar(date);
		return cal.get(Calendar.YEAR) % 100;
	}

	public static int yy(Calendar cal) {
		return cal.get(Calendar.YEAR) % 100;
	}

	/**
	 * @return 返回月份数字，注意：从1开始：1-12（返回 Calendar.MONTH对应的值加1）
	 */
	public static int month(Date date) {
		Calendar cal = getCalendar(date);
		return cal.get(Calendar.MONTH) + 1;
	}

	public static int month(Calendar cal) {
		return cal.get(Calendar.MONTH) + 1;
	}

	/**
	 * @return 返回2位的月份字串，从01开始：01-12（Calendar.MONTH对应的值加1）
	 */
	public static String mm(Date date) {
		Calendar cal = getCalendar(date);
		int m = cal.get(Calendar.MONTH) + 1;
		return m < 10 ? "0" + m : String.valueOf(m);
	}

	public static String mm(Calendar cal) {
		int m = cal.get(Calendar.MONTH) + 1;
		return m < 10 ? "0" + m : String.valueOf(m);
	}

	/**
	 * @return 返回 Calendar.DAY_OF_WEEK 对应的值
	 */
	public static int week(Date date) {
		Calendar cal = getCalendar(date);
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	public static int week(Calendar cal) {
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * 旧规则默认的dayofweek : 如果offset  = 0;那么为默认
	 * SUNDAY=1; MONDAY=2; TUESDAY=3; WEDNESDAY=4; THURSDAY=5; FRIDAY=6; SATURDAY=7;
	 */
	public static int dayofweek(Date date, int offset) {
		Calendar cal = getCalendar(date);
		return cal.get(Calendar.DAY_OF_WEEK) + offset;
	}

	public static int dayofweek(Calendar cal, int offset) {
		return cal.get(Calendar.DAY_OF_WEEK) + offset;
	}

	/**
	 * 旧规则的dayofweek.因为旧规则计算结果为数组下标，尤其表规则结果是库内表数组下标，必须从0开始，
	 * 因此必须让day of week从0 开始。通过直接offset = -1 解决：星期日=0,星期一=1,...星期六=6
	 */
	public static int dayofweek(Date date) {
		return dayofweek(date, -1);
	}

	public static int dayofweek(Calendar cal) {
		return dayofweek(cal, -1);
	}

	/**
	 * @return 返回4位年份和2位月份的字串，月份从01开始：01-12
	 */
	public static String yyyymm(Date date) {
		Calendar cal = getCalendar(date);
		return yyyy(cal) + mm(cal);
	}

	public static String yyyymm(Calendar cal) {
		return yyyy(cal) + mm(cal);
	}

	/**
	 * @return 返回 4位年份_2位月份 的字串，月份从01开始：01-12
	 */
	public static String yyyy_mm(Date date) {
		Calendar cal = getCalendar(date);
		return yyyy(cal) + "_" + mm(cal);
	}

	public static String yyyy_mm(Calendar cal) {
		return yyyy(cal) + "_" + mm(cal);
	}

	/**
	 * @return 返回2位年份和2位月份的字串，月份从01开始：01-12
	 */
	public static String yymm(Date date) {
		Calendar cal = getCalendar(date);
		return yy(cal) + mm(cal);
	}

	public static String yymm(Calendar cal) {
		return yy(cal) + mm(cal);
	}

	/**
	 * @return 返回 2位年份_2位月份 的字串，月份从01开始：01-12
	 */
	public static String yy_mm(Date date) {
		Calendar cal = getCalendar(date);
		return yy(cal) + "_" + mm(cal);
	}

	public static String yy_mm(Calendar cal) {
		return yy(cal) + "_" + mm(cal);
	}

	/**
	 * @return 返回 Calendar.DATE 对应的值。每月的1号值为1, 2号值为2...
	 */
	public static int date(Date date) {
		Calendar cal = getCalendar(date);
		return cal.get(Calendar.DATE);
	}

	public static int date(Calendar cal) {
		return cal.get(Calendar.DATE);
	}

	@SuppressWarnings("unused")
	private static Calendar getCalendar(Calendar c) {
		return c;
	}

	private static Calendar getCalendar(Date date) {
		Calendar cal = (Calendar) NestThreadLocalMap.get(GROOVY_STATIC_METHOD_CALENDAR);
		if (cal == null) {
			cal = Calendar.getInstance();
			NestThreadLocalMap.put(GROOVY_STATIC_METHOD_CALENDAR, cal);
		}
		cal.setTime(date);
		return cal;
	}

	/**
	 * @param bit 补齐后的长度
	 * @param table 数值 
	 * @return 返回前面补0达到bit长度的字符串。如果table长度大于bit，则返回table的原始值
	 */
	public static String placeHolder(int bit, long table) {
		if (bit > 18) {
			throw new IllegalArgumentException("截取的位数不能大于18位");
		}
		if (table == 0) {
			//bugfix 被0除
			return String.valueOf(pow10[bit]).substring(1);
		}
		if (table >= pow10[bit - 1]) {
			//当数值的width >= 要求的补齐位数时，应该直接返回原始数值
			return String.valueOf(table);
		}
		long max = pow10[bit];
		long placedNumber = max + table;
		return String.valueOf(placedNumber).substring(1);
	}

	@SuppressWarnings("unused")
	private static long getModRight(long targetID, int size, int bitNumber) {
		if (bitNumber < size) {
			throw new IllegalArgumentException("输入的位数比要求的size还小");
		}
		return (size == 0 ? 0 : targetID / pow10[bitNumber - size]);
	}

	/**
	 * 从左开始，取指定多的位数。默认是一个long形长度的数据，也就是bitNumber= 19
	 * 
	 * @param targetID 目标id，也就是等待被decode的数据
	 * @param st 从哪儿开始取，如果想取最左边的一位那么可以输入st = 0;ed =1;
	 * @param ed 取到哪儿，如果想取最左边的两位，那么可以输入st = 0;ed = 2;
	 * @return
	 */
	//	public static long leftBetween(long targetID,int st,int ed){
	//		int sizeAll = st + ed - 1;
	//		if(sizeAll >= 19||sizeAll <= 0){
	//			throw new IllegalArgumentException("截取19位请直接使用元数据。");
	//		}
	//		if(targetID / pow10[sizeAll] < 1){
	//			throw new IllegalArgumentException(targetID+",小于"+(st+ed)+"位，不能进行计算");
	//		}
	//		long end = getModRight(targetID, ed,19);
	//		return end % pow10[(ed-st)];
	//	}
	public static int quarter(Date date) {
		Calendar cal = getCalendar(date);
		int month = cal.get(Calendar.MONTH);
		return quarter(month);
	}

	public static int quarter(long month) {
		return quarter((int) month);
	}

	public static int halfayear(long month) {
		return halfayear((int) month);
	}

	public static int quarter(int month) {
		if (month > 11 || month < 0) {
			throw new IllegalArgumentException("month range is 1~12");
		}
		return month / 3 + 1;
	}

	public static int halfayear(Date date) {
		Calendar cal = getCalendar(date);
		int month = cal.get(Calendar.MONTH);
		return halfayear(month);
	}

	public static int halfayear(int month) {
		if (month > 11 || month < 0) {
			throw new IllegalArgumentException("month range is 1~12,current value is " + month);
		}
		return month / 6 + 1;
	}

	/**
	 * 从右开始，取指定多的位数。
	 * 假如参数是1234567.那么rightBetwen(1234567,2,3) 返回的数据是 345
	 * rightBetween(10000234,2,2) 返回的数据是2
	 * rightBetween(10000234,3,2) 返回的数据是0
	 * 
	 * @param targetID 目标id，也就是等待被decode的数据
	 * @param closeFrom 从哪儿开始取，如果想取最右边的一位那么可以输入st = 0;ed =1;
	 * @param openTo 取到哪儿，如果想取最右边的两位，那么可以输入st = 0;ed = 2;
	 * @throws
	 * 		IllegalArgumentException 如果st+ed -1 >= 19,这时候对long来说不需要截取。
	 * 								 如果targetId小于st+ed，
	 * @return
	 */
	public static long rightCut(long targetID, int closeFrom, int openTo) {
		int sizeAll = closeFrom + openTo - 1;
		if (sizeAll >= 19 || sizeAll < 0) {
			throw new IllegalArgumentException("截取19位请直接使用元数据。");
		}

		long right = targetID / pow10[(closeFrom)];
		right = right % pow10[openTo];

		return right;
	}

	public static long right(long targetID, int size) {
		if (size >= 19 || size < 0) {
			throw new IllegalArgumentException("截取19位请直接使用元数据。");
		}
		return targetID % pow10[size];
	}

	public static void validate(long targetID, int size) {
		if (targetID / pow10[size - 1] < 1) {
			throw new IllegalArgumentException(targetID + ",小于" + (size) + "位，不能进行计算");
		}
	}

	public static String right(String right, int rightLength) {
		int length = right.length();
		int start = length - rightLength;
		return right.substring(start < 0 ? 0 : start);
	}

	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		System.out.println(quarter(new Date(2010, 0, 1)));
		List<AtomicInteger> li = new ArrayList<AtomicInteger>();
		//		for(int i = 0 ; i < 90; i ++){
		//			li.add(new AtomicInteger(0));
		//		}
		//		for(int i = 0 ; i < 3000; i++){
		//			cal.add(Calendar.DATE, 1);
		//			int wom = getCalendar(cal.getTime()).get(Calendar.DAY_OF_YEAR);
		//			
		//			li.get(wom % 90).incrementAndGet();
		//		}
		//		
		//		System.out.println(li.size());
		//		int i = 0;
		//		for(AtomicInteger inte : li){
		//	
		//			System.out.println(i+"->"+inte.toString());
		//			i++;
		//		}
		cal.setTime(new Date(2012, 0, 1));
		System.out.println(cal.get(Calendar.DAY_OF_YEAR));
	}
}
