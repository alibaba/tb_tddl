package com.taobao.datasource;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class DataSourceConfigFinderTest {

    private DataSourceConfigFinder finder;

    @Before
    public void setUp() {
        finder = new DataSourceConfigFinder();
    }

    @Test
    public void testFind() {
        Set<String> expected = new HashSet<String>();
        expected.add("DefaultDS");
        expected.add("DBCDataSource");
        expected.add("DBCDataSource_cm2");
        expected.add("DBCDataSource_cm3");

        Map<String, LocalTxDataSourceDO> map = finder.find();
        Set<String> actual = map.keySet();

        assertEquals(expected, actual);

        for (LocalTxDataSourceDO ds : map.values()) {
            assertNotNull(ds);
        }
    }

    @Test
    public void testGet() throws Exception {
        LocalTxDataSourceDO actual = finder.get("DBCDataSource");
        assertEquals("DBCDataSource", actual.getJndiName());
    }

}
