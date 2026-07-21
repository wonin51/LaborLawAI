package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.config.AdminAccessInterceptor;
import com.laborlaw.ragkbdemo.config.AdminAuditInterceptor;
import com.laborlaw.ragkbdemo.service.AiPingService;
import com.laborlaw.ragkbdemo.vo.AiPingItemVO;
import com.laborlaw.ragkbdemo.vo.AiPingVO;
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

@WebMvcTest(AiPingController.class)
class AiPingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminAccessInterceptor adminAccessInterceptor;

    @MockBean
    private AdminAuditInterceptor adminAuditInterceptor;

    @MockBean
    private AiPingService aiPingService;

    @Test
    @DisplayName("GET /api/ai/ping returns connectivity results for elasticsearch, embedding, and chat")
    void pingReturnsThreeConnectivityResults() throws Exception {
        AiPingVO result = new AiPingVO(
                new AiPingItemVO(true, "ok", "Elasticsearch is reachable"),
                new AiPingItemVO(true, "ok", "Embedding model is reachable"),
                new AiPingItemVO(false, "error", "Chat request failed: connection timeout")
        );
        when(aiPingService.ping()).thenReturn(result);

        mockMvc.perform(get("/api/ai/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.elasticsearch.success").value(true))
                .andExpect(jsonPath("$.data.elasticsearch.status").value("ok"))
                .andExpect(jsonPath("$.data.embedding.success").value(true))
                .andExpect(jsonPath("$.data.chat.success").value(false))
                .andExpect(jsonPath("$.data.chat.message").value("Chat request failed: connection timeout"));
    }
}
