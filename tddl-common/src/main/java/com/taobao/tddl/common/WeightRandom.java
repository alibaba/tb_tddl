/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WeightRandom {
	
	private static final Log logger = LogFactory.getLog(WeightRandom.class);
	
	public static final int DEFAULT_WEIGHT_NEW_ADD = 0;
	public static final int DEFAULT_WEIGHT_INIT = 10;
	
	private Map<String, Integer> cachedWeightConfig;
	private final RuntimeConfigHolder<Weight> weightHolder = new RuntimeConfigHolder<Weight>();	
	
	/**
	 * 保持不变对象，只能重建，不能修改
	 */
	private static class Weight{
		public Weight(int[] weights, String[] weightKeys, int[] weightAreaEnds){
			this.weightKeys = weightKeys;
			this.weightValues = weights;
			this.weightAreaEnds = weightAreaEnds;
		}
		public final String[] weightKeys;  //调用者保证不能修改其元素
		public final int[] weightValues;   //调用者保证不能修改其元素
		public final int[] weightAreaEnds; //调用者保证不能修改其元素
	}
	
    public WeightRandom(Map<String, Integer> weightConfigs) {
		this.init(weightConfigs);
	}
    public WeightRandom(String[] keys) {
		Map<String, Integer> weightConfigs = new HashMap<String, Integer>(keys.length);
		for (String key : keys) {
			weightConfigs.put(key, DEFAULT_WEIGHT_INIT);
		}
		this.init(weightConfigs);
	}
	
	private void init(Map<String, Integer> weightConfig) {
    	this.cachedWeightConfig = weightConfig;
    	String[] weightKeys = weightConfig.keySet().toArray(new String[0]);
    	int[] weights = new int[weightConfig.size()];
    	for(int i=0; i<weights.length; i++){
    		weights[i] = weightConfig.get(weightKeys[i]);
    	}
    	int[] weightAreaEnds = genAreaEnds(weights);
    	weightHolder.set(new Weight(weights, weightKeys, weightAreaEnds));
	}
	
	/**
	 * 支持动态修改
	 */
	public void setWeightConfig(Map<String, Integer> weightConfig){
		this.init(weightConfig);
	}

	public Map<String, Integer> getWeightConfig(){
		return this.cachedWeightConfig;
	}
	
	/**
	 * 假设三个库权重    10   9   8
	 * 那么areaEnds就是  10  19  27
	 * 随机数是0~27之间的一个数
	 * 
	 * 分别去上面areaEnds里的元素比。
	 * 
	 * 发现随机数小于一个元素了，则表示应该选择这个元素
	 * 
	 * 注意：该方法不能改变参数数组内容
	 */
	private final Random random = new Random(); 
	private String select(int[] areaEnds, String[] keys){
		int sum = areaEnds[areaEnds.length - 1];
		if(sum == 0) {
			logger.error("areaEnds: "+Arrays.toString(areaEnds));
			return null;
		}
		//选择的过
		//findbugs认为这里不是很好(每次都新建一个Random)(guangxia)
		int rand = random.nextInt(sum);
		for(int i = 0; i < areaEnds.length; i++) {
			if(rand < areaEnds[i]) {
				return keys[i];
			}
		}
		return null;
	}
	
	/**
	 * @param excludeKeys 需要排除的key列表 
	 * @return
	 */
	public String select(List<String> excludeKeys) {
		final Weight w = weightHolder.get(); //后续实现保证不能改变w中任何数组的内容，否则线程不安全
		if (excludeKeys == null || excludeKeys.isEmpty()) {
			return select(w.weightAreaEnds, w.weightKeys);
		}
		int[] tempWeights = w.weightValues.clone();
		for (int k = 0; k < w.weightKeys.length; k++) {
			if (excludeKeys.contains(w.weightKeys[k])) {
				tempWeights[k] = 0;
			}
		}
		int[] tempAreaEnd = genAreaEnds(tempWeights);
		return select(tempAreaEnd, w.weightKeys);
	}
	
	public static interface Tryer<T extends Throwable> {
		/**
		 * @return null表示成功，否则返回一个异常
		 */
		public T tryOne(String name);
	}
	
	/**
	 * @return null表示成功，否则返回一个异常列表
	 */
	public <T extends Throwable> List<T> retry(int times, Tryer<T> tryer) {
		List<T> exceptions = new ArrayList<T>(0);
		List<String> excludeKeys = new ArrayList<String>(0);
		for (int i = 0; i < times; i++) {
			String name = this.select(excludeKeys);
			T e = tryer.tryOne(name);
			if (e != null) {
				exceptions.add(e);
				excludeKeys.add(name);
			} else {
				return null;
			}
		}
		return exceptions;
	}
	
	public <T extends Throwable> List<T> retry(Tryer<T> tryer) {
		return retry(3, tryer);
	}
	
	private static int[] genAreaEnds(int[] weights) {
		if(weights == null) {
			return null;
		}
		int[] areaEnds = new int[weights.length];
		int sum = 0;
		for(int i = 0; i < weights.length; i++) {
			sum += weights[i];
			areaEnds[i] = sum;
		}
		if(logger.isDebugEnabled()) {
			logger.debug("generate "+Arrays.toString(areaEnds)+" from "+Arrays.toString(weights));
		}
		if(sum == 0) {
			logger.warn("generate "+Arrays.toString(areaEnds)+" from "+Arrays.toString(weights));
		}
		return areaEnds;
	}
}
