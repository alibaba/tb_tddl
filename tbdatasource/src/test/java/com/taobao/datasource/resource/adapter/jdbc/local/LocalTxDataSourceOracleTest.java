package com.taobao.datasource.resource.adapter.jdbc.local;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.assertEquals;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.taobao.datasource.resource.connectionmanager.CachedConnectionManager;
import com.taobao.datasource.resource.security.SecureIdentityLoginModule;
import com.taobao.datasource.tm.TxManager;

public class LocalTxDataSourceOracleTest {

    private LocalTxDataSource ltds;

    private DataSource dataSource;

    private JdbcTemplate template;

    @Before
    public void setUp() throws Exception {
        SecureIdentityLoginModule securityDomain = new SecureIdentityLoginModule();
        securityDomain.setUserName("water");
        securityDomain.setPassword(SecureIdentityLoginModule.encode("water"));

        CachedConnectionManager ccm = new CachedConnectionManager();
        ccm.setTransactionManager(TxManager.getInstance());
        ccm.init();

        ltds = new LocalTxDataSource();
        ltds.setBeanName(randomAlphanumeric(32));

        ltds.setTransactionManager(TxManager.getInstance());
        ltds.setCachedConnectionManager(ccm);

        ltds.setConnectionURL("jdbc:oracle:thin:@db.dev.crm.taobao.net:1521:newcrm");
        ltds.setDriverClass("oracle.jdbc.driver.OracleDriver");
        ltds.setSecurityDomain(securityDomain);
        // ltds.setUserName("water");
        // ltds.setPassword("water");

        ltds.setExceptionSorterClassName("org.jboss.resource.adapter.jdbc.vendor.OracleExceptionSorter");
        ltds.setPreparedStatementCacheSize(75);
        ltds.setValidateOnMatch(true);
        ltds.setConnectionPropertiesString("SetBigStringTryClob=true\ndefaultRowPrefetch=50");

        ltds.setMinSize(2);
        ltds.setMaxSize(10);
        ltds.setBlockingTimeoutMillis(30000);
        ltds.setIdleTimeoutMinutes(15);
        ltds.setBackgroundValidation(false);
        ltds.setPrefill(false);
        ltds.setUseFastFail(false);
        ltds.setCriteria("ByNothing");

        ltds.init();
        dataSource = ltds.getDatasource();
        template = new JdbcTemplate(dataSource);
    }

    @After
    public void tearDown() throws Exception {
        ltds.destroy();
    }

    @Test
    public void simpleQuery() throws Exception {
        String actual = (String) template.queryForObject("SELECT sql_id FROM sql_info WHERE id = 1", String.class);
        String expected = "simple";
        assertEquals(expected, actual);
    }

}
