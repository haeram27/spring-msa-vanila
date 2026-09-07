package com.example.springwebex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.MappedInterceptor;
import com.example.springwebex.interceptor.TestServletInterceptor;

@Configuration
public class ServletInterceptorConfig {

    @Bean
    public MappedInterceptor testServletInterceptor() {
        return new MappedInterceptor(null, new TestServletInterceptor());
    }
}
