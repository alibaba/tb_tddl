package com.taobao.datasource;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.taobao.datasource.resource.security.SecureIdentityLoginModule;

public class LoginConfigFinderTest {

    private LoginConfigFinder finder;

    @Before
    public void setUp() throws Exception {
        finder = new LoginConfigFinder();
    }

    @Test
    public void testGet() {
        SecureIdentityLoginModule expected = new SecureIdentityLoginModule();
        expected.setUserName("taobao");
        expected.setPassword("131ed685e7ff86d4");

        SecureIdentityLoginModule actual = finder.get("EncryptDBCPassword");
        assertEquals(expected, actual);
    }

    @Test
    public void testFind() throws Exception {
        SecureIdentityLoginModule module1 = new SecureIdentityLoginModule();
        module1.setUserName("taobao");
        module1.setPassword("46bede21694492800a944ca43198bb47");
        SecureIdentityLoginModule module2 = new SecureIdentityLoginModule();
        module2.setUserName("taobao");
        module2.setPassword("46bede21694492800a944ca43198bb47");
        SecureIdentityLoginModule module3 = new SecureIdentityLoginModule();
        module3.setUserName("taobao");
        module3.setPassword("131ed685e7ff86d4");
        SecureIdentityLoginModule module4 = new SecureIdentityLoginModule();
        module4.setUserName("datac");
        module4.setPassword("-5434009b757fc478");

        Map<String, SecureIdentityLoginModule> expected = new HashMap<String, SecureIdentityLoginModule>();
        expected.put("EncryptDB1Password", module1);
        expected.put("EncryptDB2Password", module2);
        expected.put("EncryptDBCPassword", module3);
        expected.put("EncryptDBHPassword", module4);

        Map<String, SecureIdentityLoginModule> actual = finder.find();

        assertEquals(expected, actual);
    }

}
