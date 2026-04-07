package com.example.springwebex.config

import java.time.Duration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import com.example.springwebex.model.bean.MyDataSource

@Configuration
class MyDataSourceEnvConfig(private val env: Environment) {

    @Bean("myDataSourceWithEnv")
    fun myDataSource(): MyDataSource {
        val url = env.getProperty("my.datasource.url") ?: ""
        val username = env.getProperty("my.datasource.username") ?: ""
        val password = env.getProperty("my.datasource.password") ?: ""
        val maxConnection = env.getProperty("my.datasource.etc.max-connection", Int::class.java) ?: 0
        val timeout: Duration? = env.getProperty("my.datasource.etc.timeout", Duration::class.java)
        val options: List<String>? = env.getProperty("my.datasource.etc.options") as? List<String>

        return MyDataSource(url, username, password, maxConnection, timeout, options)
    }
}
