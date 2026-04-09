package com.example.httpclient

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.CountDownLatch
import org.springframework.boot.Banner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent

@SpringBootApplication
class SpringApplication

private val log = KotlinLogging.logger {}
private val shutdownLatch = CountDownLatch(1)

fun main(args: Array<String>) {
    log.trace { "*** main() ***" }

    SpringApplicationBuilder(SpringApplication::class.java)
        .listeners(
            ApplicationListener<ApplicationEvent> { event ->
                log.info { ":: springboot :: event :: $event" }

                if (event is ContextClosedEvent) {
                    shutdownLatch.countDown()
                }
            }
        )
        .web(WebApplicationType.NONE)
        .bannerMode(Banner.Mode.OFF)
        .run(*args)

    shutdownLatch.await()
}
