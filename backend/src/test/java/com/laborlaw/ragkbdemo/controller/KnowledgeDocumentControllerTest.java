package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.config.AdminAccountRegistry;
import com.laborlaw.ragkbdemo.config.WebMvcConfig;
import com.laborlaw.ragkbdemo.dto.KnowledgeDocumentCreateDTO;
import com.laborlaw.ragkbdemo.dto.KnowledgeDocumentPageQueryDTO;
import com.laborlaw.ragkbdemo.dto.KnowledgeDocumentStatusUpdateDTO;
import com.laborlaw.ragkbdemo.dto.KnowledgeDocumentUpdateDTO;
import com.laborlaw.ragkbdemo.mapper.KnowledgeDocumentMapper;
import com.laborlaw.ragkbdemo.service.KnowledgeDocumentService;
import com.laborlaw.ragkbdemo.vo.KnowledgeDocumentVO;
import com.laborlaw.ragkbdemo.vo.PageVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KnowledgeDocumentController.class)
@Import({WebMvcConfig.class, AdminAccountRegistry.class})
@TestPropertySource(properties = "app.admin.accounts=editor|local-admin-token|KNOWLEDGE_READ,KNOWLEDGE_WRITE;reader|read-only-token|KNOWLEDGE_READ")
class KnowledgeDocumentControllerTest {

    private static final String ADMIN_TOKEN = "local-admin-token";
    private static final String READ_ONLY_TOKEN = "read-only-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KnowledgeDocumentService knowledgeDocumentService;

    @MockBean
    private KnowledgeDocumentMapper knowledgeDocumentMapper;

    @Test
    @DisplayName("admin document endpoint rejects write operation for read-only role")
    void rejectsWriteOperationForReadOnlyRole() throws Exception {
        mockMvc.perform(post("/api/admin/knowledge/documents")
                        .header("X-Admin-Token", READ_ONLY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "只读用户不应创建文档",
                                  "document_type": "LAW"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Admin permission denied"));
    }
    @Test
    @DisplayName("admin document endpoint rejects missing token")
    void rejectsMissingAdminToken() throws Exception {
        mockMvc.perform(get("/api/admin/knowledge/documents"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("admin document endpoint rejects invalid token")
    void rejectsInvalidAdminToken() throws Exception {
        mockMvc.perform(get("/api/admin/knowledge/documents")
                        .header("X-Admin-Token", "wrong-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
    @Test
    @DisplayName("POST /api/admin/knowledge/documents returns unified error for malformed JSON")
    void createDocumentReturnsUnifiedErrorForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/admin/knowledge/documents")
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("请求体格式不正确"));
    }

    @Test
    @DisplayName("GET /api/admin/knowledge/documents/{id} hides unexpected backend errors")
    void getDocumentHidesUnexpectedBackendErrors() throws Exception {
        when(knowledgeDocumentService.getById(1L)).thenThrow(new IllegalStateException("database password leaked"));

        mockMvc.perform(get("/api/admin/knowledge/documents/{id}", 1L)
                        .header("X-Admin-Token", ADMIN_TOKEN))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("服务内部错误"));
    }
    @Test
    @DisplayName("POST /api/admin/knowledge/documents creates document metadata")
    void createDocumentReturnsCreatedDocument() throws Exception {
        when(knowledgeDocumentService.create(any(KnowledgeDocumentCreateDTO.class))).thenReturn(sampleDocument());

        mockMvc.perform(post("/api/admin/knowledge/documents")
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "中华人民共和国劳动合同法",
                                  "document_type": "LAW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("中华人民共和国劳动合同法"))
                .andExpect(jsonPath("$.data.document_type").value("LAW"));
    }

    @Test
    @DisplayName("POST /api/admin/knowledge/documents returns validation error when title is missing")
    void createDocumentReturnsValidationErrorWhenTitleMissing() throws Exception {
        mockMvc.perform(post("/api/admin/knowledge/documents")
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "document_type": "LAW"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("title is required"));
    }

    @Test
    @DisplayName("GET /api/admin/knowledge/documents returns paged documents")
    void pageDocumentsReturnsPageData() throws Exception {
        when(knowledgeDocumentService.page(any(KnowledgeDocumentPageQueryDTO.class)))
                .thenReturn(new PageVO<>(List.of(sampleDocument()), 1L, 1L, 10L, 1L));

        mockMvc.perform(get("/api/admin/knowledge/documents")
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .param("title", "劳动合同")
                        .param("document_type", "LAW")
                        .param("pageNo", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("中华人民共和国劳动合同法"));
    }

    @Test
    @DisplayName("GET /api/admin/knowledge/documents/{id} returns document detail")
    void getDocumentReturnsDetail() throws Exception {
        when(knowledgeDocumentService.getById(1L)).thenReturn(sampleDocument());

        mockMvc.perform(get("/api/admin/knowledge/documents/{id}", 1L)
                        .header("X-Admin-Token", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.public_id").value("ABCDEF1234567890ABCDEF1234"));
    }

    @Test
    @DisplayName("GET /api/admin/knowledge/documents/{id} returns code 404 when document is missing")
    void getDocumentReturnsNotFoundWhenMissing() throws Exception {
        when(knowledgeDocumentService.getById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/admin/knowledge/documents/{id}", 99L)
                        .header("X-Admin-Token", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Knowledge document not found"));
    }

    @Test
    @DisplayName("PUT /api/admin/knowledge/documents/{id} updates document metadata")
    void updateDocumentReturnsUpdatedDocument() throws Exception {
        when(knowledgeDocumentService.update(eq(1L), any(KnowledgeDocumentUpdateDTO.class))).thenReturn(sampleDocument());

        mockMvc.perform(put("/api/admin/knowledge/documents/{id}", 1L)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "中华人民共和国劳动合同法",
                                  "document_type": "LAW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    @DisplayName("PUT /api/admin/knowledge/documents/{id} returns validation error when title is missing")
    void updateDocumentReturnsValidationErrorWhenTitleMissing() throws Exception {
        mockMvc.perform(put("/api/admin/knowledge/documents/{id}", 1L)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "document_type": "LAW"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("title is required"));
    }

    @Test
    @DisplayName("PUT /api/admin/knowledge/documents/{id} returns code 404 when document is missing")
    void updateDocumentReturnsNotFoundWhenMissing() throws Exception {
        when(knowledgeDocumentService.update(eq(99L), any(KnowledgeDocumentUpdateDTO.class))).thenReturn(null);

        mockMvc.perform(put("/api/admin/knowledge/documents/{id}", 99L)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "不存在的文档",
                                  "document_type": "LAW"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Knowledge document not found"));
    }

    @Test
    @DisplayName("DELETE /api/admin/knowledge/documents/{id} deletes document metadata")
    void deleteDocumentReturnsTrue() throws Exception {
        when(knowledgeDocumentService.deleteById(1L)).thenReturn(Boolean.TRUE);

        mockMvc.perform(delete("/api/admin/knowledge/documents/{id}", 1L)
                        .header("X-Admin-Token", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("DELETE /api/admin/knowledge/documents/{id} returns code 404 when document is missing")
    void deleteDocumentReturnsNotFoundWhenMissing() throws Exception {
        when(knowledgeDocumentService.deleteById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/admin/knowledge/documents/{id}", 99L)
                        .header("X-Admin-Token", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Knowledge document not found"));
    }

    @Test
    @DisplayName("HEAD /api/admin/knowledge/documents/{id} returns HTTP 200 when document exists")
    void headDocumentReturnsOkWhenExists() throws Exception {
        when(knowledgeDocumentService.exists(1L)).thenReturn(true);

        mockMvc.perform(head("/api/admin/knowledge/documents/{id}", 1L)
                        .header("X-Admin-Token", ADMIN_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HEAD /api/admin/knowledge/documents/{id} returns HTTP 404 when document is missing")
    void headDocumentReturnsNotFoundWhenMissing() throws Exception {
        when(knowledgeDocumentService.exists(99L)).thenReturn(false);

        mockMvc.perform(head("/api/admin/knowledge/documents/{id}", 99L)
                        .header("X-Admin-Token", ADMIN_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("OPTIONS /api/admin/knowledge/documents returns collection methods")
    void optionsCollectionReturnsAllowedMethods() throws Exception {
        mockMvc.perform(options("/api/admin/knowledge/documents"))
                .andExpect(status().isOk())
                .andExpect(header().string("Allow", "POST,GET,OPTIONS"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.methods[0]").value("POST"))
                .andExpect(jsonPath("$.data.methods[1]").value("GET"))
                .andExpect(jsonPath("$.data.methods[2]").value("OPTIONS"));
    }

    @Test
    @DisplayName("OPTIONS /api/admin/knowledge/documents/{id} returns item methods")
    void optionsItemReturnsAllowedMethods() throws Exception {
        mockMvc.perform(options("/api/admin/knowledge/documents/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(header().string("Allow", "GET,PUT,DELETE,HEAD,PATCH,OPTIONS"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.methods[4]").value("PATCH"));
    }

    @Test
    @DisplayName("PATCH /api/admin/knowledge/documents/{id}/status updates document status")
    void patchStatusReturnsUpdatedDocument() throws Exception {
        KnowledgeDocumentVO published = sampleDocument();
        published.setStatus("PUBLISHED");
        when(knowledgeDocumentService.updateStatus(eq(1L), any(KnowledgeDocumentStatusUpdateDTO.class))).thenReturn(published);

        mockMvc.perform(patch("/api/admin/knowledge/documents/{id}/status", 1L)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PUBLISHED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("PATCH /api/admin/knowledge/documents/{id}/status returns validation error when status is missing")
    void patchStatusReturnsValidationErrorWhenStatusMissing() throws Exception {
        mockMvc.perform(patch("/api/admin/knowledge/documents/{id}/status", 1L)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("status is required"));
    }

    @Test
    @DisplayName("PATCH /api/admin/knowledge/documents/{id}/status returns code 404 when document is missing")
    void patchStatusReturnsNotFoundWhenMissing() throws Exception {
        when(knowledgeDocumentService.updateStatus(eq(99L), any(KnowledgeDocumentStatusUpdateDTO.class))).thenReturn(null);

        mockMvc.perform(patch("/api/admin/knowledge/documents/{id}/status", 99L)
                        .header("X-Admin-Token", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PUBLISHED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Knowledge document not found"));
    }

    private KnowledgeDocumentVO sampleDocument() {
        KnowledgeDocumentVO document = new KnowledgeDocumentVO();
        document.setId(1L);
        document.setPublicId("ABCDEF1234567890ABCDEF1234");
        document.setTitle("中华人民共和国劳动合同法");
        document.setDocumentType("LAW");
        document.setIssuingAuthority("全国人民代表大会常务委员会");
        document.setCanonicalSourceUrl("https://example.test/labor-contract-law");
        document.setJurisdictionCode("CN");
        document.setScopeText("全国范围");
        document.setAuthorityLevel(100);
        document.setCurrentVersionId(null);
        document.setStatus("DRAFT");
        document.setCreatedBy(null);
        document.setLockVersion(0);
        document.setCreatedAt(LocalDateTime.of(2026, 7, 20, 9, 0));
        document.setUpdatedAt(LocalDateTime.of(2026, 7, 20, 9, 0));
        return document;
    }
}
