package com.taobao.datasource.resource.security;

import junit.framework.Assert;

import org.junit.Test;

public class SecureIdentityLoginModuleTest {

    @Test
    public void decodePassword() throws Exception {
        String orignal = "sync_log";
        String encoded = SecureIdentityLoginModule.encode(orignal);
        System.out.println(encoded);
        Assert.assertEquals("43bb469cb8bd0c34df8592078de921bc", encoded);
        SecureIdentityLoginModule m = new SecureIdentityLoginModule();
        m.setPassword("46bede21694492800a944ca43198bb47");
        String actual = m.getDecodedPassword();
       System.out.println(actual);
       
       
    }
}
