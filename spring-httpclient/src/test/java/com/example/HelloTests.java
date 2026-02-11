package com.example;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloTests extends EvaluatedTimeTests {

    @Test
    public void hello() {
        System.out.println("hello");
        log.info("hello");
    }
}
