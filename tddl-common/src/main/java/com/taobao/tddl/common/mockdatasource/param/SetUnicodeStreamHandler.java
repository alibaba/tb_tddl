/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.mockdatasource.param;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetUnicodeStreamHandler implements ParameterHandler {
	@SuppressWarnings("deprecation")
	public void setParameter(PreparedStatement stmt, Object[] args)
			throws SQLException {
		stmt.setUnicodeStream((Integer) args[0], (InputStream) args[1], (Integer) args[2]);
	}
}
