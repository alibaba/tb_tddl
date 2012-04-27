/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SimpleNamedMessageFormatTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFormat() {
		SimpleNamedMessageFormat mf0 = new SimpleNamedMessageFormat("一二34五六78九十");
		SimpleNamedMessageFormat mf1 = new SimpleNamedMessageFormat("{one}二34五六78九{ten}");
		SimpleNamedMessageFormat mf2 = new SimpleNamedMessageFormat("一{two}34五六78{nine}十");
		SimpleNamedMessageFormat mf3 = new SimpleNamedMessageFormat("一二{three}4五六78九十");
		SimpleNamedMessageFormat mf4 = new SimpleNamedMessageFormat("{one}二{three}{three}4五六78九{ten}");
		SimpleNamedMessageFormat mf5 = new SimpleNamedMessageFormat("一二34五六78九{ten}");
		SimpleNamedMessageFormat mf6 = new SimpleNamedMessageFormat("{one}二34五六78九十");
		SimpleNamedMessageFormat mf7 = new SimpleNamedMessageFormat("{one}{one}二34五六78九{ten}{ten}");
		SimpleNamedMessageFormat mf8 = new SimpleNamedMessageFormat("{one}{two}3{four}{five}六78{nine}{ten}");
		SimpleNamedMessageFormat mf9 = new SimpleNamedMessageFormat("一{nine}2{nine}3{nine}");
		SimpleNamedMessageFormat mfa = new SimpleNamedMessageFormat("{one}二{thr{one}ee}4五六78九{ten}");
		SimpleNamedMessageFormat mfb = new SimpleNamedMessageFormat("{one}二{hasnoargs}4五六78九{ten}");
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("one", "一");
		params.put("two", "二");
		params.put("three", 3);
		params.put("four", 4);
		params.put("five", "五");
		params.put("nine", "九");
		params.put("ten", "十");
		
		Assert.assertEquals(mf0.format(params),"一二34五六78九十");
		Assert.assertEquals(mf1.format(params),"一二34五六78九十");
		Assert.assertEquals(mf2.format(params),"一二34五六78九十");
		Assert.assertEquals(mf3.format(params),"一二34五六78九十");
		Assert.assertEquals(mf4.format(params),"一二334五六78九十");
		Assert.assertEquals(mf5.format(params),"一二34五六78九十");
		Assert.assertEquals(mf6.format(params),"一二34五六78九十");
		Assert.assertEquals(mf7.format(params),"一一二34五六78九十十");
		Assert.assertEquals(mf8.format(params),"一二34五六78九十");
		Assert.assertEquals(mf9.format(params),"一九2九3九");
		Assert.assertEquals(mfa.format(params),"一二{thr{one}ee}4五六78九十");
		Assert.assertEquals(mfb.format(params),"一二{hasnoargs}4五六78九十");
	}
	@Test
	public void testFormat2() {
		SimpleNamedMessageFormat mf0 = new SimpleNamedMessageFormat("一二34五六78九十", "${", "}");
		SimpleNamedMessageFormat mf1 = new SimpleNamedMessageFormat("${one}二34五六78九${ten}", "${", "}");
		SimpleNamedMessageFormat mf2 = new SimpleNamedMessageFormat("一${two}34五六78${nine}十", "${", "}");
		SimpleNamedMessageFormat mf3 = new SimpleNamedMessageFormat("一二${three}4五六78九十", "${", "}");
		SimpleNamedMessageFormat mf4 = new SimpleNamedMessageFormat("${one}二${three}${three}4五六78九${ten}", "${", "}");
		SimpleNamedMessageFormat mf5 = new SimpleNamedMessageFormat("一二34五六78九${ten}", "${", "}");
		SimpleNamedMessageFormat mf6 = new SimpleNamedMessageFormat("${one}二34五六78九十", "${", "}");
		SimpleNamedMessageFormat mf7 = new SimpleNamedMessageFormat("${one}${one}二34五六78九${ten}${ten}", "${", "}");
		SimpleNamedMessageFormat mf8 = new SimpleNamedMessageFormat("${one}${two}3${four}${five}六78${nine}${ten}", "${", "}");
		SimpleNamedMessageFormat mf9 = new SimpleNamedMessageFormat("一${nine}2${nine}3${nine}", "${", "}");
		SimpleNamedMessageFormat mfa = new SimpleNamedMessageFormat("一${nine}2{nine}3${nine}", "${", "}");
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("one", "一");
		params.put("two", "二");
		params.put("three", 3);
		params.put("four", 4);
		params.put("five", "五");
		params.put("nine", "九");
		params.put("ten", "十");
		
		Assert.assertEquals(mf0.format(params),"一二34五六78九十");
		Assert.assertEquals(mf1.format(params),"一二34五六78九十");
		Assert.assertEquals(mf2.format(params),"一二34五六78九十");
		Assert.assertEquals(mf3.format(params),"一二34五六78九十");
		Assert.assertEquals(mf4.format(params),"一二334五六78九十");
		Assert.assertEquals(mf5.format(params),"一二34五六78九十");
		Assert.assertEquals(mf6.format(params),"一二34五六78九十");
		Assert.assertEquals(mf7.format(params),"一一二34五六78九十十");
		Assert.assertEquals(mf8.format(params),"一二34五六78九十");
		Assert.assertEquals(mf9.format(params),"一九2九3九");
		Assert.assertEquals(mfa.format(params),"一九2{nine}3九");
	}

	@Test
	public void testFormatReuse() {
		SimpleNamedMessageFormat mf = new SimpleNamedMessageFormat("{one}二3{four}五6{seven}8九{ten}");
		
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("one", "一");
		params.put("four", "四");
		params.put("seven", "七");
		params.put("ten", "十");
		Assert.assertEquals(mf.format(params),"一二3四五6七8九十");
		
		params.put("one", "1");
		params.put("four", "4");
		params.put("seven", "7");
		params.put("ten", "a");
		Assert.assertEquals(mf.format(params),"1二34五678九a");

		params.put("one", "心");
		params.put("four", "心");
		params.put("seven", "心");
		params.put("ten", "心");
		Assert.assertEquals(mf.format(params),"心二3心五6心8九心");
	}
}
