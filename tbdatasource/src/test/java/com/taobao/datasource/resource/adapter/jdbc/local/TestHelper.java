package com.taobao.datasource.resource.adapter.jdbc.local;

import java.util.HashMap;
import java.util.Map;

public class TestHelper {

    public static Map<String, Object> createMap(Object... params) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < params.length; i += 2) {
            String key = (String) params[i];
            Object value = params[i + 1];
            map.put(key, value);
        }
        return map;
    }

}
