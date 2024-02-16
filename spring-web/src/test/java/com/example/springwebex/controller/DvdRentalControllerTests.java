package com.example.springwebex.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import com.example.springwebex.EvaluatedTimeTests;

@SpringBootTest
@AutoConfigureMockMvc
public class DvdRentalControllerTests extends EvaluatedTimeTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void actorsTest() throws Exception {
        this.mockMvc.perform(get("/dvdrental/actors")).andDo(print());
    }

    @Test
    public void actorByIdTest() throws Exception {
        this.mockMvc.perform(get("/dvdrental/actor/200")).andDo(print());
    }
}
