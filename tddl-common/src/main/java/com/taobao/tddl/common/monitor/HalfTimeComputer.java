/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.monitor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HalfTimeComputer extends AbstractTimeComputer{
	private List<String> times=new ArrayList<String>();
	
	{
		times.add("00:30:00");
		times.add("01:00:00");
		times.add("01:30:00");
		times.add("02:00:00");
		times.add("02:30:00");
		times.add("03:00:00");
		times.add("03:30:00");
		times.add("04:00:00");
		times.add("04:30:00");
		times.add("05:00:00");
		times.add("05:30:00");
		times.add("06:00:00");
		times.add("06:30:00");
		times.add("07:00:00");
		times.add("07:30:00");
		times.add("08:00:00");
		times.add("08:30:00");
		times.add("09:00:00");
		times.add("09:30:00");
		times.add("10:00:00");
		times.add("10:30:00");
		times.add("11:00:00");
		times.add("11:30:00");
		times.add("12:00:00");
		times.add("12:30:00");
		times.add("13:00:00");
		times.add("13:30:00");
		times.add("14:00:00");
		times.add("14:30:00");
		times.add("15:00:00");
		times.add("15:30:00");
		times.add("16:00:00");
		times.add("16:30:00");
		times.add("17:00:00");
		times.add("17:30:00");
		times.add("18:00:00");
		times.add("18:30:00");
		times.add("19:00:00");
		times.add("19:30:00");
		times.add("20:00:00");
		times.add("20:30:00");
		times.add("21:00:00");
		times.add("21:30:00");
		times.add("22:00:00");
		times.add("22:30:00");
		times.add("23:00:00");
		times.add("23:30:00");
	}

	@Override
	List<String> getTimes() {
		return this.times;
	}
	
	public static void main(String[] args){
		HalfTimeComputer co=new HalfTimeComputer();
		System.out.println(Calendar.getInstance().getTime());
		System.out.println(co.getMostNearTime());
		System.out.println(co.getMostNearTimeInterval());
	}
}
