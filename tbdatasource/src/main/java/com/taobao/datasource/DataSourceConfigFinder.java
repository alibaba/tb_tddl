/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.datasource;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

public class DataSourceConfigFinder extends ConfigFinder<LocalTxDataSourceDO> {

    @Override
    protected File[] findConfigFiles(File currentPath) {
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("-ds.xml");
            }
        };
        File[] files = currentPath.listFiles(filter);

        // try conf/*-ds.xml
        if (ArrayUtils.isEmpty(files)) {
            File configDir = new File(currentPath, "conf");
            if (configDir.isDirectory()) {
                files = configDir.listFiles(filter);
            }
        }

        return files;
    }

    @Override
    protected void parse(Map<String, LocalTxDataSourceDO> result, File file) throws Exception {
        Collection<LocalTxDataSourceDO> dss = DataSourceConfigParser.parse(file);
        for (LocalTxDataSourceDO ds : dss) {
            result.put(ds.getJndiName(), ds);
        }
    }

    @Override
    protected String getTypeName() {
        return "Datasource";
    }

}
