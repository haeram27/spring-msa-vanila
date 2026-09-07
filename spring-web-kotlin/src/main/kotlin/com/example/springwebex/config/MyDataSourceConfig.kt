package com.example.springwebex.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import com.example.springwebex.model.bean.MyDataSource
import com.example.springwebex.model.prop.MyDataSourceProperties

@Configuration
@EnableConfigurationProperties(MyDataSourceProperties::class)
class MyDataSourceConfig(private val properties: MyDataSourceProperties) {

    @Bean("myDataSourceWithMapped")
    @Primary
    fun myDataSource(): MyDataSource {
        val etc = properties.etc
        return MyDataSource(
            properties.url,
            properties.username,
            properties.password,
            etc?.maxConnection ?: 0,
            etc?.timeout,
            etc?.options
        )
    }
}
