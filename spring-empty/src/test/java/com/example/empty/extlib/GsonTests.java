package com.example.empty.extlib;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.EvaluatedTimeTests;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
class GsonTests extends EvaluatedTimeTests {
    @Test
    public void gsonTest1() {
        Map<String, String> dataMaps = new HashMap<String, String>(5);
        dataMaps.put("key1", "value1");
        dataMaps.put("key2", "value2");
        dataMaps.put("key3", "value3");
        dataMaps.put("key4", "value4");
        dataMaps.put("key5", "value5");

        Gson gson = new Gson();
        String serializeString = gson.toJson(dataMaps);
        log.info("serializeString : " + serializeString);

        // Type type = new TypeToken<Map<String, String>>() {}.getType();
        // Map deserializeMap = gson.fromJson(serializeString, type);
        // System.out.println("deserializeMap : " + deserializeMap);
    }

    @Test
    public void gsonTest2() {

        JsonObject jsonobject = new JsonObject();
        jsonobject.addProperty("company", "tistory");
        jsonobject.addProperty("address", "seoul");
        jsonobject.addProperty("number", "0212345678");

        JsonObject name1Info = new JsonObject();
        name1Info.addProperty("name", "kim");
        name1Info.addProperty("age", "29");
        name1Info.addProperty("isNew", true);

        JsonObject name2Info = new JsonObject();
        name2Info.addProperty("name", "park");
        name2Info.addProperty("age", "27");
        name2Info.addProperty("isNew", true);

        JsonObject name3Info = new JsonObject();
        name3Info.addProperty("name", "lee");
        name3Info.addProperty("age", "26");
        name3Info.addProperty("isNew", true);

        JsonArray infoArray = new JsonArray();
        infoArray.add(name1Info);
        infoArray.add(name2Info);
        infoArray.add(name3Info);

        jsonobject.add("newEmployees", infoArray);
        // System.out.println(jsonobject);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(jsonobject);
        System.out.println(json);
    }
}
