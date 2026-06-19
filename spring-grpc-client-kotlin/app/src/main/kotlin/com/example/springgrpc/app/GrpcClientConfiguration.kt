package com.example.springgrpc.app

import com.example.springgrpc.client.HelloGrpcClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcClientConfiguration {
    @Bean(destroyMethod = "close")
    fun helloGrpcClient(properties: GrpcTargetProperties): HelloGrpcClient =
        HelloGrpcClient.connect(
            host = properties.host,
            port = properties.port,
            timeoutSeconds = properties.timeoutSeconds,
        )
}
