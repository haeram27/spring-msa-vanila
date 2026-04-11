package com.example.springwebex.runner

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import io.github.oshai.kotlinlogging.KotlinLogging
@Component
@Order(2)
class DefaultApplicationRunner : ApplicationRunner {
    private val log = KotlinLogging.logger {}
    override fun run(args: ApplicationArguments?) {
        log.info { "DefaultApplicationRunner: Application started" }
        
        if (args != null) {
            log.info { "Option Names: ${args.optionNames}" }
            log.info { "Non Option Args: ${args.nonOptionArgs}" }

            for (optionName in args.optionNames) {
                log.info { "Option - $optionName: ${args.getOptionValues(optionName)}" }
            }
        }
    }
}
