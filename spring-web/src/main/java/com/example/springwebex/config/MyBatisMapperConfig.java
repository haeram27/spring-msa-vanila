package com.example.springwebex.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@MapperScan("com.example.springwebex.dao")
public class MyBatisMapperConfig {
}
