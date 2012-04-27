/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.support.SqlLobValue;

/**
 * copied from org.springframework.jdbc.core.ArgTypePreparedStatementSetter
 */
public class ArgTypePreparedStatementSetter implements PreparedStatementSetter, ParameterDisposer {

	private final Object[] args;

	private final int[] argTypes;


	/**
	 * Create a new ArgTypePreparedStatementSetter for the given arguments.
	 * @param args the arguments to set
	 * @param argTypes the corresponding SQL types of the arguments
	 */
	public ArgTypePreparedStatementSetter(Object[] args, int[] argTypes) {
		if ((args != null && argTypes == null) || (args == null && argTypes != null) ||
				(args != null && args.length != argTypes.length)) {
			throw new InvalidDataAccessApiUsageException("args and argTypes parameters must match");
		}
		this.args = args;
		this.argTypes = argTypes;
	}

	@SuppressWarnings("rawtypes")
	public void setValues(PreparedStatement ps) throws SQLException {
		int argIndx = 1;
		if (this.args != null) {
			for (int i = 0; i < this.args.length; i++) {
				Object arg = this.args[i];
				if (arg instanceof Collection && this.argTypes[i] != Types.ARRAY) {
					Collection entries = (Collection) arg;
					for (
					Iterator it = entries.iterator(); it.hasNext();) {
						Object entry = it.next();
						Object inValue = adaptSqlTypeValue(this.argTypes[i], entry);
						StatementCreatorUtils.setParameterValue(ps, argIndx++, this.argTypes[i], inValue);
					}
				}
				else {
					Object inValue = adaptSqlTypeValue(this.argTypes[i], arg);
					StatementCreatorUtils.setParameterValue(ps, argIndx++, this.argTypes[i], inValue);
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
