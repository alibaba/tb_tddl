/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.exception.lru;

public class LRUHashMapException extends Exception {
	/**	 * 	 */	private static final long serialVersionUID = 3345525607056599286L;	public LRUHashMapException()
	{
		super();
	}
	public LRUHashMapException(String message)
	{
		super(message);
	}
	public LRUHashMapException(String message, Throwable cause)
	{
		super(message,cause);
	}
}
