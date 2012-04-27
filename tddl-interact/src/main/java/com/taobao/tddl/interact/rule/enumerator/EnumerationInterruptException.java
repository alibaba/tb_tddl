/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.interact.rule.enumerator;

import com.taobao.tddl.interact.sqljep.Comparative;

public class EnumerationInterruptException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient  Comparative comparative;
	public EnumerationInterruptException(Comparative comparative){
		this.comparative = comparative;
	}
	public EnumerationInterruptException(){
	}
	public Comparative getComparative() {
		return comparative;
	}
	public void setComparative(Comparative comparative) {
		this.comparative = comparative;
	}
	
}
