/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.util;

import org.junit.Test;

/**
 * 各种不同实现的比较测试
 * 
 * @author linxuan
 *
 */
public class TStringUtilTest {

	private static String fillTabWithSpace4(String str) {
		if (str == null) {
			return null;
		}

		int sz = str.length();
		StringBuilder buffer = new StringBuilder(sz);

		boolean isLastWhitespace = true; // 去除多余空格
		for (int i = 0; i < sz; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				buffer.append(str.charAt(i));
				isLastWhitespace = false;
			} else {
				if (!isLastWhitespace) {
					buffer.append(" ");
				}
				isLastWhitespace = true;
			}
		}

		return buffer.toString();
	}

	private static String fillTabWithSpace1(String str) {
		if (str == null) {
			return null;
		}

		int sz = str.length();
		StringBuilder buffer = new StringBuilder(sz);

		boolean isLastWhitespace = true; // 去除多余空格
		for (int i = 0; i < sz; i++) {
			char c = str.charAt(i);
			if (!Character.isWhitespace(c)) {
				buffer.append(c);
				isLastWhitespace = false;
			} else {
				if (!isLastWhitespace) {
					buffer.append(" ");
				}
				isLastWhitespace = true;
			}
		}

		return buffer.toString();
	}

	private static String fillTabWithSpace2(String str) {
		if (str == null) {
			return null;
		}

		int sz = str.length();
		StringBuilder buffer = new StringBuilder(str.trim());

		int index0 = -1, index1 = -1;
		for (int i = 0; i < sz; i++) {
			char c = buffer.charAt(i);
			if (!Character.isWhitespace(c)) {
				if (index0 != -1) {
					if (index0 == index1 && buffer.charAt(i - 1) == ' ') {

					} else {
						buffer.replace(index0, index1 + 1, " ");
						sz = sz - (index1 - index0 + 1);
						i = index0 + 1;
					}
				}
				index0 = index1 = -1;

			} else {
				if (index0 == -1) {
					index0 = index1 = i; //第一个空白
				} else {
					index1 = i;
				}
			}
		}

		return buffer.toString();
	}

	private static String fillTabWithSpace3(String str) {
		if (str == null) {
			return null;
		}
		StringBuilder buffer = new StringBuilder(str.length());
		String[] fragments = str.trim().split("\\s+");
		for (String fragment : fragments) {
			if (!"".equals(fragment))
				buffer.append(fragment).append(" ");
		}
		return buffer.toString();
	}

	@Test
	public void testFillTabWithSpace() {
		if (true) { //需要测试时才打开
			return;
		}
		String sql = "   select sum(rate)      from                                                                          feed_receive_0117                                                            t             where       RATED_UID=?     and RATER_UID=?     and suspended=0 and validscore=1      and rater_type=?     and trade_closingdate>=?     and trade_closingdate<?     and id<>?        and (IFNULL(IMPORT_FROM, 0)&8) = 0        #@#mysql_feel_01#@#EXECUTE_A_SQL_TIMEOUT#@#1#@#484#@#484#@#484";
		String xxx = "select sum(rate) from feed_receive_0117 t where RATED_UID=? and RATER_UID=? and suspended=0 and validscore=1 and rater_type=? and trade_closingdate>=? and trade_closingdate<? and id<>? and (IFNULL(IMPORT_FROM, 0)&8) = 0 #@#mysql_feel_01#@#EXECUTE_A_SQL_TIMEOUT#@#1#@#484#@#484#@#484";
		String res = null;
		long time;

		time = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			res = sql.replaceAll("\\s+", " ").trim();
		}
		System.out.println(res.equals(xxx) + "|replRegx|" + (System.currentTimeMillis() - time) + "ms:" + res);

		time = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			res = fillTabWithSpace4(sql);
		}
		System.out.println(res.equals(xxx) + "|origin|" + (System.currentTimeMillis() - time) + "ms:" + res);

		time = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			res = fillTabWithSpace1(sql);
		}
		System.out.println(res.equals(xxx) + "|charAt|" + (System.currentTimeMillis() - time) + "ms:" + res);

		time = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			res = fillTabWithSpace2(sql);
		}
		System.out.println(res.equals(xxx) + "|replace|" + (System.currentTimeMillis() - time) + "ms:" + res);

		time = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			res = fillTabWithSpace3(sql);
		}
		System.out.println(res.equals(xxx) + "|splitRegx|" + (System.currentTimeMillis() - time) + "ms:" + res);

		time = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			res = TStringUtil.fillTabWithSpace(sql);
		}
		System.out.println(res.equals(xxx) + "|substr|" + (System.currentTimeMillis() - time) + "ms:" + res);
	}

}
