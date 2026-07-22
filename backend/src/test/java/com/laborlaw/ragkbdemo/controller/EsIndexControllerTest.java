package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.config.AdminAccessInterceptor;
import com.laborlaw.ragkbdemo.config.AdminAuditInterceptor;
import com.laborlaw.ragkbdemo.exception.EsOperationException;
import com.laborlaw.ragkbdemo.service.EsIndexService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EsIndexController.class)
class EsIndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminAccessInterceptor adminAccessInterceptor;

    @MockBean
    private AdminAuditInterceptor adminAuditInterceptor;

    @MockBean
    private EsIndexService esIndexService;

    @Test
    @DisplayName("POST /api/es/init-index returns unified ApiResponse")
    void initIndexReturnsUnifiedApiResponse() throws Exception {
        when(esIndexService.initIndex("labor_index")).thenReturn("\u7d22\u5f15\u521b\u5efa\u6210\u529f");

        mockMvc.perform(post("/api/es/init-index").param("index_name", "labor_index"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data").value("\u7d22\u5f15\u521b\u5efa\u6210\u529f"));
    }

    @Test
    @DisplayName("POST /api/es/init-index returns clear ApiResponse when Elasticsearch auth fails")
    void initIndexReturnsClearApiResponseWhenElasticsearchAuthFails() throws Exception {
        when(esIndexService.initIndex("labor_index"))
                .thenThrow(new EsOperationException(503,
                        "Elasticsearch\u8ba4\u8bc1\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5RAG_AI_ELASTICSEARCH_USERNAME/RAG_AI_ELASTICSEARCH_PASSWORD"));

        mockMvc.perform(post("/api/es/init-index").param("index_name", "labor_index"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value(503))
                .andExpect(jsonPath("$.message").value(
                        "Elasticsearch\u8ba4\u8bc1\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5RAG_AI_ELASTICSEARCH_USERNAME/RAG_AI_ELASTICSEARCH_PASSWORD"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
