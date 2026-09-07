package com.example.empty;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import com.example.EvaluatedTimeTests;


@SpringBootTest
class PropertyTests extends EvaluatedTimeTests {

    @Autowired
    private Environment env;

    @Value("${java.version}")
    String javaVersion;

    @Value("${java.vendor}")
    String javaVendor;

    @Value("${java.vendor.url}")
    String javaVendorUrl;

    @Value("${java.home}")
    String javaVendorHome;

    @Test
    public void printPropertyUsingEnvironmentTest() {
        /**
         * Environment includes follows: 
         *   OS Environement 
         *   Java System Property 
         *   Command line arguments
         *   application.properties(or .yaml)
         */

        // print all Entry of Environments
        Map<String, Object> map = new LinkedHashMap<>();
        MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(propSrcs.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
                .flatMap(Arrays::<String>stream).filter(Objects::nonNull)
                .sorted(String::compareToIgnoreCase)
                .forEach(key -> map.put(key, Optional.ofNullable(env.getProperty(key)).orElse("")));

        for (Entry<String, Object> e : map.entrySet()) {
            System.out.println(e.getKey() + "=" + e.getValue());
        }

        // print a environment property
        System.out.println("user.name=" + env.getRequiredProperty("user.name"));

        // print OS environment variable
        // System.out.println("SHELL=" + System.getenv("SHELL"));

        // print OS java system property
        // System.out.println("java.version=" + System.getProperty("java.version"));
        // System.out.println("java.vendor=" + System.getProperty("java.vendor"));
        // System.out.println("java.vendor.url=" + System.getProperty("java.vendor.url"));
        // System.out.println("java.home=" + System.getProperty("java.home"));
        // System.out.println("java.class.version=" + System.getProperty("java.class.version"));
        // System.out.println("java.class.path=" + System.getProperty("java.class.path"));
        // java.ext.dir ? external directory of classes can be loaded by ClassLoader
        // System.out.println("java.ext.dir=" + System.getProperty("java.ext.dir"));
        // System.out.println("user.name=" + System.getProperty("user.name"));
        // System.out.println("user.home=" + System.getProperty("user.home"));
        // System.out.println("user.dir=" + System.getProperty("user.dir"));
        // System.out.println("os.name=" + System.getProperty("os.name"));
        // System.out.println("os.arch=" + System.getProperty("os.arch"));
        // System.out.println("os.version=" + System.getProperty("os.version"));
        // System.out.println("file.separator=" + System.getProperty("file.separator"));
        // System.out.println("path.separator=" + System.getProperty("path.separator"));
        // System.out.println("line.separator=" + System.getProperty("line.separator"));
    }

    @Test
    public void printPropertyUsingFieldValueTest() {
        System.out.println(javaVersion);
        System.out.println(javaVendor);
        System.out.println(javaVendorUrl);
        System.out.println(javaVendorHome);
    }

    @Test
    public void printPropertyUsingValueTest(@Value("${java.version}") String javaVersion,
            @Value("${java.vendor}") String javaVendor,
            @Value("${java.vendor.url}") String javaVendorUrl,
            @Value("${java.home}") String javaVendorHome) {
        System.out.println(javaVersion);
        System.out.println(javaVendor);
        System.out.println(javaVendorUrl);
        System.out.println(javaVendorHome);
    }
}
