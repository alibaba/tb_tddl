/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.taobao.tddl.common.exception.runtime.InputStringIsNotValidException;

public class StringUtils {
	public static final String NL = System.getProperty("line.separator");

	/**
	 * 验证输入的参数是否非空非""，如果为空则抛出InputStringIsNotValid异常，异常中使用errMsg作为参数。
	 * 如果非空则对其进行trim
	 * @param str 目标字段
	 * @param errMsg 为空时的提示信息
	 * @return
	 */
	public static String validAndTrim(String str, String errMsg) {
		String ret = null;
		if (str == null || (ret = str.trim()).equals("")) {
			throw new InputStringIsNotValidException(errMsg);
		}
		return ret;
	}

	/**
	 * 对字段进行trim如果字段原来就为null则仍然返回null
	 * @param str 目标字段
	 * @return 为空时的提示信息
	 */
	public static String trim(String str) {
		if (str != null) {
			return str.trim();
		}
		return null;
	}

	/**
	 * Determines whether or not the sting 'searchIn' contains the string
	 * 'searchFor', disregarding case and leading whitespace
	 *
	 * @param searchIn
	 *            the string to search in
	 * @param searchFor
	 *            the string to search for
	 *
	 * @return true if the string starts with 'searchFor' ignoring whitespace
	 */
	public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor) {
		return startsWithIgnoreCaseAndWs(searchIn, searchFor, 0);
	}

	/**
	 * Determines whether or not the sting 'searchIn' contains the string
	 * 'searchFor', disregarding case and leading whitespace
	 *
	 * @param searchIn
	 *            the string to search in
	 * @param searchFor
	 *            the string to search for
	 * @param beginPos
	 *            where to start searching
	 *
	 * @return true if the string starts with 'searchFor' ignoring whitespace
	 */
	public static boolean startsWithIgnoreCaseAndWs(String searchIn, String searchFor, int beginPos) {
		if (searchIn == null) {
			return searchFor == null;
		}

		int inLength = searchIn.length();

		for (; beginPos < inLength; beginPos++) {
			if (!Character.isWhitespace(searchIn.charAt(beginPos))) {
				break;
			}
		}

		return startsWithIgnoreCase(searchIn, beginPos, searchFor);
	}

	/**
	 * Determines whether or not the string 'searchIn' contains the string
	 * 'searchFor', dis-regarding case starting at 'startAt' Shorthand for a
	 * String.regionMatch(...)
	 *
	 * @param searchIn
	 *            the string to search in
	 * @param startAt
	 *            the position to start at
	 * @param searchFor
	 *            the string to search for
	 *
	 * @return whether searchIn starts with searchFor, ignoring case
	 */
	public static boolean startsWithIgnoreCase(String searchIn, int startAt, String searchFor) {
		return searchIn.regionMatches(true, startAt, searchFor, 0, searchFor.length());
	}

	/**
	 * Returns the given string, with comments removed
	 *
	 * @param src
	 *            the source string
	 * @param stringOpens
	 *            characters which delimit the "open" of a string
	 * @param stringCloses
	 *            characters which delimit the "close" of a string, in
	 *            counterpart order to <code>stringOpens</code>
	 * @param slashStarComments
	 *            strip slash-star type "C" style comments
	 * @param slashSlashComments
	 *            strip slash-slash C++ style comments to end-of-line
	 * @param hashComments
	 *            strip #-style comments to end-of-line
	 * @param dashDashComments
	 *            strip "--" style comments to end-of-line
	 * @return the input string with all comment-delimited data removed
	 */
	public static String stripComments(String src, String stringOpens, String stringCloses, boolean slashStarComments,
			boolean slashSlashComments, boolean hashComments, boolean dashDashComments) {
		if (src == null) {
			return null;
		}

		StringBuffer buf = new StringBuffer(src.length());

		// It's just more natural to deal with this as a stream
		// when parsing..This code is currently only called when
		// parsing the kind of metadata that developers are strongly
		// recommended to cache anyways, so we're not worried
		// about the _1_ extra object allocation if it cleans
		// up the code

		StringReader sourceReader = new StringReader(src);

		int contextMarker = Character.MIN_VALUE;
		boolean escaped = false;
		int markerTypeFound = -1;

		int ind = 0;

		int currentChar = 0;

		try {
			while ((currentChar = sourceReader.read()) != -1) {

				if (false && currentChar == '\\') {
					escaped = !escaped;
				} else if (markerTypeFound != -1 && currentChar == stringCloses.charAt(markerTypeFound) && !escaped) {
					contextMarker = Character.MIN_VALUE;
					markerTypeFound = -1;
				} else if ((ind = stringOpens.indexOf(currentChar)) != -1 && !escaped
						&& contextMarker == Character.MIN_VALUE) {
					markerTypeFound = ind;
					contextMarker = currentChar;
				}

				if (contextMarker == Character.MIN_VALUE && currentChar == '/'
						&& (slashSlashComments || slashStarComments)) {
					currentChar = sourceReader.read();
					if (currentChar == '*' && slashStarComments) {
						int prevChar = 0;
						while ((currentChar = sourceReader.read()) != '/' || prevChar != '*') {
							if (currentChar == '\r') {

								currentChar = sourceReader.read();
								if (currentChar == '\n') {
									currentChar = sourceReader.read();
								}
							} else {
								if (currentChar == '\n') {

									currentChar = sourceReader.read();
								}
							}
							if (currentChar < 0)
								break;
							prevChar = currentChar;
						}
						continue;
					} else if (currentChar == '/' && slashSlashComments) {
						while ((currentChar = sourceReader.read()) != '\n' && currentChar != '\r' && currentChar >= 0)
							;
					}
				} else if (contextMarker == Character.MIN_VALUE && currentChar == '#' && hashComments) {
					// Slurp up everything until the newline
					while ((currentChar = sourceReader.read()) != '\n' && currentChar != '\r' && currentChar >= 0)
						;
				} else if (contextMarker == Character.MIN_VALUE && currentChar == '-' && dashDashComments) {
					currentChar = sourceReader.read();

					if (currentChar == -1 || currentChar != '-') {
						buf.append('-');

						if (currentChar != -1) {
							buf.append(currentChar);
						}

						continue;
					}

					// Slurp up everything until the newline

					while ((currentChar = sourceReader.read()) != '\n' && currentChar != '\r' && currentChar >= 0)
						;
				}

				if (currentChar != -1) {
					buf.append((char) currentChar);
				}
			}
		} catch (IOException ioEx) {
			// we'll never see this from a StringReader
		}

		return buf.toString();
	}

	/**
	 * 是否以suffix串结尾，忽略大小写
	 *
	 * @param s
	 * @param suffix
	 * @return
	 */
	public static boolean endsWithIgnoreCase(String s, String suffix) {
		return s.regionMatches(true, s.length() - suffix.length(), suffix, 0, suffix.length());
	}

	/**
	 * 忽略大小写替换字符串
	 *
	 * @param s 原始字符串
	 * @param oldPattern 要替换的字符串的正则表达式
	 * @param newPattern 新字符串
	 * @return 返回替换后的字符串
	 */
	public static String replaceIgnoreCase(String s, String oldPattern, String newPattern) {
		return Pattern.compile(oldPattern, Pattern.CASE_INSENSITIVE).matcher(s).replaceAll(newPattern);
	}

	//String.split得到的每个子串没有去掉空格，另外如果有连续两个分隔符排在一起如"a,,b"则会得到一个""子串
	public static String[] split(String str, String delimiter) {
		String[] strs = str.split(delimiter);
		ArrayList<String> list = new ArrayList<String>(strs.length);

		for (String s : strs) {
			if (s != null && (s = s.trim()).length() > 0) {
				list.add(s);
			}
		}
		return list.toArray(new String[0]);
	}


	/**
	 * 简单地检查是否是逻辑表与具体子表的关系。子表名满足父表名+"_数字";
	 * @param fatherTable
	 * @param sonTable
	 * @return
	 */
	public static boolean isTableFatherAndSon(String fatherTable, String sonTable){
		if(fatherTable == null || fatherTable.trim().isEmpty() ||
				sonTable == null || sonTable.trim().isEmpty()) {
			return false;
		}
		if(!sonTable.startsWith(fatherTable) || fatherTable.length()+2 > sonTable.length() ){
			return false;
		}
		String suffix = sonTable.substring(fatherTable.length());
		if(suffix.matches("_[\\d]+")){
			return true;
		}
		return false;

	}
}
