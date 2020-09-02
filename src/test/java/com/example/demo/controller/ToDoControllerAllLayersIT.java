package com.example.demo.controller;

import com.example.demo.repository.ToDoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc

public class ToDoControllerAllLayersIT {
    @Autowired
    private MockMvc mockMvc;

    // we use h2 database, init. in DemoApplication
    @Test
    void whenGetAll_thenReturnValidResponse() throws Exception {
        this.mockMvc
                .perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].text").value("Wash the dishes"))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].completedAt").doesNotExist())
                .andExpect(jsonPath("$[1].completedAt").exists());
    }
    @Test
    void putMappingComplete() throws Exception {
        this.mockMvc
                .perform(put("/todos/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedAt").exists())
                .andDo(print());
    }

    @Test
    void postMappingWithId() throws Exception {
        this.mockMvc
                .perform(post("/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"id\":1, \"text\": \"Wash the dishes 1\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Wash the dishes 1"));
    }

    @Test
    void postMappingNewEntity() throws Exception {
        this.mockMvc
                .perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Wash the dishes 2\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.text").value("Wash the dishes 2"))
                .andDo(print());
    }

}
