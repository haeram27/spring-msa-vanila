package com.example.httpclient.component

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DefaultApplicationRunner : ApplicationRunner {
    private val log = KotlinLogging.logger {}

    override fun run(args: ApplicationArguments) {
        log.info { "## ApplicationRunner" }

        args.optionNames.forEach { name ->
            log.info { "option: name=$name, value=${args.getOptionValues(name)}" }
        }

        args.nonOptionArgs.forEach { arg ->
            log.info { "non option args: $arg" }
        }

        args.sourceArgs.forEach { arg ->
            log.info { "source arg: $arg" }
        }
    }
}
