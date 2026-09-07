package com.example.springwebex.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.example.springwebex.EvaluatedTimeTests;

@SpringBootTest
public class DvdRentalControllerTests extends EvaluatedTimeTests {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void actorsTest() throws Exception {
        this.mockMvc.perform(get("/dvdrental/actors")).andDo(print());
    }

    @Test
    public void actorByIdTest() throws Exception {
        this.mockMvc.perform(get("/dvdrental/actor/200")).andDo(print());
    }
}
