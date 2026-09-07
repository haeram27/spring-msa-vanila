package com.example.springwebex

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringExApplication

private val log = KotlinLogging.logger {}

fun main(args: Array<String>) {
    runApplication<SpringExApplication>(*args)
    log.info { "***** main() *****" }
}
