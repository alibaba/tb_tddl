/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common;

/**
 * 一个做引用切换的Holder，使用时先set、后get
 * 用于运行时配置信息需要动态修改，实时生效的场景。
 * 所谓运行时配置信息，是指运行时被实时读取，并且影响运行时行为的配置信息。
 * 运行时配置信息动态修改时，协助使用者完成copyonwrite实现：
 * 。。。
 * 
 * @author linxuan
 *
 * @param <T> 包含运行时配置信息的对象的类型
 */
public class RuntimeConfigHolder<T> {
	private volatile T runtime;

	/**
	 * @return 上一次设入的包含运行时配置信息的对象。
	 */
	public T get() {
		return runtime;
	}

	/**
	 * @param runtime 包含运行时配置信息的对象。
	 */
	public void set(T runtime) {
		this.runtime = runtime;
	}
}
