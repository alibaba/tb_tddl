package com.taobao.datasource;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.taobao.datasource.resource.security.SecureIdentityLoginModule;

public class LoginConfigParserTest {

    @Test
    public void testParse() throws Exception {
        SecureIdentityLoginModule module1 = new SecureIdentityLoginModule();
        module1.setUserName("taobao");
        module1.setPassword("46bede21694492800a944ca43198bb47");

        SecureIdentityLoginModule module2 = new SecureIdentityLoginModule();
        module2.setUserName("taobao");
        module2.setPassword("131ed685e7ff86d4");

        Map<String, SecureIdentityLoginModule> expected = new HashMap<String, SecureIdentityLoginModule>();
        expected.put("EncryptDB1Password", module1);
        expected.put("EncryptDBCPassword", module2);

        File file = new File(getClass().getClassLoader().getResource("com/taobao/datasource/test-login-config.xml")
                .getPath());
        Map<String, SecureIdentityLoginModule> actual = LoginConfigParser.parse(file);
        assertEquals(expected, actual);
    }

}
