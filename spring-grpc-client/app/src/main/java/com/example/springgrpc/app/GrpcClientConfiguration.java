package com.example.springgrpc.app;

import com.example.springgrpc.client.HelloGrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfiguration {
    @Bean(destroyMethod = "close")
    public HelloGrpcClient helloGrpcClient(GrpcTargetProperties properties) {
        return HelloGrpcClient.connect(
            properties.getHost(),
            properties.getPort(),
            properties.getTimeoutSeconds()
        );
    }
}
