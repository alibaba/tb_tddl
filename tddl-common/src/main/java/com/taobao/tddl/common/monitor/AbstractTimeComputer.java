/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.monitor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;
import com.taobao.tddl.common.util.TStringUtil;
public abstract class AbstractTimeComputer implements TimeComputer {

	private List<Date> getDates(Calendar ca){
		Calendar newCal = (Calendar) ca.clone();
		List<Date> re=new ArrayList<Date>();
		String[] pieces;
		for(String time:getTimes()){
			pieces=TStringUtil.splitm(time,":");
			newCal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(pieces[0]));
			newCal.set(Calendar.MINUTE,Integer.parseInt(pieces[1]));
			newCal.set(Calendar.SECOND,Integer.parseInt(pieces[2]));
			re.add(newCal.getTime());
		}
		
		newCal.add(Calendar.DATE, 1);
		newCal.set(Calendar.HOUR_OF_DAY,0);
		newCal.set(Calendar.MINUTE,0);
		newCal.set(Calendar.SECOND,0);
		re.add(newCal.getTime());
		
		return re;
	}
	
	private TreeMap<Long,Date> getMostNearTimeIntervalMap(Calendar ca){
		TreeMap<Long,Date> map=new TreeMap<Long, Date>();
	
		Date now=ca.getTime();
		List<Date> halfTimes=getDates(ca);
		for(Date halfTime:halfTimes){
			map.put(halfTime.getTime()-now.getTime(), halfTime);
		}
		
		return map;
	}
	
	long getMostNearTimeInterval(Calendar ca){
		TreeMap<Long,Date> map=getMostNearTimeIntervalMap(ca);
		for(Entry<Long, Date> entry: map.entrySet()){
	        if(entry.getKey()>0){
	             return entry.getKey();
	        }
		}
		
		return map.lastKey();
	}

	public long getMostNearTimeInterval(){
		Calendar ca=Calendar.getInstance();
		return getMostNearTimeInterval(ca);
	}
	public Date getMostNearTime(){
		Calendar ca=Calendar.getInstance();
		return getMostNearTime(ca);
	}
	
	protected Date getMostNearTime(Calendar ca){
		TreeMap<Long,Date> map=getMostNearTimeIntervalMap(ca);
		for(Entry<Long, Date> entry: map.entrySet()){
	        if(entry.getKey()>0){
	             return entry.getValue();
	        }
		}
		
		return map.get(map.lastKey());
	}
    abstract List<String> getTimes();
}
