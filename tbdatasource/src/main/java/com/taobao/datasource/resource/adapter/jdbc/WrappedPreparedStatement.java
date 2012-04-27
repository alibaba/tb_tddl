/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.taobao.datasource.resource.adapter.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
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
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * A wrapper for a prepared statement.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 57189 $
 */
public class WrappedPreparedStatement extends WrappedStatement implements PreparedStatement {

	private final PreparedStatement ps;

	public WrappedPreparedStatement(final WrappedConnection lc, final PreparedStatement ps) {
		super(lc, ps);
		this.ps = ps;
	}

	public Statement getUnderlyingStatement() throws SQLException {
		checkState();
		if (ps instanceof CachedPreparedStatement) {
			return ((CachedPreparedStatement) ps).getUnderlyingPreparedStatement();
		} else {
			return ps;
		}
	}

	public void setBoolean(int parameterIndex, boolean value) throws SQLException {
		checkState();
		try {
			ps.setBoolean(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setByte(int parameterIndex, byte value) throws SQLException {
		checkState();
		try {
			ps.setByte(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setShort(int parameterIndex, short value) throws SQLException {
		checkState();
		try {
			ps.setShort(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setInt(int parameterIndex, int value) throws SQLException {
		checkState();
		try {
			ps.setInt(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setLong(int parameterIndex, long value) throws SQLException {
		checkState();
		try {
			ps.setLong(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setFloat(int parameterIndex, float value) throws SQLException {
		checkState();
		try {
			ps.setFloat(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setDouble(int parameterIndex, double value) throws SQLException {
		checkState();
		try {
			ps.setDouble(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setURL(int parameterIndex, URL value) throws SQLException {
		checkState();
		try {
			ps.setURL(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setTime(int parameterIndex, Time value) throws SQLException {
		checkState();
		try {
			ps.setTime(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setTime(int parameterIndex, Time value, Calendar calendar) throws SQLException {
		checkState();
		try {
			ps.setTime(parameterIndex, value, calendar);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public boolean execute() throws SQLException {
		checkTransaction();
		try {
			checkConfiguredQueryTimeout();
			return ps.execute();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		checkState();
		try {
			return ps.getMetaData();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public ResultSet executeQuery() throws SQLException {
		checkTransaction();
		try {
			checkConfiguredQueryTimeout();
			ResultSet resultSet = ps.executeQuery();
			return registerResultSet(resultSet);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public int executeUpdate() throws SQLException {
		checkTransaction();
		try {
			checkConfiguredQueryTimeout();
			return ps.executeUpdate();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void addBatch() throws SQLException {
		checkState();
		try {
			ps.addBatch();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		checkState();
		try {
			ps.setNull(parameterIndex, sqlType);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
		checkState();
		try {
			ps.setNull(parameterIndex, sqlType, typeName);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setBigDecimal(int parameterIndex, BigDecimal value) throws SQLException {
		checkState();
		try {
			ps.setBigDecimal(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setString(int parameterIndex, String value) throws SQLException {
		checkState();
		try {
			ps.setString(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setBytes(int parameterIndex, byte[] value) throws SQLException {
		checkState();
		try {
			ps.setBytes(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setDate(int parameterIndex, Date value) throws SQLException {
		checkState();
		try {
			ps.setDate(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setDate(int parameterIndex, Date value, Calendar calendar) throws SQLException {
		checkState();
		try {
			ps.setDate(parameterIndex, value, calendar);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setTimestamp(int parameterIndex, Timestamp value) throws SQLException {
		checkState();
		try {
			ps.setTimestamp(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setTimestamp(int parameterIndex, Timestamp value, Calendar calendar) throws SQLException {
		checkState();
		try {
			ps.setTimestamp(parameterIndex, value, calendar);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	/**
	 * @deprecated
	 */
	public void setAsciiStream(int parameterIndex, InputStream stream, int length) throws SQLException {
		checkState();
		try {
			ps.setAsciiStream(parameterIndex, stream, length);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	/**
	 * @deprecated
	 */
	public void setUnicodeStream(int parameterIndex, InputStream stream, int length) throws SQLException {
		checkState();
		try {
			ps.setUnicodeStream(parameterIndex, stream, length);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setBinaryStream(int parameterIndex, InputStream stream, int length) throws SQLException {
		checkState();
		try {
			ps.setBinaryStream(parameterIndex, stream, length);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void clearParameters() throws SQLException {
		checkState();
		try {
			ps.clearParameters();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setObject(int parameterIndex, Object value, int sqlType, int scale) throws SQLException {
		checkState();
		try {
			ps.setObject(parameterIndex, value, sqlType, scale);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setObject(int parameterIndex, Object value, int sqlType) throws SQLException {
		checkState();
		try {
			ps.setObject(parameterIndex, value, sqlType);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setObject(int parameterIndex, Object value) throws SQLException {
		checkState();
		try {
			ps.setObject(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
		checkState();
		try {
			ps.setCharacterStream(parameterIndex, reader, length);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setRef(int parameterIndex, Ref value) throws SQLException {
		checkState();
		try {
			ps.setRef(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setBlob(int parameterIndex, Blob value) throws SQLException {
		checkState();
		try {
			ps.setBlob(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setClob(int parameterIndex, Clob value) throws SQLException {
		checkState();
		try {
			ps.setClob(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setArray(int parameterIndex, Array value) throws SQLException {
		checkState();
		try {
			ps.setArray(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public ParameterMetaData getParameterMetaData() throws SQLException {
		checkState();
		try {
			return ps.getParameterMetaData();
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setRowId(int parameterIndex, RowId x) throws SQLException {
		checkState();
		try {
			ps.setRowId(parameterIndex, x);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setNString(int parameterIndex, String value) throws SQLException {
		checkState();
		try {
			ps.setNString(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
		checkState();
		try {
			ps.setNCharacterStream(parameterIndex, value, length);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setNClob(int parameterIndex, NClob value) throws SQLException {
		checkState();
		try {
			ps.setNClob(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
		checkState();
		try {
			ps.setClob(parameterIndex, reader, length);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
		checkState();
		try {
			ps.setBlob(parameterIndex, inputStream, length);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
		checkState();
		try {
			ps.setNClob(parameterIndex, reader, length);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
		checkState();
		try {
			ps.setSQLXML(parameterIndex, xmlObject);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
		checkState();
		try {
			ps.setAsciiStream(parameterIndex, x, length);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
		checkState();
		try {
			ps.setBinaryStream(parameterIndex, x, length);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
		checkState();
		try {
			ps.setCharacterStream(parameterIndex, reader, length);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
		checkState();
		try {
			ps.setAsciiStream(parameterIndex, x);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
		checkState();
		try {
			ps.setBinaryStream(parameterIndex, x);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
		checkState();
		try {
			ps.setCharacterStream(parameterIndex, reader);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
		checkState();
		try {
			ps.setNCharacterStream(parameterIndex, value);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setClob(int parameterIndex, Reader reader) throws SQLException {
		checkState();
		try {
			ps.setClob(parameterIndex, reader);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
		checkState();
		try {
			ps.setBlob(parameterIndex, inputStream);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}

	public void setNClob(int parameterIndex, Reader reader) throws SQLException {
		checkState();
		try {
			ps.setNClob(parameterIndex, reader);
		} catch (Throwable t) {
			throw checkException(t);
		}
	}
}
