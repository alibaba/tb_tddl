package com.taobao.datasource;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class DataSourceConfigParserTest {

    @Test
    public void testParse() throws Exception {
        LocalTxDataSourceDO ds1 = new LocalTxDataSourceDO();
        ds1.setJndiName("DBCDataSource_cm2");
        ds1.setConnectionURL("jdbc:oracle:oci:@dev-dbcommon");
        ds1.addConnectionProperty("SetBigStringTryClob", "true");
        ds1.addConnectionProperty("defaultRowPrefetch", "50");
        ds1.setDriverClass("oracle.jdbc.driver.OracleDriver");
        ds1.setMinPoolSize(2);
        ds1.setMaxPoolSize(10);
        ds1.setPreparedStatementCacheSize(100);
        ds1.setUserName("taobao");
        ds1.setPassword("taobao");
        ds1.setTransactionIsolation("-1");
        ds1.setBlockingTimeoutMillis(30000);
        ds1.setIdleTimeoutMinutes(15);
        ds1.setValidateOnMatch(true);
        ds1.setBackgroundValidationMinutes(10);
        ds1.setTrackStatements("nowarn");

        LocalTxDataSourceDO ds2 = new LocalTxDataSourceDO();
        ds2.setJndiName("DBCDataSource_cm3");
        ds2.setConnectionURL("jdbc:oracle:oci:@test");
        ds2.setDriverClass("oracle.jdbc.driver.OracleDriver");
        ds2.setMinPoolSize(2);
        ds2.setMaxPoolSize(10);
        ds2.setPreparedStatementCacheSize(100);
        ds2.setSecurityDomain("EncryptDBCPassword");
        ds2.setTransactionIsolation("-1");
        ds2.setBlockingTimeoutMillis(30000);
        ds2.setIdleTimeoutMinutes(15);
        ds2.setValidateOnMatch(true);
        ds2.setBackgroundValidationMinutes(10);
        ds2.setTrackStatements("nowarn");

        Set<LocalTxDataSourceDO> expected = new HashSet<LocalTxDataSourceDO>();
        expected.add(ds1);
        expected.add(ds2);

        File file = new File(getClass().getClassLoader().getResource("com/taobao/datasource/test-ds.xml").getPath());
        Set<LocalTxDataSourceDO> actual = new HashSet<LocalTxDataSourceDO>(DataSourceConfigParser.parse(file));

        assertEquals(expected, actual);
    }

}
