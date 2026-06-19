package com.example.springgrpc.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SpringGrpcServerApplication

fun main(args: Array<String>) {
    runApplication<SpringGrpcServerApplication>(*args)
}
