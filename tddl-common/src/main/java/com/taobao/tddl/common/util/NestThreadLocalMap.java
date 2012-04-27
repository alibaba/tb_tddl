/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.taobao.tddl.common.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ThreadLocal Context
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 * @version $Id: ThreadLocalContext.java 3597 2006-11-23 08:11:58Z struct $
 */
public class NestThreadLocalMap{
	private static final Log log = LogFactory.getLog(NestThreadLocalMap.class);
	protected final static ThreadLocal<Map<Object,Object>> threadContext = new MapThreadLocal();
	
	private NestThreadLocalMap(){};
	
	public static void put(Object key,Object value){
		getContextMap().put(key,value);
	}
	
	public static Object remove(Object key){
		return getContextMap().remove(key);
	}
	
	public static Object get(Object key){
		return getContextMap().get(key);
	}
	
	public static boolean containsKey(Object key){
		return getContextMap().containsKey(key);
	}
	
	private static class MapThreadLocal extends ThreadLocal<Map<Object,Object>> {
        protected Map<Object,Object> initialValue() {
        	return new HashMap<Object,Object>() {
				
        		private static final long serialVersionUID = 3637958959138295593L;
				
				public Object put(Object key, Object value) {
                    if (log.isDebugEnabled()) {
                        if (containsKey(key)) {
                        	log.debug("Overwritten attribute to thread context: " + key
                                + " = " + value);
                        } else {
                        	log.debug("Added attribute to thread context: " + key + " = "
                                + value);
                        }
                    }

                    return super.put(key, value);
                }
            };
        }
    }
	
	/**
     * 取得thread context Map的实例。
     *
     * @return thread context Map的实例
     */
    protected static Map<Object,Object> getContextMap() {
        return (Map<Object,Object>) threadContext.get();
    }
	
    
    /**
     * 清理线程所有被hold住的对象。以便重用！
     */
    
    public static void reset(){
    	getContextMap().clear();
    }
}
