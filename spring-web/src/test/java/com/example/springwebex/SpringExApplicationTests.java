package com.example.springwebex;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
public class SpringExApplicationTests extends EvaluatedTimeTests {
    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void printAllBeanNamesTest() {
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : allBeanNames) {
            System.out.println(beanName);
        }
    }
}
