package com.example.springwebex.runner

import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import io.github.oshai.kotlinlogging.KotlinLogging

@Component
@Order(1)
class DefaultCommandLineRunner : CommandLineRunner {
    private val log = KotlinLogging.logger {}
    override fun run(vararg args: String) {
        log.info { "DefaultCommandLineRunner: Application started" }
        
        if (args.isNotEmpty()) {
            log.info { "Command Line Arguments Count: ${args.size}" }
            for ((index, arg) in args.withIndex()) {
                log.info { "Arg[$index]: $arg" }
            }
        }
    }
}
