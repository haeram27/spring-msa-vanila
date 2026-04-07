package com.example.springwebex.controller

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class GetControllerTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun getGreeting() {
        mockMvc.perform(get("/api/get/greeting"))
            .andDo(print())
            .andExpect(status().isOk())
    }

    @Test
    fun getCounterIncrement() {
        mockMvc.perform(get("/api/get/counter"))
            .andDo(print())
            .andExpect(status().isOk())
    }
}
