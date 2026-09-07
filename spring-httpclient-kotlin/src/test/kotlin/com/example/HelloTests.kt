package com.example

import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.Test

class HelloTests : EvaluatedTimeTests() {
    private val log = KotlinLogging.logger {}

    @Test
    fun hello() {
        println("hello")
        log.info { "hello" }
    }
}
