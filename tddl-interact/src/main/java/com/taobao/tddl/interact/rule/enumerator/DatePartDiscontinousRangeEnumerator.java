/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule.enumerator;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.taobao.tddl.interact.rule.bean.DateEnumerationParameter;
import com.taobao.tddl.interact.rule.util.NestThreadLocalMap;
import com.taobao.tddl.interact.sqljep.Comparative;

public class DatePartDiscontinousRangeEnumerator extends
		PartDiscontinousRangeEnumerator {
	private static final long LIMIT_UNIT_OF_DATE = 1L;
	private static final String DATE_ENUMERATOR = "DATE_ENUMERATOR";
	private static final int DEFAULT_DATE_ATOMIC_VALUE = 1;

//	private static class AutoIncreatementAndCalendarField{
//		public int atomicInreatementNumber;
//		public int CalendarFieldType;
//	}
//	protected AutoIncreatementAndCalendarField getAutoIncreatementAndCalendarField(Comparable<?> atomIncVal){
//	}
	@Override
	protected Comparative changeGreater2GreaterOrEq(Comparative from) {
		if (from.getComparison() == Comparative.GreaterThan) {
			Date gtDate = (Date) from.getValue();
			long gtOrEqDate = gtDate.getTime() + LIMIT_UNIT_OF_DATE;
			return new Comparative(Comparative.GreaterThanOrEqual, new Date(
					gtOrEqDate));

		}
		return from;
	}

	@Override
	protected Comparative changeLess2LessOrEq(Comparative to) {
		if (to.getComparison() == Comparative.LessThan) {
			Date less = (Date) to.getValue();
			long lessOrEqDate = less.getTime() - LIMIT_UNIT_OF_DATE;
			return new Comparative(Comparative.LessThanOrEqual, new Date(
					lessOrEqDate));
		}
		return to;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Comparable getOneStep(Comparable source, Comparable atomIncVal) {

		DateEnumerationParameter dateEnumerationParameter = getDateEnumerationParameter(atomIncVal);
		Calendar cal = getCalendar((Date) source);
		cal.add(dateEnumerationParameter.calendarFieldType, dateEnumerationParameter.atomicIncreatementNumber);
		return cal.getTime();
	}
	
	protected DateEnumerationParameter getDateEnumerationParameter(Comparable<?> comparable){
		DateEnumerationParameter dateEnumerationParameter = null;
		if(comparable== null){
			dateEnumerationParameter = new DateEnumerationParameter(DEFAULT_DATE_ATOMIC_VALUE);
		}else if(comparable instanceof Integer){
			dateEnumerationParameter = new DateEnumerationParameter((Integer)comparable);
		}else if(comparable instanceof DateEnumerationParameter){
			dateEnumerationParameter =(DateEnumerationParameter)comparable;
		}else{
			throwNotSupportIllegalArgumentException(comparable);
		}
		return dateEnumerationParameter;
	}
	private Calendar getCalendar(Date date2BeSet) {
		Calendar cal = (Calendar) NestThreadLocalMap.get(DATE_ENUMERATOR);
		if (cal == null) {
			cal = Calendar.getInstance();
			NestThreadLocalMap.put(DATE_ENUMERATOR, cal);
		}
		cal.setTime(date2BeSet);
		return cal;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected boolean inputCloseRangeGreaterThanMaxFieldOfDifination(
			Comparable from, Comparable to, Integer cumulativeTimes,
			Comparable<?> atomIncrValue) {
		if(cumulativeTimes == null){
			return false;
		}
		Calendar cal = getCalendar((Date) from);
		int rangeSet = cumulativeTimes;
		if(atomIncrValue instanceof Integer){
			//兼容老实现，对应#gmt,1,7#这种
			cal.add(Calendar.DATE, rangeSet*(Integer)atomIncrValue);
		}else if(atomIncrValue instanceof DateEnumerationParameter){
			//对应#gmt,1_month,12这样的情况
			DateEnumerationParameter dep = (DateEnumerationParameter)atomIncrValue;
			cal.add(dep.calendarFieldType, rangeSet*dep.atomicIncreatementNumber);
		}else if(atomIncrValue== null){
			//兼容老实现，对应没有#gmt#.这种普通的情况，
			cal.add(Calendar.DATE, rangeSet);
		}else{
			throwNotSupportIllegalArgumentException(atomIncrValue);
		}
		
		if (to.compareTo(cal.getTime()) >= 0) {
			return true;
		}
		return false;
	}
	private void throwNotSupportIllegalArgumentException(Object arg){
		throw new IllegalArgumentException("不能识别的类型:"+arg+" .type is"+ arg.getClass());
	}

	@Override
	protected Set<Object> getAllPassableFields(Comparative begin,
			Integer cumulativeTimes, Comparable<?> atomicIncreationValue) {
		if(cumulativeTimes == null){
			return Collections.emptySet();
		}
		Set<Object> returnSet =new HashSet<Object>(cumulativeTimes);
		DateEnumerationParameter dateEnumerationParameter = getDateEnumerationParameter(atomicIncreationValue);
		//把> < 替换为>= <=
		begin = changeGreater2GreaterOrEq(begin);
		begin = changeLess2LessOrEq(begin);
		
		
		Calendar cal = getCalendar((Date)begin.getValue());
		int comparasion = begin.getComparison();

		if (comparasion == Comparative.GreaterThanOrEqual) {
			//添加当前时间，因为当前时间内已经是大于等于了。
				returnSet.add(cal.getTime());
			//减少一次迭代，因为已经加了当前时间
			for (int i = 0; i < cumulativeTimes-1; i++) {
				cal.add(dateEnumerationParameter.calendarFieldType,dateEnumerationParameter.atomicIncreatementNumber);
				
				returnSet.add(cal.getTime());
			}
		}else if(comparasion ==Comparative.LessThanOrEqual){
				returnSet.add(cal.getTime());
			for(int i = 0;i<cumulativeTimes-1;i++){
				cal.add(dateEnumerationParameter.calendarFieldType, -dateEnumerationParameter.atomicIncreatementNumber);
				
				returnSet.add(cal.getTime());
			}
		}
	
		return returnSet;
	}
	
	public void processAllPassableFields(Comparative source,Set<Object> retValue,
			Integer cumulativeTimes, Comparable<?> atomIncrValue) {
		retValue.addAll(getAllPassableFields(source, cumulativeTimes, atomIncrValue));
		
	}

}
