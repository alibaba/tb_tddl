/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.datasource;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.taobao.datasource.resource.security.SecureIdentityLoginModule;

public class LoginConfigParser {

    private static final String NAME = "name";

    private static final DocumentFactory documentFactory = DocumentFactory.getInstance();

    private static final XPath applicationPolicyPath = documentFactory
            .createXPath("/policy/application-policy[authentication/login-module/@code='org.jboss.resource.security.SecureIdentityLoginModule']");

    private static final XPath usernamePath = documentFactory
            .createXPath("authentication/login-module/module-option[@name='username' or @name='userName']");

    private static final XPath passwordPath = documentFactory
            .createXPath("authentication/login-module/module-option[@name='password']");

    @SuppressWarnings("unchecked")
    public static Map<String, SecureIdentityLoginModule> parse(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        // Prevent from resolving dtd files
        reader.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
            }
        });
        Document document = reader.read(file);
        List<Node> nodes = applicationPolicyPath.selectNodes(document);

        Map<String, SecureIdentityLoginModule> result = new HashMap<String, SecureIdentityLoginModule>();
        for (Node node : nodes) {
            createSecureIdentityLoginModule(result, node);
        }
        return result;
    }

    private static void createSecureIdentityLoginModule(Map<String, SecureIdentityLoginModule> result, Node node) {
        Element applicationPolicy = (Element) node;
        String name = applicationPolicy.attributeValue(NAME);

        Element usernameElement = (Element) usernamePath.selectSingleNode(applicationPolicy);
        String username = usernameElement != null ? usernameElement.getTextTrim() : null;

        Element passwordElement = (Element) passwordPath.selectSingleNode(applicationPolicy);
        String password = passwordElement != null ? passwordElement.getTextTrim() : null;

        if (isNotBlank(name) && isNotBlank(username) && isNotBlank(password)) {
            SecureIdentityLoginModule module = new SecureIdentityLoginModule();
            module.setUserName(username);
            module.setPassword(password);
            result.put(name, module);
        }
    }

}
