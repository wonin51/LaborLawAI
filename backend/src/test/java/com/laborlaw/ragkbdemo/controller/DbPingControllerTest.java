package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.service.DbPingService;
import com.laborlaw.ragkbdemo.vo.DbPingVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DbPingController.class)
class DbPingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DbPingService dbPingService;

    @Test
    @DisplayName("GET /api/db/ping returns database timestamp when connection is available")
    void pingReturnsDatabaseTimestamp() throws Exception {
        when(dbPingService.ping()).thenReturn(new DbPingVO("2026-07-18 16:00:00", "ok"));

        mockMvc.perform(get("/api/db/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.timestamp").value("2026-07-18 16:00:00"))
                .andExpect(jsonPath("$.data.status").value("ok"));
    }

    @Test
    @DisplayName("GET /api/db/ping returns clear error message when connection fails")
    void pingReturnsClearErrorWhenDatabaseUnavailable() throws Exception {
        when(dbPingService.ping()).thenThrow(new IllegalStateException("Database ping failed: connection timeout"));

        mockMvc.perform(get("/api/db/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Database ping failed: connection timeout"))
                .andExpect(jsonPath("$.data.status").value("error"));
    }
}
