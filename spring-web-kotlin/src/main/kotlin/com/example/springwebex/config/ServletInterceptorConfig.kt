package com.example.springwebex.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.handler.MappedInterceptor
import com.example.springwebex.interceptor.TestServletInterceptor

@Configuration
class ServletInterceptorConfig {

    @Bean
    fun testServletInterceptor(): MappedInterceptor {
        return MappedInterceptor(null, TestServletInterceptor())
    }
}
