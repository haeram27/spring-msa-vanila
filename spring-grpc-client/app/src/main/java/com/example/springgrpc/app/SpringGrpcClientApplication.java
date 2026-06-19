package com.example.springgrpc.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringGrpcClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringGrpcClientApplication.class, args);
    }
}
