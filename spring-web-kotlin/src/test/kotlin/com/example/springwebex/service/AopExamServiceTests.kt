package com.example.springwebex.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AopExamServiceTests {

    @Autowired
    private lateinit var aopExamService: AopExamService

    @Test
    fun testSuccess() {
        val result = aopExamService.success("test")
        println("Success result: $result")
    }

    @Test
    fun testWithException() {
        try {
            aopExamService.withException("test")
        } catch (e: RuntimeException) {
            println("Caught exception: ${e.message}")
        }
    }

    @Test
    fun testMultipleCalls() {
        repeat(3) {
            val result = aopExamService.success("call $it")
            println("Call $it result: $result")
        }
    }
}
