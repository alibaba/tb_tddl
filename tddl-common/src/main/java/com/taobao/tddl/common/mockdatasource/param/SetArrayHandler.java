/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.mockdatasource.param;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetArrayHandler implements ParameterHandler {
	public void setParameter(PreparedStatement stmt, Object[] args) throws SQLException {
		stmt.setArray((Integer) args[0], (Array) args[1]);
	}
}
