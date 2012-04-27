/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.datasource;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public abstract class ConfigFinder<T> {

    private static final Logger logger = Logger.getLogger(ConfigFinder.class);

    private volatile Map<String, T> configs = null;

    public T get(String name) {
        if (configs == null) {
            synchronized (this) {
                if (configs == null) { // double check
                    configs = find();
                }
            }
        }
        T config = configs.get(name);
        if (config == null) {
            String message = String.format("%s '%s' not found", getTypeName(), name);
            throw new IllegalArgumentException(message);
        }
        return config;
    }

    protected abstract String getTypeName();

    public Map<String, T> find() {
        File classpath = getClasspath();
        Map<String, T> result = new HashMap<String, T>();
        findAlongClasspath(classpath, result);
        return result;
    }

    private void findAlongClasspath(File classpath, Map<String, T> result) {
        for (File currentPath = classpath; currentPath != null; currentPath = currentPath.getParentFile()) {
            File[] files = findConfigFiles(currentPath);
            if (logger.isDebugEnabled()) {
                String message = String.format("Found config files in path '%s': %s", currentPath, Arrays
                        .toString(files));
                logger.debug(message);
            }
            fetchConfigs(result, files);
            if (!result.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Configs are fetched");
                }
                break;
            }
        }
    }

    private void fetchConfigs(Map<String, T> result, File[] files) {
        for (File file : files) {
            try {
                parse(result, file);
            } catch (Exception e) {
                logger.error("Error parsing datasource config file: " + file, e);
            }
        }
    }

    protected abstract void parse(Map<String, T> result, File file) throws Exception;

    protected abstract File[] findConfigFiles(File currentPath);

    private File getClasspath() {
        String classpath = this.getClass().getClassLoader().getResource("").getPath();
        if (logger.isDebugEnabled()) {
            logger.debug("The classpath is " + classpath);
        }
        return new File(classpath);
    }

}
