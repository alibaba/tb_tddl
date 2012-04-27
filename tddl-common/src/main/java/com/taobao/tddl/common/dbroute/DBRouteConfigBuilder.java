/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	//package com.taobao.tddl.common.dbroute;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//
//import org.springframework.core.io.ClassPathResource;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NamedNodeMap;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
//import com.taobao.tddl.common.exception.DBRouterException;
//
//
//
//public class DBRouteConfigBuilder {
//	private static final String NODE_ROUTE = "sqlRoute";
//	private static final String NODE_SQL = "sql";
//	private static boolean validationEnabled;
//	private static Map<String, String> dbRouteMap = new HashMap<String, String>();
//
//	public static Map<String, String> buildRouteMap(InputStream reader)
//			throws DBRouterException {
//		// Parse input file
//		if (reader == null) {
//			throw new DBRouterException(
//					"The reader passed to SqlRoute is null!");
//		}
//
//		try {
//			Document doc = getDoc(reader);
//
//			Element rootElement = (Element) doc.getLastChild();
//
//			return parseSqlRouteConfig(rootElement);
//		} catch (Exception e) {
//			throw new DBRouterException(
//					"There was an error while building the SqlRoute Parse.", e);
//		}
//	}
//
//	/**
//	 * @param rootElement
//	 * 
//	 * @return
//	 * @throws DBRouterException
//	 */
//	private static Map<String, String> parseSqlRouteConfig(Element rootElement)
//			throws DBRouterException {
//		NodeList children = rootElement.getChildNodes();
//
//		for (int i = 0; i < children.getLength(); i++) {
//			Node child = children.item(i);
//
//			if (child.getNodeType() == Node.ELEMENT_NODE) {
//				if (NODE_ROUTE.equals(child.getNodeName())) {
//					parseRoute(child);
//				}
//			}
//		}
//
//		return dbRouteMap;
//	}
//
//	/**
//	 * @param child
//	 */
//	private static void parseSqlRoute(Node child) {
//		NodeList children = child.getChildNodes();
//
//		for (int i = 0; i < children.getLength(); i++) {
//			Node cNode = children.item(i);
//
//			// 如果为SQL节点
//			if (NODE_SQL.equalsIgnoreCase(cNode.getNodeName())) {
//				Properties attributes = parseAttributes(cNode);
//
//				// sql statement id
//				String id = attributes.getProperty("id");
//
//				// db name list
//				String value = attributes.getProperty("value");
//
//				dbRouteMap.put(id, value);
//			}
//		}
//	}
//
//	/**
//	 * @param child
//	 * @throws DBRouterException
//	 */
//	private static void parseRoute(Node child) throws DBRouterException {
//		Properties attributes = parseAttributes(child);
//
//		String resource = attributes.getProperty("resource");
//		if (resource != null) {
//			ClassPathResource loader = new ClassPathResource(resource);
//
//			try {
//				Document doc = getDoc(loader.getInputStream());
//
//				parseSqlRoute(doc.getLastChild());
//			} catch (IOException e) {
//				throw new DBRouterException("Parse route config file error.", e);
//			}
//		}
//	}
//
//	/**
//	 * 解析路由配置的XML配置文件
//	 * 
//	 * @param reader
//	 * 
//	 * @return
//	 * 
//	 * @throws DBRouterException
//	 */
//	private static Document getDoc(InputStream reader) throws DBRouterException {
//		try {
//			// Configuration
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//
//			dbf.setNamespaceAware(false);
//			dbf.setValidating(validationEnabled);
//			dbf.setIgnoringComments(true);
//			dbf.setIgnoringElementContentWhitespace(false);
//			dbf.setCoalescing(false);
//			dbf.setExpandEntityReferences(true);
//
//			// OutputStreamWriter errorWriter = new
//			// OutputStreamWriter(System.err);
//
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			Document doc = db.parse(reader);
//			return doc;
//
//		} catch (Exception e) {
//			throw new DBRouterException("XML Parser Error. ");
//		}
//	}
//
//	/**
//	 * 解析参数
//	 * 
//	 * @param n
//	 * 
//	 * @return
//	 */
//	private static Properties parseAttributes(Node n) {
//		Properties attributes = new Properties();
//		NamedNodeMap attributeNodes = n.getAttributes();
//
//		for (int i = 0; i < attributeNodes.getLength(); i++) {
//			Node attribute = attributeNodes.item(i);
//			String value = attribute.getNodeValue();
//
//			attributes.put(attribute.getNodeName(), value);
//		}
//
//		return attributes;
//	}
//}
