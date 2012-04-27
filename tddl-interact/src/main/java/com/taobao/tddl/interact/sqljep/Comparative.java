/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.sqljep;

import java.util.List;
import java.util.Map;

import com.taobao.tddl.common.sqlobjecttree.SQLFragment;
import com.taobao.tddl.common.sqlobjecttree.SubQueryValue;
import com.taobao.tddl.common.sqlobjecttree.Value;

/**
 * 可比较的类 实际上是是两个东西的结合
 * 
 * 符号+值
 * 
 * 例如 [> 1] , [< 1] , [= 1]
 * 
 * @author shenxun
 * 
 */
public class Comparative implements Comparable, Cloneable {

	public static final int GreaterThan = 1;
	public static final int GreaterThanOrEqual = 2;
	public static final int Equivalent = 3;
	public static final int Like = 4;
	public static final int NotLike = 5;
	public static final int NotEquivalent = 6;
	public static final int LessThan = 7;
	public static final int LessThanOrEqual = 8;

	/**
	 * 表达式取反
	 * 
	 * @param function
	 * @return
	 */
	public static int reverseComparison(int function) {
		return 9 - function;
	}

	/**
	 * 表达式前后位置调换的时候
	 * 
	 * @param function
	 * @return
	 */
	public static int exchangeComparison(int function) {
		if (function == GreaterThan) {
			return LessThan;
		} else if (function == GreaterThanOrEqual) {
			return LessThanOrEqual;
		} else if (function == LessThan) {
			return GreaterThan;
		}
		if (function == LessThanOrEqual) {
			return GreaterThanOrEqual;
		} else {
			return function;
		}
	}

	private Comparable value; // 这有可能又是个Comparative，从而实质上表示一课树（比较树）
	private int comparison;

	protected Comparative() {
	}

	public Comparative(int function, Comparable value) {
		this.comparison = function;
		this.value = value;
	}

	public Comparable getValue() {
		return value;
	}

	public void setComparison(int function) {
		this.comparison = function;
	}

	public static String getComparisonName(int function) {
		if (function == Equivalent) {
			return "=";
		} else if (function == GreaterThan) {
			return ">";
		} else if (function == GreaterThanOrEqual) {
			return ">=";
		} else if (function == LessThanOrEqual) {
			return "<=";
		} else if (function == LessThan) {
			return "<";
		} else if (function == NotEquivalent) {
			return "<>";
		} else if (function == Like) {
			return "LIKE";
		} else if (function == NotLike) {
			return "NOT LIKE";
		} else {
			return null;
		}
	}

	public static int getComparisonByIdent(String ident) {
		if ("=".equals(ident)) {
			return Equivalent;
		} else if (">".equals(ident)) {
			return GreaterThan;
		} else if (">=".equals(ident)) {
			return GreaterThanOrEqual;
		} else if ("<=".equals(ident)) {
			return LessThanOrEqual;
		} else if ("<".equals(ident)) {
			return LessThan;
		} else if ("!=".equals(ident)) {
			return NotEquivalent;
		} else if ("<>".equals(ident)) {
			return NotEquivalent;
		} else if ("like".equalsIgnoreCase(ident)) {
			return Like;
		} else {
			return -1;
		}
	}

	/**
	 * contains顺序按字符从多到少排列，否则逻辑不对，这里
	 * 先这样处理。
	 * @param completeStr
	 * @return
	 */
	public static int getComparisonByCompleteString(String completeStr) {
		if (completeStr != null) {
			String ident = completeStr.toLowerCase();
			if (ident.contains(">=")) {
				return GreaterThanOrEqual;
			}else if(ident.contains("<=")){
				return LessThanOrEqual;
			}else if(ident.contains("!=")){
				return NotEquivalent;
			}else if(ident.contains("<>")){
				return NotEquivalent;
			}else if(ident.contains("=")){
				return Equivalent;
			}else if(ident.contains(">")){
				return GreaterThan;
			}else if(ident.contains("<")){
				return LessThan;
			}else if(ident.contains("like")){
				return Like;
			}else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	public int getComparison() {
		return comparison;
	}

	public void setValue(Comparable value) {
		this.value = value;
	}

	public int compareTo(Object o) {
		if (o instanceof Comparative) {
			Comparative other = (Comparative) o;
			return this.getValue().compareTo(other.getValue());
		} else if (o instanceof Comparable) {
			return this.getValue().compareTo(o);
		}
		return -1;
	}

	public String toString() {
		if (value != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("(").append(getComparisonName(comparison));
			sb.append(value.toString()).append(")");
			return sb.toString();
		} else {
			return null;
		}
	}
	
	/**
	 * 将变量绑定到当前持有的Comparable(Comparative)中。
	 * 
	 * @param arguments
	 * @param aliasMap
	 * @return
	 */
	public Comparative getVal(List<Object> arguments,Map<String, SQLFragment> aliasMap) {
		Comparable<?> comp=null;
		if(value instanceof Value){
			if(value instanceof SubQueryValue){
				((SubQueryValue) value).setAliasMap(aliasMap);
				comp=((Value)value).getVal(arguments);
				//引擎不支持在Comparative包装内的ComparativeOR,因此在a.id=b.id这种映射中要显示的做一个hack.
				if(comp instanceof ComparativeOR){
					return (ComparativeOR)comp;
				}
			}
			
			comp=((Value) value).getVal(arguments);
			
		}else if(value instanceof Comparative){
			comp= new Comparative(((Comparative) value).getComparison(),((Comparative) value).getVal(arguments,aliasMap));
		}else{
			comp=value;
		}
		return new Comparative(this.comparison, comp);
	}

	public Object clone() {
		return new Comparative(this.comparison, this.value);
	}
}
