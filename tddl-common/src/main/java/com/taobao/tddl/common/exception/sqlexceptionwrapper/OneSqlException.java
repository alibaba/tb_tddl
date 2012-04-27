/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//package com.taobao.tddl.common.exception.sqlexceptionwrapper;
//
//import java.sql.SQLException;
//
//import javax.management.RuntimeErrorException;
//
//public class OneSqlException extends SQLException{
//	/**
//	 * serialVersionUID
//	 */
//	private static final long serialVersionUID = -8365885110000094432L;
//	private final SQLException currentSQLException,nextSQLException;
//	private final int index ;
//	public OneSqlException(int index,SQLException currentSqlException,SQLException nextSqlException) {
//		if(currentSqlException == null){
//			throw new RuntimeException("current sql exception could not be null");
//		}
//		this.index = index;
//		this.currentSQLException = currentSqlException;
//		this.nextSQLException = nextSqlException;
//	}
//	public String getSQLState() {
//		return currentSQLException.getSQLState();
//	}
//
//	public int getErrorCode() {
//		return currentSQLException.getErrorCode();
//	}
//
//	public SQLException getNextException() {
//		return currentSQLException.getNextException();
//	}
//
//	public void setNextException(SQLException ex) {
//		currentSQLException.setNextException(ex);
//	}
//	
//
//	public Throwable getCause() {
//		if(nextSQLException != null){
//			return nextSQLException;
//		}else{
//			return currentSQLException.getCause();
//		}
//	}
//
//	public String getMessage() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("merge SQLException layer ").append(index).append(" : ").append(currentSQLException.getMessage());
//		return sb.toString();
//	}
//}
