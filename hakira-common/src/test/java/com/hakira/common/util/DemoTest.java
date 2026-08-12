package com.hakira.common.util;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: hakira-common
 * @BelongsPackage: com.hakira.common.util
 * @Author: hakiraKafka
 * @CreateTime: 2024-05-24  09:30:24
 * @Description: TODO
 * @Version: 1.0
 */
public class DemoTest {
    @Test
    public void test01() {
        String jsonString = "{\"data\":\"2024-01-01\",\"time\":\"10:01:23\",\"phone\":\"123456789123\",\"address\":\"asdf\",\"name\":\"qwer\"}";

        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(jsonString, HashMap.class);

        System.out.println("Simulated put operations:");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println("map.put(\"" + entry.getKey() + "\", \"" + entry.getValue() + "\");");
        }

        // 模拟新增的put操作
        map.put("newKey", "newValue");
        System.out.println("map.put(\"newKey\", \"newValue\");");
    }

    @Test
    public void test02() {
        String jsonString = "[{\"data\":\"2024-01-01\",\"time\":\"10:01:23\",\"phone\":\"123456789123\",\"address\":\"asdf\",\"name\":\"qwer\"}]";
        List<Map<String, String>> mapList = JSONObject.parseObject(jsonString, new TypeReference<List<Map<String, String>>>() {
        }.getType());
        System.out.println(mapList);
        System.out.println("List<Map<String, String>> mapList = JSONObject.parseObject(jsonString, new TypeReference<List<Map<String, String>>>() {}.getType());");
        for (int i = 0; i < mapList.size(); i++) {
            Map<String, String> map = mapList.get(i);
            System.out.println("Map<String, String> map" + i + " = mapList.get(i);");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                System.out.println("map" + i + ".put(\"" + entry.getKey() + "\", \"" + entry.getValue() + "\");");
            }
            System.out.println("mapList.add(map" + i + ");");
        }
    }

        @Test
        public void test03() {
            String json = "{\"A\":[{\"B\":{\"C\":{\"D\":\"1\",\"E\":\"2\",\"F\":\"3\"}}}]}";
            Map<String, Object> map = JSONObject.parseObject(json, new TypeReference<Map<String, Object>>() {
            }.getType());
            System.out.println(map);
        }
}
