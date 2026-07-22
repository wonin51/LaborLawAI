package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.config.AdminAccountRegistry;
import com.laborlaw.ragkbdemo.config.AdminAccessInterceptor;
import com.laborlaw.ragkbdemo.config.AdminAuditInterceptor;
import com.laborlaw.ragkbdemo.config.WebMvcConfig;
import com.laborlaw.ragkbdemo.service.LegalDocumentIndexService;
import com.laborlaw.ragkbdemo.vo.LegalDocumentIndexResultVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LegalDocumentIndexController.class)
@Import({WebMvcConfig.class, AdminAccountRegistry.class})
@TestPropertySource(properties = "app.admin.accounts=editor|local-admin-token|KNOWLEDGE_READ,KNOWLEDGE_WRITE")
class LegalDocumentIndexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LegalDocumentIndexService legalDocumentIndexService;

    @MockBean
    private AdminAccessInterceptor adminAccessInterceptor;

    @MockBean
    private AdminAuditInterceptor adminAuditInterceptor;

    @Test
    @DisplayName("POST /api/legal-documents/{id}/index returns unified ApiResponse")
    void indexReturnsUnifiedApiResponse() throws Exception {
        LegalDocumentIndexResultVO vo = new LegalDocumentIndexResultVO();
        vo.setDocumentId(1L);
        vo.setChunkCount(3);
        vo.setIndexedCount(3);
        vo.setFailedCount(0);
        vo.setStatus("indexed");
        when(legalDocumentIndexService.index(1L)).thenReturn(vo);

        mockMvc.perform(post("/api/legal-documents/{id}/index", 1L)
                        .header("X-Admin-Token", "local-admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.document_id").value(1))
                .andExpect(jsonPath("$.data.chunk_count").value(3))
                .andExpect(jsonPath("$.data.status").value("indexed"));
    }
}
