/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 存放列名->sourceKey的映射。
 * 
 * 多添加一个Set。用于特殊场景的运算。
 * 
 * 这个set的主要作用就是存放sourceKey的同时，也存放映射后的结果。这个结果是在mapping rule中查tair以后产生的，为了减少一次查
 * 
 * tair的过程，因此要记录下查tair以后的值都是哪些，并且按照结果进行分类。
 * 
 * 因为映射规则只允许列名唯一，不允许多列参与运算。
 * 
 * 在列名有且仅有一个的情况下。set中的targetValue应该就是sourceKey通过tair映射以后的结果。
 * 
 * 在其他情况下,mappingKeys应该永远为空。
 * 
 * 这样写的作用是不污染现有sourceKeys。减少逻辑改动
 * 
 * @author shenxun
 * 
 */
public class Field
{
	public Field(int capacity)
	{
		sourceKeys = new HashMap<String, Set<Object>>(capacity);
	}

	public Map<String/* 列名 */, Set<Object>/* 得到该结果的描点值名 */> sourceKeys;

	public static final Field EMPTY_FIELD = new Field(0);

	/**
	 * 用于映射规则中存放映射后的所有值，这些值都应该有相同的列名，对应mappingTargetColumn
	 */
	public Set<Object> mappingKeys;
	 /**
	 * 对应上述mappingKeys的targetColumn
	 */
	public String mappingTargetColumn;
	
	
	public boolean equals(Object obj, Map<String, String> alias)
	{
		//用于比较两个field是否相等。field包含多个列，那么多列内的每一个值都应该能找到对应的值才算相等。
		if (!(obj instanceof Field))
		{
			return false;
		}
		Map<String, Set<Object>> target = ((Field) obj).sourceKeys;
		for (Entry<String, Set<Object>> entry : sourceKeys.entrySet())
		{
			String srcKey = entry.getKey();
			if (alias.containsKey(srcKey))
			{
				srcKey = alias.get(srcKey);
			}
			Set<Object> targetValueSet = target.get(srcKey);
			Set<Object> sourceValueSet = entry.getValue();
			for (Object srcValue : sourceValueSet)
			{
				boolean eq = false;
				for (Object tarValue : targetValueSet)
				{
					if(tarValue.equals(srcValue)){
						eq = true;
					}
				}
				if(!eq)
				{
					return false;
				}
			}
		}
		return true;
	}
}
