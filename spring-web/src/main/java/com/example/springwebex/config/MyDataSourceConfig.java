package com.example.springwebex.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.example.springwebex.model.bean.MyDataSource;
import com.example.springwebex.model.prop.MyDataSourceProperties;

@Configuration
@Profile("!test")
@EnableConfigurationProperties(MyDataSourceProperties.class)
public class MyDataSourceConfig {
    private final MyDataSourceProperties properties;

    public MyDataSourceConfig(MyDataSourceProperties properties) {
        this.properties = properties;
    }

    @Bean("myDataSourceWithMapped")
    @Primary
    public MyDataSource myDataSource() {
        return new MyDataSource(properties.getUrl(), properties.getUsername(),
                properties.getPassword(), properties.getEtc().getMaxConnection(),
                properties.getEtc().getTimeout(), properties.getEtc().getOptions());
    }
}
