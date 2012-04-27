/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yangzhu
 * 
 */
public class Parameters {
	public static final Map<ParameterMethod, ParameterHandler> parameterHandlers =
		new HashMap<ParameterMethod, ParameterHandler>(30);

	static {
		parameterHandlers.put(ParameterMethod.setArray, new SetArrayHandler());
		parameterHandlers.put(ParameterMethod.setAsciiStream,
				new SetAsciiStreamHandler());
		parameterHandlers.put(ParameterMethod.setBigDecimal,
				new SetBigDecimalHandler());
		parameterHandlers.put(ParameterMethod.setBinaryStream,
				new SetBinaryStreamHandler());
		parameterHandlers.put(ParameterMethod.setBlob, new SetBlobHandler());
		parameterHandlers.put(ParameterMethod.setBoolean,
				new SetBooleanHandler());
		parameterHandlers.put(ParameterMethod.setByte, new SetByteHandler());
		parameterHandlers.put(ParameterMethod.setBytes, new SetBytesHandler());
		parameterHandlers.put(ParameterMethod.setCharacterStream,
				new SetCharacterStreamHandler());
		parameterHandlers.put(ParameterMethod.setClob, new SetClobHandler());
		parameterHandlers.put(ParameterMethod.setDate1, new SetDate1Handler());
		parameterHandlers.put(ParameterMethod.setDate2, new SetDate2Handler());
		parameterHandlers
				.put(ParameterMethod.setDouble, new SetDoubleHandler());
		parameterHandlers.put(ParameterMethod.setFloat, new SetFloatHandler());
		parameterHandlers.put(ParameterMethod.setInt, new SetIntHandler());
		parameterHandlers.put(ParameterMethod.setLong, new SetLongHandler());
		parameterHandlers.put(ParameterMethod.setNull1, new SetNull1Handler());
		parameterHandlers.put(ParameterMethod.setNull2, new SetNull2Handler());
		parameterHandlers.put(ParameterMethod.setObject1,
				new SetObject1Handler());
		parameterHandlers.put(ParameterMethod.setObject2,
				new SetObject2Handler());
		parameterHandlers.put(ParameterMethod.setObject3,
				new SetObject3Handler());
		parameterHandlers.put(ParameterMethod.setRef, new SetRefHandler());
		parameterHandlers.put(ParameterMethod.setShort, new SetShortHandler());
		parameterHandlers
				.put(ParameterMethod.setString, new SetStringHandler());
		parameterHandlers.put(ParameterMethod.setTime1, new SetTime1Handler());
		parameterHandlers.put(ParameterMethod.setTime2, new SetTime2Handler());
		parameterHandlers.put(ParameterMethod.setTimestamp1,
				new SetTimestamp1Handler());
		parameterHandlers.put(ParameterMethod.setTimestamp2,
				new SetTimestamp2Handler());
		parameterHandlers.put(ParameterMethod.setUnicodeStream,
				new SetUnicodeStreamHandler());
		parameterHandlers.put(ParameterMethod.setURL, new SetURLHandler());
	}
	
	public static void setParameters(PreparedStatement ps, Map<Integer, ParameterContext> parameterSettings) throws SQLException {
		for (ParameterContext context : parameterSettings.values()) {
			parameterHandlers.get(context.getParameterMethod()).setParameter(ps, context.getArgs());
		}
	}

}
