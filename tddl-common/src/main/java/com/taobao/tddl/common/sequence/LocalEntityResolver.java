/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.sequence;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/*
 * @author guangxia
 * @since 1.0, 2009-5-12 ÏÂÎç08:39:02
 */
public class LocalEntityResolver implements EntityResolver {

	public InputSource resolveEntity(String publicId, String systemId) {
		if("-//arch.taobao.com//tddl generators config DTD//ZH".equals(publicId)
				&& "http://arch.taobao.com/tddl/generators.dtd".equals(systemId)) {
			return new InputSource(getClass().getResourceAsStream("/generators.dtd"));
		} else {
			return new InputSource(systemId);
		}
	}

}
