package com.taobao.datasource.resource.adapter.jdbc.local;

import static com.taobao.datasource.resource.adapter.jdbc.local.TestHelper.createMap;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.hsqldb.jdbc.jdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import com.taobao.datasource.resource.connectionmanager.CachedConnectionManager;
import com.taobao.datasource.tm.TxManager;

@SuppressWarnings("unchecked")
public class LocalTxDataSourceHsqldbTest {

    private LocalTxDataSource ltds;

    private jdbcDataSource jds;

    private DataSource dataSource;

    private JdbcTemplate template;

    @Before
    public void setUp() throws Exception {
        setUpDatabase();
        setUpDataSource();
    }

    private void setUpDataSource() throws Exception {
        CachedConnectionManager ccm = new CachedConnectionManager();
        ccm.setTransactionManager(TxManager.getInstance());
        ccm.init();

        ltds = new LocalTxDataSource();
        ltds.setBeanName(randomAlphanumeric(32));

        ltds.setTransactionManager(TxManager.getInstance());
        ltds.setCachedConnectionManager(ccm);

        ltds.setConnectionURL("jdbc:hsqldb:mem:test");
        ltds.setDriverClass("org.hsqldb.jdbcDriver");
        ltds.setUserName("sa");
        ltds.setPassword("");

        ltds.setExceptionSorterClassName("org.jboss.resource.adapter.jdbc.GenericExceptionSorter");
        ltds.setPreparedStatementCacheSize(75);

        ltds.setBlockingTimeoutMillis(30000);
        ltds.setIdleTimeoutMinutes(15);
        ltds.setMaxSize(10);
        ltds.setMinSize(2);

        ltds.init();
        dataSource = ltds.getDatasource();
        template = new JdbcTemplate(dataSource);
    }

    private void setUpDatabase() {
        jds = new jdbcDataSource();
        jds.setDatabase("jdbc:hsqldb:mem:test");
        jds.setUser("sa");
        jds.setPassword("");

        JdbcTemplate template = new JdbcTemplate(jds);
        template.execute("CREATE TABLE test (a INT PRIMARY KEY, b INT, c INT)");
        template.execute("INSERT INTO test (a, b, c) VALUES (1, 2, 3)");
        template.execute("INSERT INTO test (a, b, c) VALUES (2, 3, 4)");
        template.execute("INSERT INTO test (a, b, c) VALUES (3, 4, 5)");
    }

    @After
    public void tearDown() throws Exception {
        JdbcTemplate template = new JdbcTemplate(jds);
        template.execute("DROP TABLE test");

        ltds.destroy();
    }

    @Test
    public void simpleQuery() throws Exception {
        Map<String, Object> actual = template.queryForMap("SELECT * FROM test WHERE a = 1");
        Map<String, Object> expected = createMap("A", 1, "B", 2, "C", 3);
        assertEquals(expected, actual);
    }

    @Test
    public void rollback() throws Exception {
        template.execute(new ConnectionCallback() {

            public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                con.setAutoCommit(false);
                con.createStatement().execute("INSERT INTO test (a, b, c) VALUES (4, 5, 6)");
                con.rollback();
                return null;
            }

        });
        int actual = template.queryForInt("SELECT count(*) FROM test");
        int expected = 3;
        assertEquals(expected, actual);
    }

}
