/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.support.SqlLobValue;

/**
 * copied from org.springframework.jdbc.core.ArgPreparedStatementSetter
 */
public class ArgPreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {

	private final Object[] args;


	/**
	 * Create a new ArgPreparedStatementSetter for the given arguments.
	 * @param args the arguments to set
	 */
	public ArgPreparedStatementSetter(Object[] args) {
		this.args = args;
	}


	public void setValues(PreparedStatement ps) throws SQLException {
		if (this.args != null) {
			for (int i = 0; i < this.args.length; i++) {
				Object arg = this.args[i];
				if (arg instanceof SqlParameterValue) {
					SqlParameterValue paramValue = (SqlParameterValue) arg;
					Object inValue = adaptSqlTypeValue(paramValue.getSqlType(), paramValue.getValue());
					StatementCreatorUtils.setParameterValue(ps, i + 1, paramValue, inValue);
				}
				else {
					StatementCreatorUtils.setParameterValue(ps, i + 1, SqlTypeValue.TYPE_UNKNOWN, arg);
				}
			}
		}
	}

	private static Object adaptSqlTypeValue(int sqlType, Object inValue) {
		if (sqlType == java.sql.Types.CLOB && inValue instanceof String) {
			return new SqlLobValue((String) inValue);
		} else if (sqlType == java.sql.Types.BLOB && inValue instanceof byte[]) {
			return new SqlLobValue((byte[]) inValue);
		}
		return inValue;
	}

	public void cleanupParameters() {
		StatementCreatorUtils.cleanupParameters(this.args);
	}

}
