package com.alextc.steamcardapi.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsApiInformation() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Steam Card API"))
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.card").value("/api/steam/card.svg"))
                .andExpect(jsonPath("$.gameCard").value("/api/steam/game/{appId}/card.svg"));
    }
}
