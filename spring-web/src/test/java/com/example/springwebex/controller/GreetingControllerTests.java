
/*-
 * https://spring.io/guides/gs/rest-service/
 * https://github.com/spring-guides/gs-rest-service.git
 * https://www.baeldung.com/spring-boot-testing
 * https://spring.io/guides/gs/testing-web/
 */
package com.example.springwebex.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.springwebex.EvaluatedTimeTests;

@SpringBootTest
public class GreetingControllerTests extends EvaluatedTimeTests {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void errorTest() throws Exception {
        this.mockMvc.perform(get("/error")).andDo(log());
    }

    @Test
    public void noParamGreetingShouldReturnDefaultMessage() throws Exception {

        this.mockMvc.perform(get("/api/get/greeting")).andDo(print()).andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Hello, World!"));
    }

    @Test
    public void paramGreetingShouldReturnTailoredMessage() throws Exception {

        this.mockMvc.perform(get("/api/get/greeting").param("name", "Spring Community")).andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Hello, Spring Community!"));
    }

}
