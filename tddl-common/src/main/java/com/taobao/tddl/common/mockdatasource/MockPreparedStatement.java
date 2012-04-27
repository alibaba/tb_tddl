/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.common.mockdatasource;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.tddl.common.mockdatasource.MockDataSource.ExecuteInfo;
import com.taobao.tddl.common.mockdatasource.param.ParameterContext;
import com.taobao.tddl.common.mockdatasource.param.ParameterHandler;
import com.taobao.tddl.common.mockdatasource.param.ParameterMethod;
import com.taobao.tddl.common.mockdatasource.param.SetArrayHandler;
import com.taobao.tddl.common.mockdatasource.param.SetAsciiStreamHandler;
import com.taobao.tddl.common.mockdatasource.param.SetBigDecimalHandler;
import com.taobao.tddl.common.mockdatasource.param.SetBinaryStreamHandler;
import com.taobao.tddl.common.mockdatasource.param.SetBlobHandler;
import com.taobao.tddl.common.mockdatasource.param.SetBooleanHandler;
import com.taobao.tddl.common.mockdatasource.param.SetByteHandler;
import com.taobao.tddl.common.mockdatasource.param.SetBytesHandler;
import com.taobao.tddl.common.mockdatasource.param.SetCharacterStreamHandler;
import com.taobao.tddl.common.mockdatasource.param.SetClobHandler;
import com.taobao.tddl.common.mockdatasource.param.SetDate1Handler;
import com.taobao.tddl.common.mockdatasource.param.SetDate2Handler;
import com.taobao.tddl.common.mockdatasource.param.SetDoubleHandler;
import com.taobao.tddl.common.mockdatasource.param.SetFloatHandler;
import com.taobao.tddl.common.mockdatasource.param.SetIntHandler;
import com.taobao.tddl.common.mockdatasource.param.SetLongHandler;
import com.taobao.tddl.common.mockdatasource.param.SetNull1Handler;
import com.taobao.tddl.common.mockdatasource.param.SetNull2Handler;
import com.taobao.tddl.common.mockdatasource.param.SetObject1Handler;
import com.taobao.tddl.common.mockdatasource.param.SetObject2Handler;
import com.taobao.tddl.common.mockdatasource.param.SetObject3Handler;
import com.taobao.tddl.common.mockdatasource.param.SetRefHandler;
import com.taobao.tddl.common.mockdatasource.param.SetShortHandler;
import com.taobao.tddl.common.mockdatasource.param.SetStringHandler;
import com.taobao.tddl.common.mockdatasource.param.SetTime1Handler;
import com.taobao.tddl.common.mockdatasource.param.SetTime2Handler;
import com.taobao.tddl.common.mockdatasource.param.SetTimestamp1Handler;
import com.taobao.tddl.common.mockdatasource.param.SetTimestamp2Handler;
import com.taobao.tddl.common.mockdatasource.param.SetURLHandler;
import com.taobao.tddl.common.mockdatasource.param.SetUnicodeStreamHandler;

public class MockPreparedStatement extends MockStatement implements PreparedStatement{
	private static final Log log = LogFactory.getLog(MockPreparedStatement.class);

	public MockPreparedStatement(String method, MockDataSource mockDataSource, String sql) {
		super(method, mockDataSource, sql);
	}
	
	private static final Map<ParameterMethod, ParameterHandler> parameterHandlers = new HashMap<ParameterMethod, ParameterHandler>(30);

	static {
		parameterHandlers.put(ParameterMethod.setArray, new SetArrayHandler());
		parameterHandlers.put(ParameterMethod.setAsciiStream, new SetAsciiStreamHandler());
		parameterHandlers.put(ParameterMethod.setBigDecimal, new SetBigDecimalHandler());
		parameterHandlers.put(ParameterMethod.setBinaryStream, new SetBinaryStreamHandler());
		parameterHandlers.put(ParameterMethod.setBlob, new SetBlobHandler());
		parameterHandlers.put(ParameterMethod.setBoolean, new SetBooleanHandler());
		parameterHandlers.put(ParameterMethod.setByte, new SetByteHandler());
		parameterHandlers.put(ParameterMethod.setBytes, new SetBytesHandler());
		parameterHandlers.put(ParameterMethod.setCharacterStream, new SetCharacterStreamHandler());
		parameterHandlers.put(ParameterMethod.setClob, new SetClobHandler());
		parameterHandlers.put(ParameterMethod.setDate1, new SetDate1Handler());
		parameterHandlers.put(ParameterMethod.setDate2, new SetDate2Handler());
		parameterHandlers.put(ParameterMethod.setDouble, new SetDoubleHandler());
		parameterHandlers.put(ParameterMethod.setFloat, new SetFloatHandler());
		parameterHandlers.put(ParameterMethod.setInt, new SetIntHandler());
		parameterHandlers.put(ParameterMethod.setLong, new SetLongHandler());
		parameterHandlers.put(ParameterMethod.setNull1, new SetNull1Handler());
		parameterHandlers.put(ParameterMethod.setNull2, new SetNull2Handler());
		parameterHandlers.put(ParameterMethod.setObject1, new SetObject1Handler());
		parameterHandlers.put(ParameterMethod.setObject2, new SetObject2Handler());
		parameterHandlers.put(ParameterMethod.setObject3, new SetObject3Handler());
		parameterHandlers.put(ParameterMethod.setRef, new SetRefHandler());
		parameterHandlers.put(ParameterMethod.setShort, new SetShortHandler());
		parameterHandlers.put(ParameterMethod.setString, new SetStringHandler());
		parameterHandlers.put(ParameterMethod.setTime1, new SetTime1Handler());
		parameterHandlers.put(ParameterMethod.setTime2, new SetTime2Handler());
		parameterHandlers.put(ParameterMethod.setTimestamp1, new SetTimestamp1Handler());
		parameterHandlers.put(ParameterMethod.setTimestamp2, new SetTimestamp2Handler());
		parameterHandlers.put(ParameterMethod.setUnicodeStream, new SetUnicodeStreamHandler());
		parameterHandlers.put(ParameterMethod.setURL, new SetURLHandler());
	}


	private int autoGeneratedKeys = -1;

	private int[] columnIndexes;

	private String[] columnNames;

	private Map<Integer, ParameterContext> parameterSettings = new TreeMap<Integer, ParameterContext>();

	private PreparedStatement prepareStatementInternal(Connection connection, String targetSql)
			throws SQLException {
		PreparedStatement ps;
		if (getResultSetType() != -1 && getResultSetConcurrency() != -1 && getResultSetHoldability() != -1) {
			ps = connection.prepareStatement(targetSql, getResultSetType(), getResultSetConcurrency(), getResultSetHoldability());
		} else if (getResultSetType() != -1 && getResultSetConcurrency() != -1) {
			ps = connection.prepareStatement(targetSql, getResultSetType(), getResultSetConcurrency());
		} else if (autoGeneratedKeys != -1) {
			ps = connection.prepareStatement(targetSql, autoGeneratedKeys);
		} else if (columnIndexes != null) {
			ps = connection.prepareStatement(targetSql, columnIndexes);
		} else if (columnNames != null) {
			ps = connection.prepareStatement(targetSql, columnNames);
		} else {
			ps = connection.prepareStatement(targetSql);
		}

		return ps;
	}

	private void changeParameters(Map<Integer, Object> changedParameters) {
		for (Map.Entry<Integer, Object> entry : changedParameters.entrySet()) {
			// 注意：SQL解析那边绑定参数从0开始计数，因此需要加1。
			ParameterContext context = parameterSettings.get(entry.getKey() + 1);
			if (context.getParameterMethod() != ParameterMethod.setNull1
					&& context.getParameterMethod() != ParameterMethod.setNull2) {
				context.getArgs()[1] = entry.getValue();
			}
		}
	}

	private void setParameters(PreparedStatement ps) throws SQLException {
		for (ParameterContext context : parameterSettings.values()) {
			parameterHandlers.get(context.getParameterMethod()).setParameter(ps, context.getArgs());
		}
	}

	private List<Object> getParameters() {
		List<Object> parameters = new ArrayList<Object>();
		for (ParameterContext context : parameterSettings.values()) {
			if (context.getParameterMethod() != ParameterMethod.setNull1
					&& context.getParameterMethod() != ParameterMethod.setNull2) {
				parameters.add(context.getArgs()[1]);
			} else {
				parameters.add(null);
			}
		}

		return parameters;
	}

	public void clearParameters() throws SQLException {
		parameterSettings.clear();
	}

	public boolean execute() throws SQLException {
		checkClosed();
		return executerHandler.executeSql("ps.execute", sql);
		}

	public ResultSet executeQuery() throws SQLException {
		if (log.isDebugEnabled()) {
			log.debug("invoke executeQuery, sql = " + sql);
		}
		
		checkClosed();

		SQLException e = MockDataSource.popPreException(MockDataSource.m_executeQuery);
		if(e!=null){
			throw e;
		}
		
		return executerHandler.execute("ps.executeQuery", sql);
	}

	public int executeUpdate() throws SQLException {
		if (log.isDebugEnabled()) {
			log.debug("invoke executeUpdate, sql = " + sql);
		}

		checkClosed();

		SQLException e = MockDataSource.popPreException(MockDataSource.m_executeUpdate);
		if(e!=null){
			throw e;
		}

		return super.updateInternal("ps.executeUpdate", sql);
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		throw new UnsupportedOperationException("getMetaData");
	}

	public ParameterMetaData getParameterMetaData() throws SQLException {
		throw new UnsupportedOperationException("getParameterMetaData");
	}

	public void setArray(int i, Array x) throws SQLException {
		parameterSettings.put(i, new ParameterContext(ParameterMethod.setArray, new Object[] {i, x}));
	}

	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setAsciiStream, new Object[] {parameterIndex, x, length}));
	}

	public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setBigDecimal, new Object[] {parameterIndex, x}));
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setBinaryStream, new Object[] {parameterIndex, x, length}));
	}

	public void setBlob(int i, Blob x) throws SQLException {
		parameterSettings.put(i, new ParameterContext(ParameterMethod.setBlob, new Object[] {i, x}));
	}

	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setBoolean, new Object[] {parameterIndex, x}));
	}

	public void setByte(int parameterIndex, byte x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setByte, new Object[] {parameterIndex, x}));
	}

	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setBytes, new Object[] {parameterIndex, x}));
	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setCharacterStream, new Object[] {parameterIndex, reader, length}));
	}

	public void setClob(int i, Clob x) throws SQLException {
		parameterSettings.put(i, new ParameterContext(ParameterMethod.setClob, new Object[] {i, x}));
	}

	public void setDate(int parameterIndex, Date x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setDate1, new Object[] {parameterIndex, x}));
	}

	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setDate2, new Object[] {parameterIndex, x, cal}));
	}

	public void setDouble(int parameterIndex, double x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setDouble, new Object[] {parameterIndex, x}));
	}

	public void setFloat(int parameterIndex, float x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setFloat, new Object[] {parameterIndex, x}));
	}

	public void setInt(int parameterIndex, int x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setInt, new Object[] {parameterIndex, x}));
	}

	public void setLong(int parameterIndex, long x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setLong, new Object[] {parameterIndex, x}));
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setNull1, new Object[] {parameterIndex, sqlType}));
	}

	public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
		parameterSettings.put(paramIndex, new ParameterContext(ParameterMethod.setNull2, new Object[] {paramIndex, sqlType, typeName}));
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setObject1, new Object[] {parameterIndex, x}));
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setObject2, new Object[] {parameterIndex, x, targetSqlType}));
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setObject3, new Object[] {parameterIndex, x, targetSqlType, scale}));
	}

	public void setRef(int i, Ref x) throws SQLException {
		parameterSettings.put(i, new ParameterContext(ParameterMethod.setRef, new Object[] {i, x}));
	}

	public void setShort(int parameterIndex, short x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setShort, new Object[] {parameterIndex, x}));
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setString, new Object[] {parameterIndex, x}));
	}

	public void setTime(int parameterIndex, Time x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setTime1, new Object[] {parameterIndex, x}));
	}

	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setTime2, new Object[] {parameterIndex, x, cal}));
	}

	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setTimestamp1, new Object[] {parameterIndex, x}));
	}

	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setTimestamp2, new Object[] {parameterIndex, x, cal}));
	}

	public void setURL(int parameterIndex, URL x) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setURL, new Object[] {parameterIndex, x}));
	}

	@Deprecated
	public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
		parameterSettings.put(parameterIndex, new ParameterContext(ParameterMethod.setUnicodeStream, new Object[] {parameterIndex, x, length}));
	}

	List<Object> batchedArgs;
	public void addBatch() throws SQLException {
		if (batchedArgs == null) {
			batchedArgs = new ArrayList<Object>();
		}

		List<ParameterContext> batchedParameterSettings = new ArrayList<ParameterContext>();
		batchedParameterSettings.addAll(parameterSettings.values());

		batchedArgs.add(batchedParameterSettings);
	}

	

	private static void setBatchParameters(PreparedStatement ps, List<ParameterContext> batchedParameters) throws SQLException {
		for (ParameterContext context : batchedParameters) {
			parameterHandlers.get(context.getParameterMethod()).setParameter(ps, context.getArgs());
		}
	}

	private static List<Object> getBatchParameters(List<ParameterContext> batchedParameters) {
		List<Object> parameters = new ArrayList<Object>();
		for (ParameterContext context : batchedParameters) {
			parameters.add(context.getArgs()[1]);
		}

		return parameters;
	}



	@SuppressWarnings("unchecked")
	public int[] executeBatch() throws SQLException {
//		int[] ints = new int[batchedArgs.size()];
//		for(int i = 0 ; i <ints.length ; i ++){
//			ints[i] = executeUpdate();
//		}
//		return ints;
		mds.checkState();
		MockDataSource.record(new ExecuteInfo(this.mds, "executeBatch", this.sql, null));
		return new int[] { -1, -1 };
	}

	public void clearBatch() throws SQLException {
		super.clearBatch();
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public int getAutoGeneratedKeys() {
		return autoGeneratedKeys;
	}

	public void setAutoGeneratedKeys(int autoGeneratedKeys) {
		this.autoGeneratedKeys = autoGeneratedKeys;
	}

	public int[] getColumnIndexes() {
		return columnIndexes;
	}

	public void setColumnIndexes(int[] columnIndexes) {
		this.columnIndexes = columnIndexes;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	public void setPoolable(boolean poolable) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public boolean isPoolable() throws SQLException
	{
		// TODO Auto-generated method stub
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setRowId(int parameterIndex, RowId x) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setNString(int parameterIndex, String value)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setNCharacterStream(int parameterIndex, Reader value,
			long length) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setClob(int parameterIndex, Reader reader, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setNClob(int parameterIndex, Reader reader, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setSQLXML(int parameterIndex, SQLXML xmlObject)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setAsciiStream(int parameterIndex, InputStream x, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setBinaryStream(int parameterIndex, InputStream x, long length)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setCharacterStream(int parameterIndex, Reader reader,
			long length) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setAsciiStream(int parameterIndex, InputStream x)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setBinaryStream(int parameterIndex, InputStream x)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setCharacterStream(int parameterIndex, Reader reader)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setNCharacterStream(int parameterIndex, Reader value)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setBlob(int parameterIndex, InputStream inputStream)
			throws SQLException
	{
		// TODO Auto-generated method stub
		
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException
	{
		// TODO Auto-generated method stub
		
	}
}
