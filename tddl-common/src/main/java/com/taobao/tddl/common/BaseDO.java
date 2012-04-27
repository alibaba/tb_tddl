/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	/*
 * Created on 2005-1-13
 *
 */
package com.taobao.tddl.common;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * 基本路由基类
 * @author xingdian
 * @version $Id: BaseDO.java,v 1.2 2005/06/14 10:20:07 shenyu Exp $
 */
public class BaseDO implements Serializable,XidHolder{
    /** Comment for <code>serialVersionUID</code> */
    private static final long serialVersionUID = 3257568403819476528L;
    private String     xid;

    /**
     * @return Returns the dbRoute.
     */
    public String getXid() {
        return xid;
    }

    /**
     * @param dbRoute The dbRoute to set.
     */
    public void setXid(String xid) {
        this.xid = xid;
    }
    
    public String toString() {
		return ToStringBuilder.reflectionToString(this) ;
	}
}
