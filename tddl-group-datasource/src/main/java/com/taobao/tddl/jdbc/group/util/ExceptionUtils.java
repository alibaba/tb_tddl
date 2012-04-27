/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.tddl.jdbc.group.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.tddl.jdbc.group.parameter.ParameterContext;

public class ExceptionUtils {
	public static StackTraceElement split = new StackTraceElement("------- one sql exceptions-----", "", "", 0);
	public static final String SQL_EXECUTION_ERROR_CONTEXT_LOG = "SQL_EXECUTION_ERROR_CONTEXT_LOG";
	private static final String SQL_EXECUTION_ERROR_CONTEXT_MESSAGE = "SQLException ,context is ";
	private static final Log log = LogFactory.getLog(SQL_EXECUTION_ERROR_CONTEXT_LOG);
	public static void throwSQLException(List<SQLException> exceptions, String sql, List<Object> args) throws SQLException {
		if (exceptions != null && !exceptions.isEmpty()) {
			SQLException first = exceptions.get(0);
			if(sql != null){
				log.info(("TDDL SQL EXECUTE ERROR REPORTER:"+getErrorContext(sql, args,SQL_EXECUTION_ERROR_CONTEXT_MESSAGE)),first);
			}
			for (int i = 1, n = exceptions.size(); i < n; i++) {
				if(sql != null){
					log.info(("layer:"+n+"TDDL SQL EXECUTE ERROR REPORTER :"+getErrorContext(sql, args,SQL_EXECUTION_ERROR_CONTEXT_MESSAGE)),exceptions.get(i));
				}
			}
			throw mergeException(exceptions);
		}
	}
	public static List<SQLException> appendToExceptionList(List<SQLException> list,
			SQLException sqlException) {
		if (list == null) {
			list = new LinkedList<SQLException>();
		}
		list.add(sqlException);
		return list;

	}
	public static SQLException mergeException(List<SQLException> exceptions){
//		return new OneToManySQLExceptionsWrapper(exceptions);
		SQLException first = exceptions.get(0);
		List<StackTraceElement> stes = new ArrayList<StackTraceElement>(30*exceptions.size());
		//stes.addAll(Arrays.asList(first.getStackTrace()));
		boolean hasSplit = false;
		for(StackTraceElement ste : first.getStackTrace()){
			stes.add(ste);
			if(ste == split){
				hasSplit = true;
			}
		}
		if(!hasSplit){
			stes.add(split);
		}
		SQLException current = null;
		for (int i = 1, n = exceptions.size(); i < n; i++) {
			//newEx.setNextException(exceptions.get(i));
//			current.setNextException(exceptions.get(i));
			current = exceptions.get(i);
			//stes.addAll(Arrays.asList(exceptions.get(i).getStackTrace()));
			hasSplit = false;
			for(StackTraceElement ste : current.getStackTrace()){
				stes.add(ste);
				if(ste == split){
					hasSplit = true;
				}
			}
			if(!hasSplit){
				stes.add(split);
			}
		}
		//newEx.getCause();
		first.setStackTrace(stes.toArray(new StackTraceElement[stes.size()]));
		return first;
	}
	public static void throwSQLException(SQLException exception,String sql
			,List<Object> args)	throws SQLException {
			if(sql != null){
				log.info(("TDDL SQL EXECUTE ERROR REPORTER:"+getErrorContext(sql, args,SQL_EXECUTION_ERROR_CONTEXT_MESSAGE))+"nest Exceptions is "+exception.getMessage(),exception);
			}
			throw exception;
	}
	public static String getErrorContext(String sql,List<Object> arguments,String message){
		StringBuilder sb =new StringBuilder();
		sb.append(message)
		.append(sql).append("|||arguments:");
		printArgument(arguments, sb);
		return sb.toString();
	}
	
	private static void printArgument(List<Object> parameters,StringBuilder sb){
		int i = 0;
		if(parameters != null){
			for(Object param:parameters){
				
				sb.append("[index:").append(i).append("|parameter:").append(param)
				.append("|typeclass:").append(param==null?null:param.getClass().getName()).append("]");
				i++;
			}
		}else{
			sb.append("[empty]");
		}
	}
	public static void throwSQLException(List<SQLException> exceptions, String sql, Map<Integer, ParameterContext> parameter) throws SQLException {
		if (exceptions != null && !exceptions.isEmpty()) {
			SQLException first = exceptions.get(0);
			if(sql != null){
				log.info(("TDDL SQL EXECUTE ERROR REPORTER:"+getErrorContext(sql, parameter,SQL_EXECUTION_ERROR_CONTEXT_MESSAGE)),first);
			}
			for (int i = 1, n = exceptions.size(); i < n; i++) {
				if(sql != null){
					log.info(("layer:"+n+"TDDL SQL EXECUTE ERROR REPORTER :"+getErrorContext(sql, parameter,SQL_EXECUTION_ERROR_CONTEXT_MESSAGE)),exceptions.get(i));
				}
			}
			throw mergeException(exceptions);
		}
	}

	public static void throwSQLException(SQLException exception,String sql
			,Map<Integer, ParameterContext> parameter)	throws SQLException {
			if(sql != null){
				log.info(("TDDL SQL EXECUTE ERROR REPORTER:"+getErrorContext(sql, parameter,SQL_EXECUTION_ERROR_CONTEXT_MESSAGE))+"nest Exceptions is "+exception.getMessage(),exception);
			}
			throw exception;
	}
	public static String getErrorContext(String sql,Map<Integer, ParameterContext> parameter,String message){
		StringBuilder sb =new StringBuilder();
		sb.append(message)
		.append(sql).append("|||arguments:");
		printArgument(parameter, sb);
		return sb.toString();
	}
	
	private static void printArgument(Map<Integer, ParameterContext> parameter,StringBuilder sb){
		int i = 0;
		if(parameter != null){
			for(Object param:parameter.entrySet()){
				
				sb.append("[index:").append(i).append("|parameter:").append(param)
				.append("|typeclass:").append(param==null?null:param.getClass().getName()).append("]");
				i++;
			}
		}else{
			sb.append("[empty]");
		}
	}
	
	/**
	 * 打印sqlException 到error log里，这并不会阻断整个执行流程，但为了保证不丢失log,所以必须将异常打印出去
	 * 
	 * 打印后的异常list会被清空
	 * 
	 * @param logger
	 * @param message
	 * @param sqlExceptions
	 */
	public static void printSQLExceptionToErrorLog(Log logger,String message,List<SQLException> sqlExceptions){
		if(sqlExceptions != null && !sqlExceptions.isEmpty()){
			for(SQLException sqlException : sqlExceptions){
				logger.error(message,sqlException);
			}
			sqlExceptions.clear();
		}
	}
}
