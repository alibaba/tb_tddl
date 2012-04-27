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
package org.jboss.resource.adapter.jdbc.vendor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import org.jboss.logging.Logger;

import com.taobao.datasource.resource.adapter.jdbc.ValidConnectionChecker;

/**
 * Implements check valid connection sql Requires MySQL driver 3.1.8 or later.
 * This should work on just about any version of the database itself but will
 * only be "fast" on version 3.22.1 and later. Prior to that version it just
 * does "SELECT 1" anyhow.
 * 
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author <a href="mailto:acoliver ot jbosss dat org">Andrew C. Oliver</a>
 * @author <a href="mailto:jim.moran@jboss.org">Jim Moran</a>
 * @version $Revision: 57189 $
 */
public class MySQLValidConnectionChecker implements ValidConnectionChecker, Serializable {
	private static final long serialVersionUID = -2227528634302168878L;

	private static final Logger log = Logger.getLogger(MySQLValidConnectionChecker.class);

	private Method ping;
	private boolean driverHasPingMethod = false;

	// The timeout (apparently the timeout is ignored?)
	private static Object[] params = new Object[] {};

	public MySQLValidConnectionChecker() {
		try {
			Class mysqlConnection = Thread.currentThread().getContextClassLoader().loadClass("com.mysql.jdbc.Connection");
			ping = mysqlConnection.getMethod("ping", new Class[] {});
			if (ping != null) {
				driverHasPingMethod = true;
			}
		} catch (Exception e) {
			log.warn("Cannot resolve com.mysq.jdbc.Connection.ping method.  Will use 'SELECT 1' instead.", e);
		}
	}

	public SQLException isValidConnection(Connection c) {
		//if there is a ping method then use it, otherwise just use a 'SELECT 1' statement
		if (driverHasPingMethod) {
			try {
				ping.invoke(c, params);
			} catch (Exception e) {
				if (e instanceof SQLException) {
					return (SQLException) e;
				} else {
					log.warn("Unexpected error in ping", e);
					return new SQLException("ping failed: " + e.toString());
				}
			}
			
		} else {
			
			Statement stmt = null;
                        ResultSet rs = null;
			try {
				stmt = c.createStatement();
				rs = stmt.executeQuery("SELECT 1");
			} catch (Exception e) {
				if (e instanceof SQLException) {
					return (SQLException) e;
				} else {
					log.warn("Unexpected error in ping (SELECT 1)", e);
					return new SQLException("ping (SELECT 1) failed: " + e.toString());
				}	
			} finally {
				//cleanup the Statment
				try {
                                        if (rs != null) rs.close();
					if (stmt != null) stmt.close();
				} catch (SQLException e) {
				}
			}
			
		}
		return null;
	}
}
