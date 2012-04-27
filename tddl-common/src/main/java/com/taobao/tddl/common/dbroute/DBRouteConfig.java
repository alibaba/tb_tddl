/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//package com.taobao.tddl.common.dbroute;
//
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.StringTokenizer;
//
//import com.taobao.tddl.common.exception.DBRouterException;
//
///**
// * 数据库分库的查找搜寻策略配置文件管理器
// * 
// */
//public class DBRouteConfig {
//	private String configLocation;
//	private List<DBRouteRule> routingRules = new ArrayList<DBRouteRule>();
//	private List<String> allNodeNameList = new ArrayList<String>();
//	private List<String> defaultNodeNameList = new ArrayList<String>();
//	private Map<String, String> statementRuleMap = new HashMap<String, String>();
//	private Map<String, Properties> dbRuleMap = new HashMap<String, Properties>();
//
//	/**
//	 * 根据路由策略返回该路由指定的数据库 直接根据客户端指定的DBRoute决定应该被查询的数据库
//	 * 
//	 * @param DBRoute
//	 * 
//	 * @return List
//	 * 
//	 * @throws DBRouterException
//	 */
//	public List<String> routingDB(DBRoute dbRoute) throws DBRouterException {
//		if (null == dbRoute) {
//			return null;
//		}
//
//		List<String> nodeNameList = new ArrayList<String>();
//
//		if (dbRoute.getRoutingStrategy() == DBRoute.BY_XID) {
//			String xid = dbRoute.getXid();
//
//			if (xid != null) {
//				if (xid.indexOf(",") != -1) {
//					StringTokenizer st = new StringTokenizer(xid, ",");
//
//					while (st.hasMoreTokens()) {
//						String dbxid = st.nextToken();
//
//						if (allNodeNameList.contains(dbxid)) {
//							nodeNameList.add(dbxid);
//						}
//					}
//
//					return nodeNameList;
//				} else if (allNodeNameList.contains(xid)) {
//					nodeNameList.add(xid);
//					return nodeNameList;
//				}
//			}
//		}
//		//尽量不使用字串id
//		if (dbRoute.getRoutingStrategy() == DBRoute.BY_USER) {
//			String userId = dbRoute.getUserId();
//
//			if (userId != null) {
//				for (DBRouteRule rr : routingRules) {
//
//					if (rr.getRuleBaseName().equals("UserId")) {
//						if (rr.isMatched(userId)) {
//							nodeNameList.add(rr.getdbName());
//							return nodeNameList;
//						}
//					}
//				}
//			}
//		}
//
//		if (dbRoute.getRoutingStrategy() == DBRoute.BY_USER_LONG) {
//			long userId = dbRoute.getUserIdLong();
//
//			if (userId != DBRoute.DEFAULT_USER_ID_LONG) {
//				for (DBRouteRule rr : routingRules) {
//
//					if (rr.getRuleBaseName().equals("UserIdL")) {
//						if (rr.isMatched(userId)) {
//							nodeNameList.add(rr.getdbName());
//							return nodeNameList;
//						}
//					}
//				}
//			}
//		}
//
//		return null;
//	}
//
//	/**
//	 * 根据查询的SQLMAPING ID 查找属于哪个数据库
//	 * 
//	 * @param statement
//	 * 
//	 * @return
//	 * 
//	 * @throws DBRouterException
//	 */
//	public List<String> routingDB(String statement) throws DBRouterException {
//		if (statement == null) {
//			return null;
//		}
//
//		String xid = (String) statementRuleMap.get(statement);
//
//		if (xid != null) {
//			List<String> nodeNameList = new ArrayList<String>();
//
//			if (xid.indexOf(",") != -1) {
//				StringTokenizer st = new StringTokenizer(xid, ",");
//
//				while (st.hasMoreTokens()) {
//					String dbxid = st.nextToken();
//
//					if (allNodeNameList.contains(dbxid)) {
//						nodeNameList.add(dbxid);
//					}
//				}
//
//				return nodeNameList;
//			} else if (allNodeNameList.contains(xid)) {
//				nodeNameList.add(xid);
//				return nodeNameList;
//			}
//		}
//
//		return null;
//	}
//
//	/**
//	 * 决定数据库路由的主入口，一般客户端均调用此方法返回应该查询的数据库。
//	 * 
//	 * @param dbRoute
//	 * @param statement
//	 * 
//	 * @return
//	 * 
//	 * @throws DBRouterException
//	 */
//	public List<String> routingDB(DBRoute dbRoute, String statement)
//			throws DBRouterException {
//		List<String> nodeNameListByNodeRule = routingDB(dbRoute);
//
//		if ((nodeNameListByNodeRule != null)
//				&& !nodeNameListByNodeRule.isEmpty()) {
//			return nodeNameListByNodeRule;
//		}
//
//		List<String> nodeNameListByStatementRule = routingDB(statement);
//
//		if (nodeNameListByStatementRule != null) {
//			return nodeNameListByStatementRule;
//		}
//
//		return defaultNodeNameList;
//	}
//
//	/**
//	 * 初始化方法，此处将规则进行初始化
//	 * 
//	 * @throws DBRouterException
//	 */
//	public void init() throws DBRouterException {
//		// 初始化配置文件中指定的规则集合
//		if (dbRuleMap.size() == 0) {
//			return;
//		}
//
//		for (Map.Entry<String, Properties> entry : dbRuleMap.entrySet()) {
//
//			String dbKey = entry.getKey();
//
//			Properties element = entry.getValue();
//
//			for (Map.Entry<Object, Object> subEntry : element.entrySet()) {
//
//				String ruleKey = (String) subEntry.getKey();
//				String rule = (String) subEntry.getValue();
//
//				DBRouteRule dbRule = new DBRouteRule(dbKey, ruleKey, rule);
//
//				routingRules.add(dbRule);
//			}
//		}
//		if (configLocation == null) {
//			throw new DBRouterException("未指定statement--路由器映射xml文件");
//		}
//		configLocation=configLocation.trim();
//		if(configLocation.equals("")){
//			throw new DBRouterException("未指定statement--路由器映射xml文件");
//		}
//		InputStream in=null;
//			in =getClass().getResourceAsStream(configLocation);
//			// 初始化SQL语句映射集合
//			statementRuleMap = DBRouteConfigBuilder
//					.buildRouteMap(in);
//	}
//
//	/**
//	 * @return Returns the allNodeNameList.
//	 */
//	public List<String> getAllNodeNameList() {
//		return allNodeNameList;
//	}
//
//	/**
//	 * @param allNodeNameList
//	 *            The allNodeNameList to set.
//	 */
//	public void setAllNodeNameList(List<String> allNodeNameList) {
//		this.allNodeNameList = allNodeNameList;
//	}
//
//	/**
//	 * @return Returns the defaultNodeNameList.
//	 */
//	public List<String> getDefaultNodeNameList() {
//		return defaultNodeNameList;
//	}
//
//	/**
//	 * @param defaultNodeNameList
//	 *            The defaultNodeNameList to set.
//	 */
//	public void setDefaultNodeNameList(List<String> defaultNodeNameList) {
//		this.defaultNodeNameList = defaultNodeNameList;
//	}
//
//	/**
//	 * @return Returns the dbRuleMap.
//	 */
//	public Map<String, Properties> getDbRuleMap() {
//		return dbRuleMap;
//	}
//
//	/**
//	 * @param dbRuleMap
//	 *            The dbRuleMap to set.
//	 */
//	public void setDbRuleMap(Map<String, Properties> dbRuleMap) {
//		this.dbRuleMap = dbRuleMap;
//	}
//
//	/**
//	 * @param configLocation
//	 *            The configLocation to set.
//	 */
//	public void setConfigLocation(String configLocation) {
//		this.configLocation = configLocation;
//	}
//}
