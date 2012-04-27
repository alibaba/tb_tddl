/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.datasource;

import java.io.File;
import java.util.Map;

import com.taobao.datasource.resource.security.SecureIdentityLoginModule;

public class LoginConfigFinder extends ConfigFinder<SecureIdentityLoginModule> {

    @Override
    protected void parse(Map<String, SecureIdentityLoginModule> result, File file) throws Exception {
        Map<String, SecureIdentityLoginModule> modules = LoginConfigParser.parse(file);
        result.putAll(modules);
    }

    @Override
    protected File[] findConfigFiles(File currentPath) {
        File file = new File(currentPath, "conf/login-config.xml");
        if (file.isFile()) {
            return new File[] { file };
        } else {
            return new File[0];
        }
    }

    @Override
    protected String getTypeName() {
        return "Security domain";
    }

}
