package com.example.springgrpc.app

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("sample.grpc.target")
data class GrpcTargetProperties(
    val host: String = "localhost",
    val port: Int = 9090,
    val timeoutSeconds: Long = 5,
)
