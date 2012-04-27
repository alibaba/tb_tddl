/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule.enumerator;


public class IntegerPartDiscontinousRangeEnumerator extends NumberPartDiscontinousRangeEnumerator {
	/*
	private static final int LIMIT_UNIT_OF_INT = 1;
	public static final int DEFAULT_ATOMIC_VALUE = 1;

	@Override
	protected Comparative changeGreater2GreaterOrEq(Comparative from) {
		if (from.getComparison() == Comparative.GreaterThan) {

			int fromComparable = (Integer) from.getValue();

			return new Comparative(Comparative.GreaterThanOrEqual, fromComparable + LIMIT_UNIT_OF_INT);
		} else {
			return from;
		}
	}

	@Override
	protected Comparative changeLess2LessOrEq(Comparative to) {
		if (to.getComparison() == Comparative.LessThan) {

			int toComparable = (Integer) to.getValue();

			return new Comparative(Comparative.LessThanOrEqual, toComparable - LIMIT_UNIT_OF_INT);
		} else {

			return to;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Comparable getOneStep(Comparable source, Comparable atomIncVal) {
		if (atomIncVal == null) {
			atomIncVal = DEFAULT_ATOMIC_VALUE;
		}
		int sourceInt = (Integer) source;

		int atomIncValInt = (Integer) atomIncVal;

		return sourceInt + atomIncValInt;
	}

	@SuppressWarnings("unchecked")
	protected boolean inputCloseRangeGreaterThanMaxFieldOfDifination(Comparable from, Comparable to,
			Integer cumulativeTimes, Comparable<?> atomIncrValue) {
		if (cumulativeTimes == null) {
			return false;
		}
		if (atomIncrValue == null) {
			atomIncrValue = DEFAULT_ATOMIC_VALUE;
		}
		int fromInt = (Integer) from;
		int toInt = (Integer) to;
		int atomIncValInt = (Integer) atomIncrValue;
		int size = cumulativeTimes;

		if ((toInt - fromInt) > (atomIncValInt * size)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected Set<Object> getAllPassableFields(Comparative begin, Integer cumulativeTimes,
			Comparable<?> atomicIncreationValue) {
		if (cumulativeTimes == null) {
			throw new IllegalStateException("在没有提供叠加次数的前提下，不能够根据当前范围条件选出对应的定义域的枚举值，sql中不要出现> < >= <=");
		}
		if (atomicIncreationValue == null) {
			atomicIncreationValue = DEFAULT_ATOMIC_VALUE;
		}
		//把> < 替换为>= <=
		begin = changeGreater2GreaterOrEq(begin);
		begin = changeLess2LessOrEq(begin);

		Set<Object> returnSet = new HashSet<Object>(cumulativeTimes);
		int beginInt = (Integer) begin.getValue();
		int atomicIncreateValueInt = (Integer) atomicIncreationValue;
		int comparasion = begin.getComparison();

		if (comparasion == Comparative.GreaterThanOrEqual) {
			for (int i = 0; i < cumulativeTimes; i++) {
				returnSet.add(beginInt + atomicIncreateValueInt * i);
			}
		} else if (comparasion == Comparative.LessThanOrEqual) {
			for (int i = 0; i < cumulativeTimes; i++) {
				returnSet.add(beginInt - atomicIncreateValueInt * i);
			}
		}
		return returnSet;
	}

	public void processAllPassableFields(Comparative source, Set<Object> retValue, Integer cumulativeTimes,
			Comparable<?> atomIncrValue) {
		retValue.addAll(getAllPassableFields(source, cumulativeTimes, atomIncrValue));

	}
	*/

	@SuppressWarnings("rawtypes")
	@Override
	protected Number cast2Number(Comparable begin) {
		return (Integer) begin;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Number getNumber(Comparable begin) {
		return (Integer) begin;
	}

	@Override
	protected Number plus(Number begin, int plus) {
		return (Integer) begin + plus;
	}

}
