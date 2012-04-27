/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.hintparsercommon;

import java.util.HashMap;import java.util.Map;import com.taobao.tddl.common.util.TStringUtil;
/**
 * hint解析器的基础实现。 为了将hint和正常注释加以区分，这里添加一个特殊标记“+”。形如:(见下)
 * hint必须出现在所有sql的最前面，如果不是在sql最前面出现，则不算为tddl hint
 * 
 * @author whisper
 * 
 */
/* + hint1=value;hint2=value;hint3=value */
public class TDDLHintParser {
	/**
	 * 将一段tddl注释解析为一个string map. 参数如下面的例子:（因为注释的问题，直接写在方法上面的例子上了)
	 * 
	 * @param sql
	 * @return
	 */
	/* + hint1:value;hint2:value;hint3:value */
	public static Map<String, String> parseHint(String sqlHint) {
		//剥离注释中的/*+和*/
		sqlHint = TStringUtil.substringBetween(sqlHint, "/*+", "*/");
		//这里使用StringUtil带来的副作用是如果有 sql = xx ;; b = uu ;则中间的两个分割;;会被识别为一个，但可以接受
		String[] hints = TStringUtil.splitm(sqlHint,";");
		Map<String, String> hintMap = new HashMap<String, String>(hints.length);
		for(String hint : hints){
			if(hint == null){
				throw new IllegalArgumentException("hint is null");
			}else{
				String[] pair = TStringUtil.splitm(hint,":");
				if(pair.length != 2){
					throw new IllegalArgumentException("参数个数错误，键值对不为2;"+hint);
				}
				String key = pair[0];
				key = key.trim();
				String value = pair[1];
				value = value.trim();
				hintMap.put(key, value);
			}
		}
		return hintMap;
	}
	/**
	 * 用于判断是否包含TDDL hint
	 * @param sql
	 * @return
	 */
	public static boolean containTDDLHint(String sql){
		if(sql == null){
			return false;
		}
		return sql.trim().startsWith("/*+");
	}
	
	 /**
     * 取得两个分隔符之间的子串。
     * 
     * <p>
     * 如果字符串为<code>null</code>，则返回<code>null</code>。 如果分隔子串为<code>null</code>，则返回<code>null</code>。
     * <pre>
     * StringUtil.substringBetween(null, *, *)          = null
     * StringUtil.substringBetween("", "", "")          = ""
     * StringUtil.substringBetween("", "", "tag")       = null
     * StringUtil.substringBetween("", "tag", "tag")    = null
     * StringUtil.substringBetween("yabcz", null, null) = null
     * StringUtil.substringBetween("yabcz", "", "")     = ""
     * StringUtil.substringBetween("yabcz", "y", "z")   = "abc"
     * StringUtil.substringBetween("yabczyabcz", "y", "z")   = "abc"
     * </pre>
     * </p>
     *
     * @param str 字符串
     * @param open 要搜索的分隔子串1
     * @param close 要搜索的分隔子串2
     *
     * @return 子串，如果原始串为<code>null</code>或未找到分隔子串，则返回<code>null</code>
     */
	 static String substringBetween(String str, String open, String close) {
		return substringBetween(str, open, close, 0);
	}
	/**
	 * 将sql中的hint删除掉。
	 * 
	 * @param sql
	 * @return
	 */
	public static String removeHint(String sql){
		if(containTDDLHint(sql)){
			int index = sql.indexOf("*/");
			//加上*/的两个字符
			return sql.substring(index+2);
		}
		return sql;
	}
	 public static void main(String[] args) {
		long l = System.currentTimeMillis();
		String s = "/*+ db:{db1} */sql";
		for(int i = 0 ; i < 50000 ; i ++){
			removeHint(s);
		}
		System.out.println(System.currentTimeMillis() - l);
//		System.out.println(removeHint(s));
	}
	/**
     * 取得两个分隔符之间的子串。
     * 
     * <p>
     * 如果字符串为<code>null</code>，则返回<code>null</code>。 如果分隔子串为<code>null</code>，则返回<code>null</code>。
     * <pre>
     * StringUtil.substringBetween(null, *, *)          = null
     * StringUtil.substringBetween("", "", "")          = ""
     * StringUtil.substringBetween("", "", "tag")       = null
     * StringUtil.substringBetween("", "tag", "tag")    = null
     * StringUtil.substringBetween("yabcz", null, null) = throw IllegalArgumentException
     * StringUtil.substringBetween("yabcz", "", "")     = ""
     * StringUtil.substringBetween("yabcz", "y", "z")   = "abc"
     * StringUtil.substringBetween("yabczyabcz", "y", "z")   = "abc"
     * </pre>
     * </p>
     *
     * @param str 字符串
     * @param open 要搜索的分隔子串1
     * @param close 要搜索的分隔子串2
     * @param fromIndex 从指定index处搜索
     *
     * @return 子串，如果原始串为<code>null</code>或未找到分隔子串，则返回<code>null</code>
     * @throws IllegalArgumentException 如果没有找到open标签或者close标签
     */
	private static String substringBetween(String str, String open,
			String close, int fromIndex) {
		if ((str == null) || (open == null) || (close == null)) {
			return null;
		}
		str = str.trim();
		int start = str.indexOf(open, fromIndex);

		if (start != -1) {
			int end = str.indexOf(close, start + open.length());

			if (end != -1) {
				return str.substring(start + open.length(), end);
			}else{
				throw new IllegalArgumentException("can't find end :"+close);
			}
		}else{
			throw new IllegalArgumentException("can't find start :"+open);
		}
	}

}
