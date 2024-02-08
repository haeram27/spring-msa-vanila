package com.example.springwebex.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.springwebex.service.AopExamService;

@SpringBootTest
class AopTests {

    @Autowired
    AopExamService service;

    @Test
    public void exampleTest() throws Exception {
        System.out.println("\n*******************************************");
        service.methodNorm();

        System.out.println("\n*******************************************");
        service.methodAspectDuplicated();

        System.out.println("\n*******************************************");
        try {
            service.methodExcept();
        } catch (Exception ex) {
            System.out.println(String.join("::", ex.getClass().getName(), ex.getMessage()));
        }
    }
}
