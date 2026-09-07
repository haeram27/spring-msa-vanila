package com.example.springwebex

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.junit.jupiter.api.Assertions.assertNotNull

@SpringBootTest
class ApplicationContextTests {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun testApplicationContextInitialization() {
        assertNotNull(applicationContext)
    }

    @Test
    fun testBeansAreLoaded() {
        val beanCount = applicationContext.beanDefinitionCount
        println("Number of beans loaded: $beanCount")
        assertNotNull(beanCount)
    }

    @Test
    fun testGraduateControllerExists() {
        val controller = applicationContext.getBean("getController")
        assertNotNull(controller)
    }
}
