/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * PreparedStatement设置参数的处理
 * 
 * @author linxuan
 *
 */
public enum ParameterMethod {
	setArray, setAsciiStream, setBigDecimal, setBinaryStream, setBlob, setBoolean, setByte, setBytes, //
	setCharacterStream, setClob, setDate1, setDate2, setDouble, setFloat, setInt, setLong, //
	setNull1, setNull2, setObject1, setObject2, setObject3, setRef, setShort, setString, //
	setTime1, setTime2, setTimestamp1, setTimestamp2, setURL, setUnicodeStream; //

	/**
	 * @param methodAndArgsList的每一个元素(eachElementInList)是一个Object数组。每个数组包含两个元素：
	 * eachElementInList[0]:ParameterMethod
	 * eachElementInList[1]:ps.setXXX()的参数列表，组成见setParameter
	 */
	public static void setParameters(PreparedStatement stmt, List<Object[]> methodAndArgsList) throws SQLException {
		for (Object[] methodAndArgs : methodAndArgsList) {
			ParameterMethod pm = (ParameterMethod) methodAndArgs[0];
			Object[] args = (Object[]) methodAndArgs[1];
			pm.setParameter(stmt, args);
		}
	}

	public static void setParameters(PreparedStatement stmt, Map<Integer,ParameterContext> parameterSettings) throws SQLException {
		if (null != parameterSettings) {
			for (ParameterContext context : parameterSettings.values()) {
				context.getParameterMethod().setParameter(stmt, context.getArgs());
			}
		}
	}

	/**
	 * args[0]: index
	 * args[1..n] 参数
	 * @throws SQLException 
	 */
	@SuppressWarnings("deprecation")
	public void setParameter(PreparedStatement stmt, Object... args) throws SQLException {
		switch (this) {
		case setArray:
			stmt.setArray((Integer) args[0], (Array) args[1]);
			break;
		case setAsciiStream:
			stmt.setAsciiStream((Integer) args[0], (InputStream) args[1], (Integer) args[2]);
			break;
		case setBigDecimal:
			stmt.setBigDecimal((Integer) args[0], (BigDecimal) args[1]);
			break;
		case setBinaryStream:
			stmt.setBinaryStream((Integer) args[0], (InputStream) args[1], (Integer) args[2]);
			break;
		case setBlob:
			stmt.setBlob((Integer) args[0], (Blob) args[1]);
			break;
		case setBoolean:
			stmt.setBoolean((Integer) args[0], (Boolean) args[1]);
			break;
		case setByte:
			stmt.setByte((Integer) args[0], (Byte) args[1]);
			break;
		case setBytes:
			stmt.setBytes((Integer) args[0], (byte[]) args[1]);
			break;
		case setCharacterStream:
			stmt.setCharacterStream((Integer) args[0], (Reader) args[1], (Integer) args[2]);
			break;
		case setClob:
			stmt.setClob((Integer) args[0], (Clob) args[1]);
			break;
		case setDate1:
			stmt.setDate((Integer) args[0], (Date) args[1]);
			break;
		case setDate2:
			stmt.setDate((Integer) args[0], (Date) args[1], (Calendar) args[2]);
			break;
		case setDouble:
			stmt.setDouble((Integer) args[0], (Double) args[1]);
			break;
		case setFloat:
			stmt.setFloat((Integer) args[0], (Float) args[1]);
			break;
		case setInt:
			stmt.setInt((Integer) args[0], (Integer) args[1]);
			break;
		case setLong:
			stmt.setLong((Integer) args[0], (Long) args[1]);
			break;
		case setNull1:
			stmt.setNull((Integer) args[0], (Integer) args[1]);
			break;
		case setNull2:
			stmt.setNull((Integer) args[0], (Integer) args[1], (String) args[2]);
			break;
		case setObject1:
			stmt.setObject((Integer) args[0], args[1]);
			break;
		case setObject2:
			stmt.setObject((Integer) args[0], args[1], (Integer) args[2]);
			break;
		case setObject3:
			stmt.setObject((Integer) args[0], args[1], (Integer) args[2], (Integer) args[3]);
			break;
		case setRef:
			stmt.setRef((Integer) args[0], (Ref) args[1]);
			break;
		case setShort:
			stmt.setShort((Integer) args[0], (Short) args[1]);
			break;
		case setString:
			stmt.setString((Integer) args[0], (String) args[1]);
			break;
		case setTime1:
			stmt.setTime((Integer) args[0], (Time) args[1]);
			break;
		case setTime2:
			stmt.setTime((Integer) args[0], (Time) args[1], (Calendar) args[2]);
			break;
		case setTimestamp1:
			stmt.setTimestamp((Integer) args[0], (Timestamp) args[1]);
			break;
		case setTimestamp2:
			stmt.setTimestamp((Integer) args[0], (Timestamp) args[1], (Calendar) args[2]);
			break;
		case setURL:
			stmt.setURL((Integer) args[0], (URL) args[1]);
			break;
		case setUnicodeStream:
			stmt.setUnicodeStream((Integer) args[0], (InputStream) args[1], (Integer) args[2]);
			break;
		default:
			throw new IllegalArgumentException("Unhandled ParameterMethod:" + this.name());
		}
	}
}
