/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.config;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author yangzhu
 *
 */
public class WeightUnitTest {

	@Test
	public void all() {
		Weight w = new Weight(null);
		Assert.assertEquals(w.r, 10);
		Assert.assertEquals(w.w, 10);
		Assert.assertEquals(w.p, 0);
		Assert.assertEquals(w.q, 0);
		
		w = new Weight("");
		Assert.assertEquals(w.r, 0);
		Assert.assertEquals(w.w, 0);
		Assert.assertEquals(w.p, 0);
		Assert.assertEquals(w.q, 0);
		
		w = new Weight("   ");
		Assert.assertEquals(w.r, 0);
		Assert.assertEquals(w.w, 0);
		Assert.assertEquals(w.p, 0);
		Assert.assertEquals(w.q, 0);
		
		w = new Weight("rwpq");
		Assert.assertEquals(w.r, 10);
		Assert.assertEquals(w.w, 10);
		Assert.assertEquals(w.p, 0);
		Assert.assertEquals(w.q, 0);
		
		w = new Weight("");
		Assert.assertEquals(w.r, 0);
		Assert.assertEquals(w.w, 0);
		Assert.assertEquals(w.p, 0);
		Assert.assertEquals(w.q, 0);
		
		w = new Weight("r10w20p1q2");
		Assert.assertEquals(w.r, 10);
		Assert.assertEquals(w.w, 20);
		Assert.assertEquals(w.p, 1);
		Assert.assertEquals(w.q, 2);
		
		Assert.assertEquals(w.toString(), "Weight[r=10, w=20, p=1, q=2]");
		
		w = new Weight("R10W20P1Q2");
		Assert.assertEquals(w.r, 10);
		Assert.assertEquals(w.w, 20);
		Assert.assertEquals(w.p, 1);
		Assert.assertEquals(w.q, 2);
		
		Assert.assertEquals(w.toString(), "Weight[r=10, w=20, p=1, q=2]");
	}
}
