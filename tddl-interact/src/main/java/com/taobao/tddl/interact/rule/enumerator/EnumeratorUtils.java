/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule.enumerator;

import java.math.BigDecimal;

public class EnumeratorUtils {
	/**
	 * 将BigDecimal转换为long或者double
	 * @param big
	 * @return
	 */
	public static Comparable<?> toPrimaryValue(Comparable<?> comp){
		
		if(comp instanceof BigDecimal){
			BigDecimal big = (BigDecimal) comp;
			int scale = big.scale();
			if(scale == 0){
				//long int
				try {
					return big.longValueExact();
				} catch (ArithmeticException e) {
					return big;
				}
			}else{
				//double float
				return big.doubleValue();
			}
		}else{
			return comp;
		}
		
	}
}
