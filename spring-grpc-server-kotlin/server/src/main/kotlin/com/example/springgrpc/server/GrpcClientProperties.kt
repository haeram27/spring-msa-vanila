package com.example.springgrpc.server

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("sample.grpc.client")
data class GrpcClientProperties(
    val host: String = "localhost",
    val port: Int = 9090,
    val timeoutSeconds: Long = 5,
)
