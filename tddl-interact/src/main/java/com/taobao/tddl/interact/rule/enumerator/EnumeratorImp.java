/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule.enumerator;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.taobao.tddl.interact.sqljep.Comparative;
import com.taobao.tddl.interact.sqljep.ComparativeAND;
import com.taobao.tddl.interact.sqljep.ComparativeBaseList;
import com.taobao.tddl.interact.sqljep.ComparativeOR;
import com.taobao.tddl.interact.rule.exception.NotSupportException;
import static com.taobao.tddl.interact.rule.enumerator.EnumeratorUtils.toPrimaryValue;

public class EnumeratorImp implements Enumerator {
	private static final String DEFAULT_ENUMERATOR = "DEFAULT_ENUMERATOR";
	protected static final Map<String,CloseIntervalFieldsEnumeratorHandler> enumeratorMap = new HashMap<String, CloseIntervalFieldsEnumeratorHandler>();
	
	{
		enumeratorMap.put(Integer.class.getName(), new IntegerPartDiscontinousRangeEnumerator());
		enumeratorMap.put(Long.class.getName(), new LongPartDiscontinousRangeEnumerator());
		enumeratorMap.put(BigDecimal.class.getName(), new LongPartDiscontinousRangeEnumerator());
		enumeratorMap.put(Date.class.getName(), new DatePartDiscontinousRangeEnumerator());
		enumeratorMap.put(java.sql.Date.class.getName(), new DatePartDiscontinousRangeEnumerator());
		enumeratorMap.put(java.sql.Timestamp.class.getName(), new DatePartDiscontinousRangeEnumerator());
		enumeratorMap.put(DEFAULT_ENUMERATOR, new DefaultEnumerator());
	}
	private boolean isDebug = false;
	/**
	 * 根据传入的参数决定使用哪类枚举器
	 * 
	 * TODO 应该将枚举器限定范围缩小，其实枚举器的核心作用就是算a > ? and a < ?这样的表达式，
	 * 而其他的时候是不需要用这种自增+枚举的方式来搞的。
	 * @param comp
	 * @param needMergeValueInCloseInterval
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private  CloseIntervalFieldsEnumeratorHandler getCloseIntervalEnumeratorHandlerByComparative(Comparative comp,boolean needMergeValueInCloseInterval){
		if(!needMergeValueInCloseInterval){
			return enumeratorMap.get(DEFAULT_ENUMERATOR);
		}
		if(comp == null){
			throw new IllegalArgumentException("不知道当前值是什么类型的，无法找到对应的枚举器"+comp);
		}
		
		Comparable value = comp.getValue();
		
		if(value instanceof ComparativeBaseList){
			ComparativeBaseList comparativeBaseList = (ComparativeBaseList)value;
			for(Comparative comparative:comparativeBaseList.getList()){
				return getCloseIntervalEnumeratorHandlerByComparative(comparative,needMergeValueInCloseInterval);
			}
			throw new IllegalStateException("should not be here");
		}else if(value instanceof Comparative){
			return getCloseIntervalEnumeratorHandlerByComparative(comp,needMergeValueInCloseInterval);
		}else{
			//表明是一个comparative对象
			CloseIntervalFieldsEnumeratorHandler enumeratorHandler = enumeratorMap.get(value.getClass().getName());
			if(enumeratorHandler != null){
				return enumeratorHandler;
			}else{
				return enumeratorMap.get(DEFAULT_ENUMERATOR);
			}
		}
	}
	@SuppressWarnings("rawtypes")
	public Set<Object> getEnumeratedValue(Comparable condition,Integer cumulativeTimes,Comparable<?> atomIncrValue,boolean needMergeValueInCloseInterval) {
		Set<Object> retValue = null;
		if (!isDebug) {
			retValue = new HashSet<Object>();
		} else {
			retValue = new TreeSet<Object>();
		}
		try {
			process(condition, retValue,cumulativeTimes,atomIncrValue,needMergeValueInCloseInterval);
		} catch (EnumerationInterruptException e) {
			processAllPassableFields(e.getComparative(),retValue, cumulativeTimes, atomIncrValue,needMergeValueInCloseInterval);
		}
		return retValue;
	}
	private void process(Comparable<?> condition, Set<Object> retValue,Integer cumulativeTimes,Comparable<?> atomIncrValue
			,boolean needMergeValueInCloseInterval) {

		if (condition == null) {
			retValue.add(null);
		} else if (condition instanceof ComparativeOR) {

			processComparativeOR(condition, retValue,cumulativeTimes,atomIncrValue,needMergeValueInCloseInterval);

		} else if (condition instanceof ComparativeAND) {

			processComparativeAnd(condition, retValue,cumulativeTimes,atomIncrValue,needMergeValueInCloseInterval);

		} else if (condition instanceof Comparative) {
			processComparativeOne(condition, retValue,cumulativeTimes,atomIncrValue,needMergeValueInCloseInterval);
		} else {
			retValue.add(condition);
		}
	}

	private boolean containsEquvilentRelation(Comparative comp) {
		int comparasion = comp.getComparison();

		if (comparasion == Comparative.Equivalent
				|| comparasion == Comparative.GreaterThanOrEqual
				|| comparasion == Comparative.LessThanOrEqual) {
			return true;
		}
		return false;
	}
	@SuppressWarnings("unchecked")
	private void processComparativeAnd(Comparable<?> condition,
			Set<Object> retValue,Integer cumulativeTimes,Comparable<?> atomIncrValue,boolean needMergeValueInCloseInterval) {
		List<Comparative> andList = ((ComparativeAND) condition).getList();
		// 多余两个感觉没什么实际的意义，碰到了再处理
		if (andList.size() == 2) {
			Comparable<?> arg1 = andList.get(0);
			Comparable<?> arg2 = andList.get(1);

			Comparative compArg1 = valid2varableInAndIsNotComparativeBaseList(arg1);

			Comparative compArg2 = valid2varableInAndIsNotComparativeBaseList(arg2);
//
//			if(compArg1 == null){
//				throw new IllegalArgumentException("and 参数中有一个为null");
//			}
//			if(compArg2 == null){
//				throw new IllegalArgumentException("and 参数中有一个为null");
//			}
			int compResult = 0;
			try {
				compArg1.setValue(toPrimaryValue(compArg1.getValue()));
				compArg2.setValue(toPrimaryValue(compArg2.getValue()));
				compResult = compArg1.getValue().compareTo(compArg2.getValue());
			} catch (NullPointerException e) {
				throw new RuntimeException("and条件中有一个值为null",e);
			}
			

			if (compResult == 0) {
				// 值相等，如果都含有=关系，那么还有个公共点，否则一个公共点都没有
				if (containsEquvilentRelation(compArg1)
						&& containsEquvilentRelation(compArg2)) {

					retValue.add(compArg1.getValue());
				}
				// else{
				// 一个公共点都没有的情况，扔掉
				// }
			} else if (compResult < 0) {
				// arg1 < arg2
				processTwoDifferentArgsInComparativeAnd(retValue, compArg1,
						compArg2,cumulativeTimes,atomIncrValue,needMergeValueInCloseInterval);
			} else {
				// compResult>0
				// arg1 > arg2
				processTwoDifferentArgsInComparativeAnd(retValue, compArg2,
						compArg1,cumulativeTimes,atomIncrValue,needMergeValueInCloseInterval);
			}
		} else {
			throw new IllegalArgumentException("目前只支持一个and节点上有两个子节点");
		}
	}

	/**
	 * 处理在一个and条件中的两个不同的argument
	 * 
	 * @param samplingField
	 * @param from
	 * @param to
	 */
	@SuppressWarnings("rawtypes")
	private void processTwoDifferentArgsInComparativeAnd(Set<Object> retValue,
			Comparative from, Comparative to,Integer cumulativeTimes,Comparable<?> atomIncrValue
			,boolean needMergeValueInCloseInterval) {
		if (isCloseInterval(from, to)) {
			mergeFeildOfDefinitionInCloseInterval(from, to, retValue,cumulativeTimes,atomIncrValue,needMergeValueInCloseInterval);
		} else {
			Comparable temp = compareAndGetIntersactionOneValue(from, to);
			if (temp != null) {
				retValue.add(temp);
			}else{
				//闭区间已经处理过，x >= ? and x = ? 或者 x <= ? and x = ?有交的也处理过，纯粹的> 和 <已经被转化为 >= 以及<=
				//这里主要处理三类情况 x <= 3 and x>=5  这类，
				if(from.getComparison() == Comparative.LessThanOrEqual||
						from.getComparison() == Comparative.LessThan){
					if(to.getComparison() == Comparative.LessThanOrEqual
						||to.getComparison() == Comparative.LessThan){
						processAllPassableFields(from, retValue, cumulativeTimes, atomIncrValue,needMergeValueInCloseInterval);
					}else{
						//to为GreaterThanOrEqual,活着为Equals 那么是个开区间。do nothing.
						
					}
				}else if(to.getComparison() == Comparative.GreaterThanOrEqual||
						to.getComparison() == Comparative.GreaterThan){
					if(from.getComparison() == Comparative.GreaterThanOrEqual||
							from.getComparison() == Comparative.GreaterThan	){
						processAllPassableFields(to, retValue, cumulativeTimes, atomIncrValue,needMergeValueInCloseInterval);
					}else{
						//from为LessThanOrEqual，或者为Equals,为开区间
					}
				}else{
					throw new IllegalArgumentException("should not be here");
				}
			}
			// else{
			// 不是0<x and x=3这种情况
			// }
		}
	}

	/**
	 * 处理一个and条件中 x > 1 and x = 3 类似这样的情况，因为前面已经对from 和 to 相等的情况作了处理
	 * 因此这里只需要处理不等的情况中的上述问题。
	 * 同时也处理了x = 1 and x = 2这种情况。以及x = 1 and x>2 和x < 1 and x =2这种情况
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected static Comparable compareAndGetIntersactionOneValue(
			Comparative from, Comparative to) {
		// x = from and x <= to
		if (from.getComparison() == Comparative.Equivalent) {
			if (to.getComparison() == Comparative.LessThan
					|| to.getComparison() == Comparative.LessThanOrEqual) {
				return from.getValue();
			}
		}
		// x <= from and x = to
		if (to.getComparison() == Comparative.Equivalent) {
			if (from.getComparison() == Comparative.GreaterThan
					|| from.getComparison() == Comparative.GreaterThanOrEqual) {
				return to.getValue();
			}
		}
		return null;
	}

	protected static boolean isCloseInterval(Comparative from, Comparative to) {
		int fromComparasion = from.getComparison();

		int toComparasion = to.getComparison();

		// 本来想简单通过数值比大小，但发现里面还有not in,like这类的标记，还是保守点写清楚
		if ((fromComparasion == Comparative.GreaterThan || fromComparasion == Comparative.GreaterThanOrEqual)
				&& (toComparasion == Comparative.LessThan || toComparasion == Comparative.LessThanOrEqual)) {
			return true;
		} else {
			return false;
		}

	}

	private Comparative valid2varableInAndIsNotComparativeBaseList(
			Comparable<?> arg) {
		if (arg instanceof ComparativeBaseList) {

			throw new IllegalArgumentException("在一组and条件中只支持两个范围的值共同决定分表，不支持3个");
		}

		if (arg instanceof Comparative) {
			Comparative comp = ((Comparative) arg);
			int comparison = comp.getComparison();

			if (comparison == 0) {

				// 0的时候意味着这个非COmparativeBaseList的Comparative是个纯粹的包装对象。
				return valid2varableInAndIsNotComparativeBaseList(comp
						.getValue());
			} else {

				// 其他就是有意义的值对象了
				return comp;
			}
		} else {
			// 否则就是基本对象，应该用等于包装
			throw new IllegalArgumentException("input value is not a comparative: "+arg);
			// return new Comparative(Comparative.Equivalent,arg);
		}

	}

	private void processComparativeOne(Comparable<?> condition,
			Set<Object> retValue,Integer cumulativeTimes,Comparable<?> atomIncrValue,boolean needMergeValueInCloseInterval) {
		Comparative comp = (Comparative) condition;
		int comparison = comp.getComparison();
		switch (comparison) {
		case 0:

			// 为0 的时候表示纯粹的包装对象。
			process(comp.getValue(), retValue,cumulativeTimes,atomIncrValue,needMergeValueInCloseInterval);
			break;
		case Comparative.Equivalent:

			// 等于关系，直接放在collection
			retValue.add(toPrimaryValue(comp.getValue()));
			break;
		case Comparative.GreaterThan:
		case Comparative.GreaterThanOrEqual:
		case Comparative.LessThan:
		case Comparative.LessThanOrEqual:
			//各种需要全取的情况
			throw new EnumerationInterruptException(comp);
		default:
			throw new NotSupportException("not support yet");
		}
	}

	private void processComparativeOR(Comparable<?> condition,
			Set<Object> retValue,Integer cumulativeTimes,Comparable<?> atomIncrValue,boolean needMergeValueInCloseInterval) {
		List<Comparative> orList = ((ComparativeOR) condition).getList();

		for (Comparative comp : orList) {

			process(comp, retValue,cumulativeTimes,atomIncrValue,needMergeValueInCloseInterval);

		}
	}

	/**
	 * 穷举出从from到to中的所有值，根据自增value
	 * 
	 * @param from
	 * @param to
	 */
	private void mergeFeildOfDefinitionInCloseInterval(
			Comparative from, Comparative to, Set<Object> retValue,Integer cumulativeTimes,Comparable<?> atomIncrValue
			,boolean needMergeValueInCloseInterval){
		if(!needMergeValueInCloseInterval){
			throw new IllegalArgumentException("请打开规则的needMergeValueInCloseInterval选项，以支持分库分表条件中使用> < >= <=");
		}
		//重构 现在这种架构下，id =? id in (?,?,?)都能走最短路径，但如果有多个 id > ? and id < ? or id> ? and id<? 则要从map中查多次。不过因为这种情况比较少，因此可以忽略
		CloseIntervalFieldsEnumeratorHandler closeIntervalFieldsEnumeratorHandler = getCloseIntervalEnumeratorHandlerByComparative(from, needMergeValueInCloseInterval);
		closeIntervalFieldsEnumeratorHandler.mergeFeildOfDefinitionInCloseInterval(from, to, retValue, cumulativeTimes, atomIncrValue);
	}
	/**
	 * 函数的目标是返回全部可能的值，主要用于无限的定义域的处理，一般的说，对于部分连续部分不连续的函数曲线。
	 * 这个值应该是从任意一个值开始，按照原子自增值与倍数穷举出该函数的y的一个变化周期中x对应的变化周期的所有点即可。
	 * @param retValue
	 * @param cumulativeTimes
	 * @param atomIncrValue
	 */
	private void processAllPassableFields(Comparative source ,Set<Object> retValue,Integer cumulativeTimes,Comparable<?> atomIncrValue,
			boolean needMergeValueInCloseInterval){
		if(!needMergeValueInCloseInterval){
			throw new IllegalArgumentException("请打开规则的needMergeValueInCloseInterval选项，以支持分库分表条件中使用> < >= <=");
		}
		//重构 现在这种架构下，id =? id in (?,?,?)都能走最短路径，但如果有多个 id > ? and id < ? or id> ? and id<? 则要从map中查多次。不过因为这种情况比较少，因此可以忽略
		CloseIntervalFieldsEnumeratorHandler closeIntervalFieldsEnumeratorHandler = getCloseIntervalEnumeratorHandlerByComparative(source, needMergeValueInCloseInterval);
		closeIntervalFieldsEnumeratorHandler.processAllPassableFields(source, retValue, cumulativeTimes, atomIncrValue);
	}

//	public boolean isNeedMergeValueInCloseInterval() {
//		return needMergeValueInCloseInterval;
//	}
//
//	public void setNeedMergeValueInCloseInterval(
//			boolean needMergeValueInCloseInterval) {
//		this.needMergeValueInCloseInterval = needMergeValueInCloseInterval;
//	}

	public boolean isDebug() {
		return isDebug;
	}

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

}
