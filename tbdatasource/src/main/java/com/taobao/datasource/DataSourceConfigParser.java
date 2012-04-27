/*(C) 2007-2012 Alibaba Group Holding Limited.	 *This program is free software; you can redistribute it and/or modify	*it under the terms of the GNU General Public License version 2 as	* published by the Free Software Foundation.	* Authors:	*   junyu <junyu@taobao.com> , shenxun <shenxun@taobao.com>,	*   linxuan <linxuan@taobao.com> ,qihao <qihao@taobao.com> 	*/	package com.taobao.datasource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

@SuppressWarnings("unchecked")
public class DataSourceConfigParser {

    private static final String BACKGROUND_VALIDATION = "background-validation";

    private static final String BACKGROUND_VALIDATION_MINUTES = "background-validation-minutes";

    private static final String BLOCKING_TIMEOUT_MILLIS = "blocking-timeout-millis";

    private static final String CHECK_VALID_CONNECTION_SQL = "check-valid-connection-sql";

    private static final String CONNECTION_PROPERTY = "connection-property";

    private static final String CONNECTION_URL = "connection-url";

    private static final int DEFAULT_BACKGROUND_VALIDATION_MINUTES = 10;

    private static final int DEFAULT_BLOCKING_TIMEOUT_MILLIS = 30000;

    private static final int DEFAULT_IDLE_TIMEOUT_MINUTES = 15;

    private static final int DEFAULT_MAX_POOL_SIZE = 20;

    private static final int DEFAULT_MIN_POOL_SIZE = 0;

    private static final int DEFAULT_PREPARED_STATEMENT_CACHE_SIZE = 0;

    private static final int DEFAULT_QUERY_TIMEOUT = 0;

    private static final String DEFAULT_TRACK_STATEMENTS = "nowarn";

    private static final String DEFAULT_TRANSACTION_ISOLATION = "-1";

    private static final String DRIVER_CLASS = "driver-class";

    private static final String EXCEPTION_SORTER_CLASS_NAME = "exception-sorter-class-name";

    private static final String FALSE = "false";

    private static final String IDLE_TIMEOUT_MINUTES = "idle-timeout-minutes";

    private static final String JNDI_NAME = "jndi-name";

    private static final String LOCAL_TX_DATASOURCE = "local-tx-datasource";

    private static final String MAX_POOL_SIZE = "max-pool-size";

    private static final String MIN_POOL_SIZE = "min-pool-size";

    private static final String NAME = "name";

    private static final String NEW_CONNECTION_SQL = "new-connection-sql";

    private static final String PASSWORD = "password";

    private static final String PREFILL = "prefill";

    private static final String PREPARED_STATEMENT_CACHE_SIZE = "prepared-statement-cache-size";

    private static final String QUERY_TIMEOUT = "query-timeout";

    private static final String SECURITY_DOMAIN = "security-domain";

    private static final String SET_TX_QUERY_TIMEOUT = "set-tx-query-timeout";

    private static final String SHARE_PREPARED_STATEMENTS = "share-prepared-statements";

    private static final String TRACK_STATEMENTS = "track-statements";

    private static final String TRANSACTION_ISOLATION = "transaction-isolation";

    private static final String TRUE = "true";

    private static final String USE_FAST_FAIL = "use-fast-fail";

    private static final String USER_NAME = "user-name";

    private static final String VALID_CONNECTION_CHECKER_CLASS_NAME = "valid-connection-checker-class-name";

    private static final String VALIDATE_ON_MATCH = "validate-on-match";

    public static Collection<LocalTxDataSourceDO> parse(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(file);
        Element datasources = document.getRootElement();
        List<Element> localTxDatasources = (List<Element>) datasources.elements(LOCAL_TX_DATASOURCE);

        Collection<LocalTxDataSourceDO> result = new ArrayList<LocalTxDataSourceDO>(localTxDatasources.size());
        for (Element localTxDatasource : localTxDatasources) {
            LocalTxDataSourceDO ds = createLocalTxDataSourceDO(localTxDatasource);
            result.add(ds);
        }

        return result;
    }

    private static LocalTxDataSourceDO createLocalTxDataSourceDO(Element localTxDatasource) {
        LocalTxDataSourceDO ds = new LocalTxDataSourceDO();
        ds.setJndiName(localTxDatasource.elementTextTrim(JNDI_NAME));
        ds.setConnectionURL(localTxDatasource.elementTextTrim(CONNECTION_URL));
        ds.setDriverClass(localTxDatasource.elementTextTrim(DRIVER_CLASS));
        String transactionIsolation = localTxDatasource.elementTextTrim(TRANSACTION_ISOLATION);
        ds.setTransactionIsolation(transactionIsolation != null ? transactionIsolation : DEFAULT_TRANSACTION_ISOLATION);
        ds.setUserName(localTxDatasource.elementTextTrim(USER_NAME));
        ds.setPassword(localTxDatasource.elementTextTrim(PASSWORD));
        ds.setSecurityDomain(localTxDatasource.elementTextTrim(SECURITY_DOMAIN));
        ds.setMinPoolSize(NumberUtils.toInt(localTxDatasource.elementTextTrim(MIN_POOL_SIZE), DEFAULT_MIN_POOL_SIZE));
        ds.setMaxPoolSize(NumberUtils.toInt(localTxDatasource.elementTextTrim(MAX_POOL_SIZE), DEFAULT_MAX_POOL_SIZE));
        ds.setBlockingTimeoutMillis(NumberUtils.toInt(localTxDatasource.elementTextTrim(BLOCKING_TIMEOUT_MILLIS),
                DEFAULT_BLOCKING_TIMEOUT_MILLIS));
        ds.setBackgroundValidation(TRUE.equalsIgnoreCase(localTxDatasource.elementTextTrim(BACKGROUND_VALIDATION)));
        ds.setBackgroundValidationMinutes(NumberUtils.toLong(localTxDatasource
                .elementTextTrim(BACKGROUND_VALIDATION_MINUTES), DEFAULT_BACKGROUND_VALIDATION_MINUTES));
        ds.setIdleTimeoutMinutes(NumberUtils.toLong(localTxDatasource.elementTextTrim(IDLE_TIMEOUT_MINUTES),
                DEFAULT_IDLE_TIMEOUT_MINUTES));
        ds.setValidateOnMatch(!FALSE.equalsIgnoreCase(localTxDatasource.elementTextTrim(VALIDATE_ON_MATCH)));
        ds.setNewConnectionSQL(localTxDatasource.elementTextTrim(NEW_CONNECTION_SQL));
        ds.setCheckValidConnectionSQL(localTxDatasource.elementTextTrim(CHECK_VALID_CONNECTION_SQL));
        ds.setValidConnectionCheckerClassName(localTxDatasource.elementTextTrim(VALID_CONNECTION_CHECKER_CLASS_NAME));
        ds.setExceptionSorterClassName(localTxDatasource.elementTextTrim(EXCEPTION_SORTER_CLASS_NAME));
        String trackStatements = localTxDatasource.elementTextTrim(TRACK_STATEMENTS);
        ds.setTrackStatements(trackStatements != null ? trackStatements : DEFAULT_TRACK_STATEMENTS);
        ds.setPrefill(TRUE.equalsIgnoreCase(localTxDatasource.elementTextTrim(PREFILL)));
        ds.setUseFastFail(TRUE.equalsIgnoreCase(localTxDatasource.elementTextTrim(USE_FAST_FAIL)));
        ds.setPreparedStatementCacheSize(NumberUtils.toInt(localTxDatasource
                .elementTextTrim(PREPARED_STATEMENT_CACHE_SIZE), DEFAULT_PREPARED_STATEMENT_CACHE_SIZE));
        ds.setSharePreparedStatements(localTxDatasource.element(SHARE_PREPARED_STATEMENTS) != null);
        ds.setTxQueryTimeout(localTxDatasource.element(SET_TX_QUERY_TIMEOUT) != null);
        ds.setQueryTimeout(NumberUtils.toInt(localTxDatasource.elementTextTrim(QUERY_TIMEOUT), DEFAULT_QUERY_TIMEOUT));

        for (Element connectionProperty : (List<Element>) localTxDatasource.elements(CONNECTION_PROPERTY)) {
            ds.addConnectionProperty(connectionProperty.attributeValue(NAME), connectionProperty.getTextTrim());
        }

        return ds;
    }

}
