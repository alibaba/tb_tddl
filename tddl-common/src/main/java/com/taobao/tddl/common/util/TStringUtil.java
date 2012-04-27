/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * TDDL专用的字符处理便捷类
 * 
 * @author linxuan
 * 
 */
public class TStringUtil {
	/**
	 * 获得第一个start，end之间的字串， 不包括start，end本身。返回值已做了trim
	 */
	public static String getBetween(String sql, String start, String end) {
		int index0 = sql.indexOf(start);
		if (index0 == -1) {
			return null;
		}
		int index1 = sql.indexOf(end, index0);
		if (index1 == -1) {
			return null;
		}
		return sql.substring(index0 + start.length(), index1).trim();
	}

	/**
	 * 只做一次切分
	 * @param str
	 * @param splitor
	 * @return
	 */
	public static String[] twoPartSplit(String str, String splitor) {
		if (splitor != null) {
			int index = str.indexOf(splitor);
			if(index!=-1){
			    String first = str.substring(0, index);
			    String sec = str.substring(index + splitor.length());
		        return new String[]{first,sec};
			}else{
				return new String[] { str };
			}
		} else {
			return new String[] { str };
		}
	}
	
	public static List<String> split(String str,String splitor){
		List<String> re=new ArrayList<String>();
		String[] strs=twoPartSplit(str,splitor);
		if(strs.length==2){
			re.add(strs[0]);
			re.addAll(split(strs[1],splitor));
		}else{
			re.add(strs[0]);
		}
		return re;
	}
	
	public static void main(String[] args){
		String test="sdfsdfsdfs liqiangsdfsdfwerfsdfliqiang woshi whaosdf";
		List<String> strs=split(test,"liqiang");
		for(String str:strs){
			System.out.println(str);
		}
	}
	
	/**
	 * 去除第一个start,end之间的字符串，包括start,end本身
	 * 
	 * @param sql
	 * @param start
	 * @param end
	 * @return
	 */
	public static String removeBetweenWithSplitor(String sql, String start,
			String end) {
		int index0 = sql.indexOf(start);
		if (index0 == -1) {
			return sql;
		}
		int index1 = sql.indexOf(end, index0);
		if (index1 == -1) {
			return sql;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(sql.substring(0, index0));
		sb.append(" ");
		sb.append(sql.substring(index1 + end.length()));
		return sb.toString();
	}

	/**
	 * 将所有/t/s/n等空白符全部替换为空格，并且去除多余空白 各种不同实现的比较测试，参见：TStringUtilTest
	 */
	public static String fillTabWithSpace(String str) {
		if (str == null) {
			return null;
		}

		str = str.trim();
		int sz = str.length();
		StringBuilder buffer = new StringBuilder(sz);

		int index = 0, index0 = -1, index1 = -1;
		for (int i = 0; i < sz; i++) {
			char c = str.charAt(i);
			if (!Character.isWhitespace(c)) {
				if (index0 != -1) {
					// if (!(index0 == index1 && str.charAt(i - 1) == ' ')) {
					if (index0 != index1 || str.charAt(i - 1) != ' ') {
						buffer.append(str.substring(index, index0)).append(" ");
						index = index1 + 1;
					}
				}
				index0 = index1 = -1;
			} else {
				if (index0 == -1) {
					index0 = index1 = i; // 第一个空白
				} else {
					index1 = i;
				}
			}
		}

		buffer.append(str.substring(index));

		return buffer.toString();
	}		/**     * 比较两个字符串（大小写敏感）。     * <pre>     * StringUtil.equals(null, null)   = true     * StringUtil.equals(null, "abc")  = false     * StringUtil.equals("abc", null)  = false     * StringUtil.equals("abc", "abc") = true     * StringUtil.equals("abc", "ABC") = false     * </pre>     *     * @param str1 要比较的字符串1     * @param str2 要比较的字符串2     *     * @return 如果两个字符串相同，或者都是<code>null</code>，则返回<code>true</code>     */    public static boolean equals(String str1, String str2) {        if (str1 == null) {            return str2 == null;        }        return str1.equals(str2);    }	/**     * 取得指定分隔符的前两次出现之间的子串。     *      * <p>     * 如果字符串为<code>null</code>，则返回<code>null</code>。 如果分隔子串为<code>null</code>，则返回<code>null</code>。     * <pre>     * StringUtil.substringBetween(null, *)            = null     * StringUtil.substringBetween("", "")             = ""     * StringUtil.substringBetween("", "tag")          = null     * StringUtil.substringBetween("tagabctag", null)  = null     * StringUtil.substringBetween("tagabctag", "")    = ""     * StringUtil.substringBetween("tagabctag", "tag") = "abc"     * </pre>     * </p>     *     * @param str 字符串     * @param tag 要搜索的分隔子串     *     * @return 子串，如果原始串为<code>null</code>或未找到分隔子串，则返回<code>null</code>     */    public static String substringBetween(String str, String tag) {        return substringBetween(str, tag, tag, 0);    }    /**     * 取得两个分隔符之间的子串。     *      * <p>     * 如果字符串为<code>null</code>，则返回<code>null</code>。 如果分隔子串为<code>null</code>，则返回<code>null</code>。     * <pre>     * StringUtil.substringBetween(null, *, *)          = null     * StringUtil.substringBetween("", "", "")          = ""     * StringUtil.substringBetween("", "", "tag")       = null     * StringUtil.substringBetween("", "tag", "tag")    = null     * StringUtil.substringBetween("yabcz", null, null) = null     * StringUtil.substringBetween("yabcz", "", "")     = ""     * StringUtil.substringBetween("yabcz", "y", "z")   = "abc"     * StringUtil.substringBetween("yabczyabcz", "y", "z")   = "abc"     * </pre>     * </p>     *     * @param str 字符串     * @param open 要搜索的分隔子串1     * @param close 要搜索的分隔子串2     *     * @return 子串，如果原始串为<code>null</code>或未找到分隔子串，则返回<code>null</code>     */    public static String substringBetween(String str, String open, String close) {        return substringBetween(str, open, close, 0);    }    /**     * 取得两个分隔符之间的子串。     *      * <p>     * 如果字符串为<code>null</code>，则返回<code>null</code>。 如果分隔子串为<code>null</code>，则返回<code>null</code>。     * <pre>     * StringUtil.substringBetween(null, *, *)          = null     * StringUtil.substringBetween("", "", "")          = ""     * StringUtil.substringBetween("", "", "tag")       = null     * StringUtil.substringBetween("", "tag", "tag")    = null     * StringUtil.substringBetween("yabcz", null, null) = null     * StringUtil.substringBetween("yabcz", "", "")     = ""     * StringUtil.substringBetween("yabcz", "y", "z")   = "abc"     * StringUtil.substringBetween("yabczyabcz", "y", "z")   = "abc"     * </pre>     * </p>     *     * @param str 字符串     * @param open 要搜索的分隔子串1     * @param close 要搜索的分隔子串2     * @param fromIndex 从指定index处搜索     *     * @return 子串，如果原始串为<code>null</code>或未找到分隔子串，则返回<code>null</code>     */    public static String substringBetween(String str, String open, String close, int fromIndex) {        if ((str == null) || (open == null) || (close == null)) {            return null;        }        int start = str.indexOf(open, fromIndex);        if (start != -1) {            int end = str.indexOf(close, start + open.length());            if (end != -1) {                return str.substring(start + open.length(), end);            }        }        return null;    }    	 /**     * 取得长度为指定字符数的最右边的子串。     * <pre>     * StringUtil.right(null, *)    = null     * StringUtil.right(*, -ve)     = ""     * StringUtil.right("", *)      = ""     * StringUtil.right("abc", 0)   = ""     * StringUtil.right("abc", 2)   = "bc"     * StringUtil.right("abc", 4)   = "abc"     * </pre>     *     * @param str 字符串     * @param len 最右子串的长度     *     * @return 子串，如果原始字串为<code>null</code>，则返回<code>null</code>     */    public static String right(String str, int len) {        if (str == null) {            return null;        }        if (len < 0) {            return EMPTY_STRING;        }        if (str.length() <= len) {            return str;        } else {            return str.substring(str.length() - len);        }    }    	  /**     * 取得长度为指定字符数的最左边的子串。     * <pre>     * StringUtil.left(null, *)    = null     * StringUtil.left(*, -ve)     = ""     * StringUtil.left("", *)      = ""     * StringUtil.left("abc", 0)   = ""     * StringUtil.left("abc", 2)   = "ab"     * StringUtil.left("abc", 4)   = "abc"     * </pre>     *     * @param str 字符串     * @param len 最左子串的长度     *     * @return 子串，如果原始字串为<code>null</code>，则返回<code>null</code>     */    public static String left(String str, int len) {        if (str == null) {            return null;        }        if (len < 0) {            return EMPTY_STRING;        }        if (str.length() <= len) {            return str;        } else {            return str.substring(0, len);        }    }    	/**     * 判断字符串是否只包含unicode数字。     *      * <p>     * <code>null</code>将返回<code>false</code>，空字符串<code>""</code>将返回<code>true</code>。     * </p>     * <pre>     * StringUtil.isNumeric(null)   = false     * StringUtil.isNumeric("")     = true     * StringUtil.isNumeric("  ")   = false     * StringUtil.isNumeric("123")  = true     * StringUtil.isNumeric("12 3") = false     * StringUtil.isNumeric("ab2c") = false     * StringUtil.isNumeric("12-3") = false     * StringUtil.isNumeric("12.3") = false     * </pre>     *     * @param str 要检查的字符串     *     * @return 如果字符串非<code>null</code>并且全由unicode数字组成，则返回<code>true</code>     */    public static boolean isNumeric(String str) {        if (str == null) {            return false;        }        int length = str.length();        for (int i = 0; i < length; i++) {            if (!Character.isDigit(str.charAt(i))) {                return false;            }        }        return true;    }    	  /** 空字符串。 */    public static final String EMPTY_STRING = "";        /**     * 取得最后一个的分隔子串之前的子串。     *      * <p>     * 如果字符串为<code>null</code>，则返回<code>null</code>。 如果分隔子串为<code>null</code>或未找到该子串，则返回原字符串。     * <pre>     * StringUtil.substringBeforeLast(null, *)      = null     * StringUtil.substringBeforeLast("", *)        = ""     * StringUtil.substringBeforeLast("abcba", "b") = "abc"     * StringUtil.substringBeforeLast("abc", "c")   = "ab"     * StringUtil.substringBeforeLast("a", "a")     = ""     * StringUtil.substringBeforeLast("a", "z")     = "a"     * StringUtil.substringBeforeLast("a", null)    = "a"     * StringUtil.substringBeforeLast("a", "")      = "a"     * </pre>     * </p>     *     * @param str 字符串     * @param separator 要搜索的分隔子串     *     * @return 子串，如果原始串为<code>null</code>，则返回<code>null</code>     */    public static String substringBeforeLast(String str, String separator) {        if ((str == null) || (separator == null) || (str.length() == 0)                    || (separator.length() == 0)) {            return str;        }        int pos = str.lastIndexOf(separator);        if (pos == -1) {            return str;        }        return str.substring(0, pos);    }    /**     * 取得最后一个的分隔子串之后的子串。     *      * <p>     * 如果字符串为<code>null</code>，则返回<code>null</code>。 如果分隔子串为<code>null</code>或未找到该子串，则返回原字符串。     * <pre>     * StringUtil.substringAfterLast(null, *)      = null     * StringUtil.substringAfterLast("", *)        = ""     * StringUtil.substringAfterLast(*, "")        = ""     * StringUtil.substringAfterLast(*, null)      = ""     * StringUtil.substringAfterLast("abc", "a")   = "bc"     * StringUtil.substringAfterLast("abcba", "b") = "a"     * StringUtil.substringAfterLast("abc", "c")   = ""     * StringUtil.substringAfterLast("a", "a")     = ""     * StringUtil.substringAfterLast("a", "z")     = ""     * </pre>     * </p>     *     * @param str 字符串     * @param separator 要搜索的分隔子串     *     * @return 子串，如果原始串为<code>null</code>，则返回<code>null</code>     */    public static String substringAfterLast(String str, String separator) {        if ((str == null) || (str.length() == 0)) {            return str;        }        if ((separator == null) || (separator.length() == 0)) {            return EMPTY_STRING;        }        int pos = str.lastIndexOf(separator);        if ((pos == -1) || (pos == (str.length() - separator.length()))) {            return EMPTY_STRING;        }        return str.substring(pos + separator.length());    }        /**     * 取得第一个出现的分隔子串之后的子串。     *      * <p>     * 如果字符串为<code>null</code>，则返回<code>null</code>。 如果分隔子串为<code>null</code>或未找到该子串，则返回原字符串。     * <pre>     * StringUtil.substringAfter(null, *)      = null     * StringUtil.substringAfter("", *)        = ""     * StringUtil.substringAfter(*, null)      = ""     * StringUtil.substringAfter("abc", "a")   = "bc"     * StringUtil.substringAfter("abcba", "b") = "cba"     * StringUtil.substringAfter("abc", "c")   = ""     * StringUtil.substringAfter("abc", "d")   = ""     * StringUtil.substringAfter("abc", "")    = "abc"     * </pre>     * </p>     *     * @param str 字符串     * @param separator 要搜索的分隔子串     *     * @return 子串，如果原始串为<code>null</code>，则返回<code>null</code>     */    public static String substringAfter(String str, String separator) {        if ((str == null) || (str.length() == 0)) {            return str;        }        if (separator == null) {            return EMPTY_STRING;        }        int pos = str.indexOf(separator);        if (pos == -1) {            return EMPTY_STRING;        }        return str.substring(pos + separator.length());    }    	/* ============================================================================ */    /*  搜索并取子串函数。                                                          */    /* ============================================================================ */    /**     * 取得第一个出现的分隔子串之前的子串。     *      * <p>     * 如果字符串为<code>null</code>，则返回<code>null</code>。 如果分隔子串为<code>null</code>或未找到该子串，则返回原字符串。     * <pre>     * StringUtil.substringBefore(null, *)      = null     * StringUtil.substringBefore("", *)        = ""     * StringUtil.substringBefore("abc", "a")   = ""     * StringUtil.substringBefore("abcba", "b") = "a"     * StringUtil.substringBefore("abc", "c")   = "ab"     * StringUtil.substringBefore("abc", "d")   = "abc"     * StringUtil.substringBefore("abc", "")    = ""     * StringUtil.substringBefore("abc", null)  = "abc"     * </pre>     * </p>     *     * @param str 字符串     * @param separator 要搜索的分隔子串     *     * @return 子串，如果原始串为<code>null</code>，则返回<code>null</code>     */    public static String substringBefore(String str, String separator) {        if ((str == null) || (separator == null) || (str.length() == 0)) {            return str;        }        if (separator.length() == 0) {            return EMPTY_STRING;        }        int pos = str.indexOf(separator);        if (pos == -1) {            return str;        }        return str.substring(0, pos);    }		  /**     * 检查字符串中是否包含指定的字符串。如果字符串为<code>null</code>，将返回<code>false</code>。     * <pre>     * StringUtil.contains(null, *)     = false     * StringUtil.contains(*, null)     = false     * StringUtil.contains("", "")      = true     * StringUtil.contains("abc", "")   = true     * StringUtil.contains("abc", "a")  = true     * StringUtil.contains("abc", "z")  = false     * </pre>     *     * @param str 要扫描的字符串     * @param searchStr 要查找的字符串     *     * @return 如果找到，则返回<code>true</code>     */    public static boolean contains(String str, String searchStr) {        if ((str == null) || (searchStr == null)) {            return false;        }        return str.indexOf(searchStr) >= 0;    }	 /* ============================================================================ */    /*  去空白（或指定字符）的函数。                                                */    /*                                                                              */    /*  以下方法用来除去一个字串中的空白或指定字符。                                */    /* ============================================================================ */    /**     * 除去字符串头尾部的空白，如果字符串是<code>null</code>，依然返回<code>null</code>。     *      * <p>     * 注意，和<code>String.trim</code>不同，此方法使用<code>Character.isWhitespace</code>来判定空白，     * 因而可以除去英文字符集之外的其它空白，如中文空格。     * <pre>     * StringUtil.trim(null)          = null     * StringUtil.trim("")            = ""     * StringUtil.trim("     ")       = ""     * StringUtil.trim("abc")         = "abc"     * StringUtil.trim("    abc    ") = "abc"     * </pre>     * </p>     *     * @param str 要处理的字符串     *     * @return 除去空白的字符串，如果原字串为<code>null</code>，则返回<code>null</code>     */    public static String trim(String str) {        return trim(str, null, 0);    }    	   /**     * 除去字符串头尾部的指定字符，如果字符串是<code>null</code>，依然返回<code>null</code>。     * <pre>     * StringUtil.trim(null, *)          = null     * StringUtil.trim("", *)            = ""     * StringUtil.trim("abc", null)      = "abc"     * StringUtil.trim("  abc", null)    = "abc"     * StringUtil.trim("abc  ", null)    = "abc"     * StringUtil.trim(" abc ", null)    = "abc"     * StringUtil.trim("  abcyx", "xyz") = "  abc"     * </pre>     *     * @param str 要处理的字符串     * @param stripChars 要除去的字符，如果为<code>null</code>表示除去空白字符     * @param mode <code>-1</code>表示trimStart，<code>0</code>表示trim全部，<code>1</code>表示trimEnd     *     * @return 除去指定字符后的的字符串，如果原字串为<code>null</code>，则返回<code>null</code>     */    private static String trim(String str, String stripChars, int mode) {        if (str == null) {            return null;        }        int length = str.length();        int start = 0;        int end   = length;        // 扫描字符串头部        if (mode <= 0) {            if (stripChars == null) {                while ((start < end) && (Character.isWhitespace(str.charAt(start)))) {                    start++;                }            } else if (stripChars.length() == 0) {                return str;            } else {                while ((start < end) && (stripChars.indexOf(str.charAt(start)) != -1)) {                    start++;                }            }        }        // 扫描字符串尾部        if (mode >= 0) {            if (stripChars == null) {                while ((start < end) && (Character.isWhitespace(str.charAt(end - 1)))) {                    end--;                }            } else if (stripChars.length() == 0) {                return str;            } else {                while ((start < end) && (stripChars.indexOf(str.charAt(end - 1)) != -1)) {                    end--;                }            }        }        if ((start > 0) || (end < length)) {            return str.substring(start, end);        }        return str;    }    	 /**     * 检查字符串是否不是空白：<code>null</code>、空字符串<code>""</code>或只有空白字符。     * <pre>     * StringUtil.isBlank(null)      = false     * StringUtil.isBlank("")        = false     * StringUtil.isBlank(" ")       = false     * StringUtil.isBlank("bob")     = true     * StringUtil.isBlank("  bob  ") = true     * </pre>     *     * @param str 要检查的字符串     *     * @return 如果为空白, 则返回<code>true</code>     */    public static boolean isNotBlank(String str) {        int length;        if ((str == null) || ((length = str.length()) == 0)) {            return false;        }        for (int i = 0; i < length; i++) {            if (!Character.isWhitespace(str.charAt(i))) {                return true;            }        }        return false;    }		  /**     * 检查字符串是否是空白：<code>null</code>、空字符串<code>""</code>或只有空白字符。     * <pre>     * StringUtil.isBlank(null)      = true     * StringUtil.isBlank("")        = true     * StringUtil.isBlank(" ")       = true     * StringUtil.isBlank("bob")     = false     * StringUtil.isBlank("  bob  ") = false     * </pre>     *     * @param str 要检查的字符串     *     * @return 如果为空白, 则返回<code>true</code>     */    public static boolean isBlank(String str) {        int length;        if ((str == null) || ((length = str.length()) == 0)) {            return true;        }        for (int i = 0; i < length; i++) {            if (!Character.isWhitespace(str.charAt(i))) {                return false;            }        }        return true;    }		   /**     * 将字符串按指定字符分割。     *      * <p>     * 分隔符不会出现在目标数组中，连续的分隔符就被看作一个。如果字符串为<code>null</code>，则返回<code>null</code>。     * <pre>     * StringUtil.split(null, *)                = null     * StringUtil.split("", *)                  = []     * StringUtil.split("abc def", null)        = ["abc", "def"]     * StringUtil.split("abc def", " ")         = ["abc", "def"]     * StringUtil.split("abc  def", " ")        = ["abc", "def"]     * StringUtil.split(" ab:  cd::ef  ", ":")  = ["ab", "cd", "ef"]     * StringUtil.split("abc.def", "")          = ["abc.def"]     *  </pre>     * </p>     *     * @param str 要分割的字符串     * @param separatorChars 分隔符     *     * @return 分割后的字符串数组，如果原字符串为<code>null</code>，则返回<code>null</code>     */    public static String[] splitm(String str, String separatorChars) {        return split(str, separatorChars, -1);    }    public static final String[] EMPTY_STRING_ARRAY = new String[0];        /**     * 将字符串按指定字符分割。     *      * <p>     * 分隔符不会出现在目标数组中，连续的分隔符就被看作一个。如果字符串为<code>null</code>，则返回<code>null</code>。     * <pre>     * StringUtil.split(null, *, *)                 = null     * StringUtil.split("", *, *)                   = []     * StringUtil.split("ab cd ef", null, 0)        = ["ab", "cd", "ef"]     * StringUtil.split("  ab   cd ef  ", null, 0)  = ["ab", "cd", "ef"]     * StringUtil.split("ab:cd::ef", ":", 0)        = ["ab", "cd", "ef"]     * StringUtil.split("ab:cd:ef", ":", 2)         = ["ab", "cdef"]     * StringUtil.split("abc.def", "", 2)           = ["abc.def"]     * </pre>     * </p>     *     * @param str 要分割的字符串     * @param separatorChars 分隔符     * @param max 返回的数组的最大个数，如果小于等于0，则表示无限制     *     * @return 分割后的字符串数组，如果原字符串为<code>null</code>，则返回<code>null</code>     */    public static String[] split(String str, String separatorChars, int max) {        if (str == null) {            return null;        }        int length = str.length();        if (length == 0) {            return EMPTY_STRING_ARRAY;        }        List    list      = new ArrayList();        int     sizePlus1 = 1;        int     i         = 0;        int     start     = 0;        boolean match     = false;        if (separatorChars == null) {            // null表示使用空白作为分隔符            while (i < length) {                if (Character.isWhitespace(str.charAt(i))) {                    if (match) {                        if (sizePlus1++ == max) {                            i = length;                        }                        list.add(str.substring(start, i));                        match = false;                    }                    start = ++i;                    continue;                }                match = true;                i++;            }        } else if (separatorChars.length() == 1) {            // 优化分隔符长度为1的情形            char sep = separatorChars.charAt(0);            while (i < length) {                if (str.charAt(i) == sep) {                    if (match) {                        if (sizePlus1++ == max) {                            i = length;                        }                        list.add(str.substring(start, i));                        match = false;                    }                    start = ++i;                    continue;                }                match = true;                i++;            }        } else {            // 一般情形            while (i < length) {                if (separatorChars.indexOf(str.charAt(i)) >= 0) {                    if (match) {                        if (sizePlus1++ == max) {                            i = length;                        }                        list.add(str.substring(start, i));                        match = false;                    }                    start = ++i;                    continue;                }                match = true;                i++;            }        }        if (match) {            list.add(str.substring(start, i));        }        return (String[]) list.toArray(new String[list.size()]);    }
}
