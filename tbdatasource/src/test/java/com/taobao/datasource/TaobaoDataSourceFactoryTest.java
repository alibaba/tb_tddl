package com.taobao.datasource;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.taobao.datasource.resource.adapter.jdbc.local.LocalTxDataSource;

public class TaobaoDataSourceFactoryTest {

    @Test
    public void testCreateLocalTxDataSource() throws Exception {
        LocalTxDataSourceDO dataSourceDO = new LocalTxDataSourceDO();
        dataSourceDO.setJndiName("testDataSource");
        dataSourceDO.setConnectionURL("jdbc:mysql://localhost:3306/test");
        dataSourceDO.addConnectionProperty("SetBigStringTryClob", "true");
        dataSourceDO.addConnectionProperty("defaultRowPrefetch", "50");
        dataSourceDO.setDriverClass("com.mysql.jdbc.Driver");
        dataSourceDO.setMinPoolSize(2);
        dataSourceDO.setMaxPoolSize(4);
        dataSourceDO.setPreparedStatementCacheSize(75);
        dataSourceDO.setUserName("root");
        dataSourceDO.setPassword("123@abc");
        LocalTxDataSource createLocalTxDataSource = TaobaoDataSourceFactory
                .createLocalTxDataSource(dataSourceDO);
        assertEquals("test", createLocalTxDataSource.getDatasource().getConnection().getCatalog());
    }

}
