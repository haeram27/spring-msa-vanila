package com.example.springwebex.config

import java.time.Duration
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import com.example.springwebex.model.bean.MyDataSource

@Configuration
class MyDataSourceValueConfig {

    @Value("\${my.datasource.url}")
    private lateinit var url: String

    @Value("\${my.datasource.username}")
    private lateinit var username: String

    @Value("\${my.datasource.password}")
    private lateinit var password: String

    @Value("\${my.datasource.etc.max-connection}")
    private var maxConnection: Int = 0

    @Value("\${my.datasource.etc.timeout}")
    private var timeout: Duration? = null

    @Value("\${my.datasource.etc.options}")
    private var options: List<String>? = null

    @Bean("myDataSourceWithValue1")
    fun myDataSource1(): MyDataSource {
        return MyDataSource(url, username, password, maxConnection, timeout, options)
    }

    @Bean("myDataSourceWithValue2")
    fun myDataSource2(
        @Value("\${my.datasource.url}") url: String,
        @Value("\${my.datasource.username}") username: String,
        @Value("\${my.datasource.password}") password: String,
        @Value("\${my.datasource.etc.max-connection}") maxConnection: Int,
        @Value("\${my.datasource.etc.timeout}") timeout: Duration?,
        @Value("\${my.datasource.etc.options}") options: List<String>?
    ): MyDataSource {
        return MyDataSource(url, username, password, maxConnection, timeout, options)
    }
}
