package com.example.springwebex.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.springwebex.model.bean.MyDataSource;

@SpringBootTest
public class ConfigurationPropertyTests {
    @Autowired
    private MyDataSource myDataSource;

    @Test
    public void printMyDataSource() {
        System.out.println(myDataSource.getUrl());
        System.out.println(myDataSource.getUsername());
        System.out.println(myDataSource.getPassword());
        System.out.println(myDataSource.getMaxConnection());
        System.out.println(myDataSource.getTimeout());
        System.out.println(myDataSource.getOptions());
    }
}
