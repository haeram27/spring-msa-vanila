package com.example.springgrpc.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SpringGrpcClientApplication

fun main(args: Array<String>) {
    runApplication<SpringGrpcClientApplication>(*args)
}
