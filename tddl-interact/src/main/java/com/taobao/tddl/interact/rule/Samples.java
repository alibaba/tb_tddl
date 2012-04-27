/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 一个描点集合的抽象。支持线程不安全的笛卡尔积迭代遍历。一个Sample表示一个笛卡尔积抽样
 * 将多列独立的枚举值，用特殊的遍历方法，转换为笛卡尔积抽样（即不同列值的组合）
 * 一个Sample用一个Map<String, Object>表示，key包含了各列，value对应每列的一个取值
 * 
 * @author linxuan
 *
 */
public class Samples implements Iterable<Map<String/*列名*/, Object/*列值*/>>, Iterator<Map<String, Object>> {
	private final Map<String, Set<Object>> columnEnumerates;
	private final String[] subColums; //使用哪几列，便于做sub
	private final Set<String> subColumSet;//与subColums保持一致，便于判,只读

	public Samples(Map<String, Set<Object>> columnEnumerates) {
		this.columnEnumerates = columnEnumerates;
		this.subColums = columnEnumerates.keySet().toArray(new String[columnEnumerates.size()]);
		this.subColumSet = columnEnumerates.keySet();//subColumSet只读，这样应该没问题
	}

	public Samples(Map<String, Set<Object>> columnEnumerates, String[] subColumns) {
		this.columnEnumerates = columnEnumerates;
		this.subColums = subColumns;
		this.subColumSet = new HashSet<String>();
		this.subColumSet.addAll(Arrays.asList(subColumns));
		if (subColumSet.size() != subColums.length) {
			throw new IllegalArgumentException(Arrays.toString(subColumns) + " has duplicate columm");
		}
	}

	public Samples(Set<String> columnNames) {
		this.columnEnumerates = new HashMap<String, Set<Object>>();
		for (String name : columnNames) {
			this.columnEnumerates.put(name, new HashSet<Object>(1));
		}
		this.subColums = columnNames.toArray(new String[columnEnumerates.size()]);
		this.subColumSet = Collections.unmodifiableSet(columnNames);//subColumSet只读
	}

	/**
	 * TODO 评估columnEnumerates共享的风险
	 * @param columns 如果columns包含本对象columnEnumerates中不存在的key，后果不可预期
	 */
	public Samples subSamples(String[] columns) {
		if (columns.length == this.subColums.length)
			return this; //这里就不判读columns是否都和this一致了，有一定风险
		return new Samples(this.columnEnumerates, columns);//可能会使第三层sub由小变大，但是不影响使用。也没有判读一致性
	}
	
	/**
	 * @return 如果subColums和columnEnumerates相同，则直接返回，否则抽取
	 */
	public Map<String, Set<Object>> getColumnEnumerates() {
		if (this.columnEnumerates.size() == subColums.length) {
			return this.columnEnumerates;
		} else {
			Map<String, Set<Object>> res = new HashMap<String, Set<Object>>(subColums.length);
			for (String column : subColums) {
				res.put(column, this.columnEnumerates.get(column));
			}
			return res;
		}
	}
	
	/**
	 * @return 列个数
	 */
	public int size() {
		return this.subColums.length;
	}

	/**
	 * TODO 评估columnEnumerates共享的风险
	 * 合并other到本对象的columnEnumerates，other中的列会覆盖本对象中的列
	 * @return 新的对象，和本对象共享columnEnumerates；所以merge后应该使用返回的对象，而不再使用本对象
	 */
	/*public Samples mergeSamples(Samples other) {
		this.columnEnumerates.putAll(other.columnEnumerates);
		this.subColumSet.addAll(other.subColumSet);
		return new Samples(this.columnEnumerates, this.subColums);
	}*/

	/**
	 * 向一个列添加枚举值
	 */
	public void addEnumerates(String name, Set<Object> values) {
		if (columnEnumerates.containsKey(name)) {
			columnEnumerates.get(name).addAll(values);
		} else {
			throw new IllegalArgumentException(Arrays.toString(subColums) + ", Samples not contain key:" + name);
		}
	}

	/**
	 * 添加一个Sample组合。若某个列名不在本Samples中，则直接抛空指针
	 */
	public void addSample(Map<String, Object> aCartesianSample) {
		for (Map.Entry<String, Object> e : aCartesianSample.entrySet()) {
			columnEnumerates.get(e.getKey()).add(e.getValue());
		}
	}

	/**
	 * 下面是笛卡尔积迭代遍历的实现
	 */
	private Map<String, Object> currentCartesianSample; //currentCartesianProduct当前的笛卡尔值
	private Iterator<Object>[] iterators;//这种方式尾端iterator要反复重新打开，KeyIterator对象会创建比较多。考虑用Object[]加游标
	private int cursor;

	@SuppressWarnings("unchecked")
	public Iterator<Map<String, Object>> iterator() {
		//每次迭代前清空上次迭代状态
		currentCartesianSample = new HashMap<String, Object>(subColums.length);
		iterators = new Iterator[subColums.length];
		int i = cursor = 0;
		for (String name : subColums) {
			iterators[i++] = columnEnumerates.get(name).iterator();
		}
		return this;
	}

	public boolean hasNext() {
		for (Iterator<Object> it : iterators) {
			if (it.hasNext()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 返回结果只能读取。如若修改后果不可预期。
	 * columnSamples每个列的枚举值集合必须至少有一个元素。
	 */
	public Map<String, Object> next() {
		for (;;) {
			if (iterators[cursor].hasNext()) {
				currentCartesianSample.put(subColums[cursor], iterators[cursor].next());
				if (cursor == subColums.length - 1) {
					break;
				} else {
					cursor++;
				}
			} else {
				if (cursor == 0) {
					break; //全部结束了
				} else {
					//重新打开当前的iterator备下一轮用
					iterators[cursor] = columnEnumerates.get(subColums[cursor]).iterator();
					cursor--;
				}
			}
		}
		return currentCartesianSample;
	}

	public void remove() {
		throw new UnsupportedOperationException(getClass().getName() + ".remove()");
	}

	/**
	 * columnEnumerates共享，而且keySet可能和subColums不一致，所以这里要保持返回值语义一致
	 * @return
	 */
	/*public Map<String, Set<Object>> getColumnEnumerates() {
		return columnEnumerates;
	}*/
	public Set<Object> getColumnEnumerates(String name) {
		return columnEnumerates.get(name);
	}

	public Set<String> getSubColumSet() {
		return subColumSet;
	}

	public static class SamplesCtx {
		public final static int merge = 0;
		public final static int replace = 1;
		public final Samples samples;
		public final int dealType;

		public SamplesCtx(Samples commonSamples, int dealType) {
			this.samples = commonSamples;
			this.dealType = dealType;
		}
	}

	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	public String toString() {
		StringBuilder sb = new StringBuilder("Samples{");
		for (String column : this.subColumSet) {
			sb.append(column).append("=[");
			for (Object value : this.columnEnumerates.get(column)) {
				if (value instanceof Calendar) {
					sb.append(df.format(((Calendar) value).getTime())).append(",");
				} else {
					sb.append(value).append(",");
				}
			}
			sb.append("]");
		}
		sb.append("}");
		return sb.toString();
	}

	private static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	private static Map<Character, Integer> rDigits = new HashMap<Character, Integer>(16);
	static {
		for (int i = 0; i < digits.length; ++i) {
			rDigits.put(digits[i], i);
		}
	}

	/**
	 * 将一个字节数组转化为可见的字符串
	 */
	public static String bytes2string(byte[] bt) {
		int l = bt.length;
		char[] out = new char[l << 1];
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = digits[(0xF0 & bt[i]) >>> 4];
			out[j++] = digits[0x0F & bt[i]];
		}
		return new String(out);
	}

	/**
	 * 将字符串转换为bytes
	 */
	public static byte[] string2bytes(String str) {
		if (null == str) {
			throw new NullPointerException("参数不能为空");
		}
		char[] chs = str.toCharArray();
		byte[] data = new byte[chs.length/2];
		for (int i = 0; i < data.length; ++i) {
			int h = rDigits.get(chs[i * 2]).intValue();
			int l = rDigits.get(chs[i * 2 + 1]).intValue();
			data[i] = (byte) ((h & 0x0F) << 4 | (l & 0x0F));
		}
		return data;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		System.out.println(bytes2string("crm_scheme_detail".getBytes("utf-8")));
		byte[] bs = string2bytes("63726D5F736368656D655F64657461696C");
		System.out.println(bytes2string(bs));
		System.out.println(new String(bs,"utf-8"));
		System.out.println("------------------------------");

		bs = string2bytes("63726D5F726566756E645F7472616465");
		System.out.println(new String(bs,"utf-8"));
		bs = string2bytes("63726D5F726566756E645F7472616465");
		System.out.println(new String(bs,"utf-8"));
		
	}
}
