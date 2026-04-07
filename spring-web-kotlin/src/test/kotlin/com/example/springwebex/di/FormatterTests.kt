package com.example.springwebex.di

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.junit.jupiter.api.Assertions.assertNotNull

@SpringBootTest
class FormatterTests {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun testFooFormatter() {
        val formatter = applicationContext.getBean("fooFormatter", Formatter::class.java)
        assertNotNull(formatter)
        val result = formatter.format("test")
        println("FooFormatter result: $result")
    }

    @Test
    fun testBarFormatter() {
        val formatter = applicationContext.getBean("barFormatter", Formatter::class.java)
        assertNotNull(formatter)
        val result = formatter.format("test")
        println("BarFormatter result: $result")
    }

    @Test
    fun testZooFormatter() {
        val formatter = applicationContext.getBean("zooFormatter", Formatter::class.java)
        assertNotNull(formatter)
        val result = formatter.format("test")
        println("ZooFormatter result: $result")
    }
}
