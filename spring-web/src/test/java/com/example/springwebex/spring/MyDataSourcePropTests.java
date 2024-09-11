package com.example.springwebex.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.springwebex.model.bean.MyDataSource;

@SpringBootTest
class MyDataSourcePropTests {

    @Autowired
    private MyDataSource datasource;

    @Test
    public void printMyDataSourceTest() {
        System.out.println("ds.url: " + datasource.getUrl());
        System.out.println("ds.username: " + datasource.getUsername());
        System.out.println("ds.password: " + datasource.getPassword());
        System.out.println("ds.maxconn: " + datasource.getMaxConnection());
        System.out.println("ds.timeout: " + datasource.getTimeout());
        System.out.println("ds.options: " + datasource.getOptions());
    }
}
