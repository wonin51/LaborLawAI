package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.config.AdminAccessInterceptor;
import com.laborlaw.ragkbdemo.config.AdminAuditInterceptor;
import com.laborlaw.ragkbdemo.dto.KbChunkRefQueryDTO;
import com.laborlaw.ragkbdemo.dto.KbDocumentQueryDTO;
import com.laborlaw.ragkbdemo.dto.QaCitationQueryDTO;
import com.laborlaw.ragkbdemo.dto.QaRecordQueryDTO;
import com.laborlaw.ragkbdemo.service.KnowledgeSourceService;
import com.laborlaw.ragkbdemo.service.QaCitationService;
import com.laborlaw.ragkbdemo.service.QaRecordService;
import com.laborlaw.ragkbdemo.vo.KbChunkRefVO;
import com.laborlaw.ragkbdemo.vo.KbDocumentQueryVO;
import com.laborlaw.ragkbdemo.vo.PageVO;
import com.laborlaw.ragkbdemo.vo.QaCitationVO;
import com.laborlaw.ragkbdemo.vo.QaRecordVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({KnowledgeSourceController.class, QaRecordController.class, QaCitationController.class})
class ReadOnlyQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminAccessInterceptor adminAccessInterceptor;

    @MockBean
    private AdminAuditInterceptor adminAuditInterceptor;

    @MockBean
    private KnowledgeSourceService knowledgeSourceService;

    @MockBean
    private QaRecordService qaRecordService;

    @MockBean
    private QaCitationService qaCitationService;

    @Test
    @DisplayName("GET /api/knowledge/documents returns paged documents and passes filters")
    void pageKnowledgeDocumentsReturnsPagedDocumentsAndPassesFilters() throws Exception {
        KbDocumentQueryVO document = new KbDocumentQueryVO();
        document.setId(9L);
        document.setPublicId("DOC00000000000000000000001");
        document.setTitle("中华人民共和国劳动合同法");
        document.setDocumentType("LAW");
        document.setIssuingAuthority("全国人民代表大会常务委员会");
        document.setJurisdictionCode("CN");
        document.setScopeText("全国范围");
        document.setAuthorityLevel(100);
        document.setStatus("PUBLISHED");
        document.setCreatedAt(LocalDateTime.of(2026, 7, 21, 10, 0));
        document.setUpdatedAt(LocalDateTime.of(2026, 7, 21, 11, 0));
        when(knowledgeSourceService.pageDocuments(any(KbDocumentQueryDTO.class)))
                .thenReturn(new PageVO<>(List.of(document), 1L, 2L, 20L, 1L));

        mockMvc.perform(get("/api/knowledge/documents")
                        .param("title", "劳动合同")
                        .param("document_type", "LAW")
                        .param("status", "PUBLISHED")
                        .param("page_no", "2")
                        .param("page_size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].id").value(9))
                .andExpect(jsonPath("$.data.records[0].title").value("中华人民共和国劳动合同法"))
                .andExpect(jsonPath("$.data.records[0].document_type").value("LAW"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.pageNo").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(20));

        ArgumentCaptor<KbDocumentQueryDTO> captor = ArgumentCaptor.forClass(KbDocumentQueryDTO.class);
        verify(knowledgeSourceService).pageDocuments(captor.capture());
        KbDocumentQueryDTO query = captor.getValue();
        assertThat(query.getTitle()).isEqualTo("劳动合同");
        assertThat(query.getDocumentType()).isEqualTo("LAW");
        assertThat(query.getStatus()).isEqualTo("PUBLISHED");
        assertThat(query.getPageNo()).isEqualTo(2L);
        assertThat(query.getPageSize()).isEqualTo(20L);
    }

    @Test
    @DisplayName("GET /api/knowledge/chunk-refs returns paged chunk refs and passes filters")
    void pageKnowledgeChunkRefsReturnsPagedChunkRefsAndPassesFilters() throws Exception {
        KbChunkRefVO chunk = new KbChunkRefVO();
        chunk.setId(8L);
        chunk.setDocumentVersionId(3L);
        chunk.setChunkKey("law-3-001");
        chunk.setChunkNo(1);
        chunk.setSectionPath("第一章 总则");
        chunk.setArticleNo("第一条");
        chunk.setTokenCount(128);
        chunk.setStatus("ACTIVE");
        chunk.setCreatedAt(LocalDateTime.of(2026, 7, 21, 10, 0));
        when(knowledgeSourceService.pageChunkRefs(any(KbChunkRefQueryDTO.class)))
                .thenReturn(new PageVO<>(List.of(chunk), 1L, 1L, 10L, 1L));

        mockMvc.perform(get("/api/knowledge/chunk-refs")
                        .param("document_version_id", "3")
                        .param("status", "ACTIVE")
                        .param("chunk_key", "law")
                        .param("section_path", "总则")
                        .param("page_no", "1")
                        .param("page_size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].id").value(8))
                .andExpect(jsonPath("$.data.records[0].document_version_id").value(3))
                .andExpect(jsonPath("$.data.records[0].chunk_key").value("law-3-001"));

        ArgumentCaptor<KbChunkRefQueryDTO> captor = ArgumentCaptor.forClass(KbChunkRefQueryDTO.class);
        verify(knowledgeSourceService).pageChunkRefs(captor.capture());
        KbChunkRefQueryDTO query = captor.getValue();
        assertThat(query.getDocumentVersionId()).isEqualTo(3L);
        assertThat(query.getStatus()).isEqualTo("ACTIVE");
        assertThat(query.getChunkKey()).isEqualTo("law");
        assertThat(query.getSectionPath()).isEqualTo("总则");
        assertThat(query.getPageNo()).isEqualTo(1L);
        assertThat(query.getPageSize()).isEqualTo(10L);
    }

    @Test
    @DisplayName("GET /api/qa/records returns paged QA records with question field and passes filters")
    void pageQaRecordsReturnsPagedRecordsWithQuestionFieldAndPassesFilters() throws Exception {
        QaRecordVO record = new QaRecordVO();
        record.setId(7L);
        record.setQuestion("入职两个月没签劳动合同怎么办？");
        record.setAnswerStatus("COMPLETED");
        record.setConclusionSummary("可以评估二倍工资差额请求");
        record.setCitationCoverage(new BigDecimal("0.90"));
        record.setCreatedAt(LocalDateTime.of(2026, 7, 21, 10, 0));
        when(qaRecordService.pageRecords(any(QaRecordQueryDTO.class)))
                .thenReturn(new PageVO<>(List.of(record), 1L, 3L, 15L, 1L));

        mockMvc.perform(get("/api/qa/records")
                        .param("answer_status", "COMPLETED")
                        .param("topic_id", "6")
                        .param("page_no", "3")
                        .param("page_size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].id").value(7))
                .andExpect(jsonPath("$.data.records[0].question").value("入职两个月没签劳动合同怎么办？"))
                .andExpect(jsonPath("$.data.records[0].answer_status").value("COMPLETED"));

        ArgumentCaptor<QaRecordQueryDTO> captor = ArgumentCaptor.forClass(QaRecordQueryDTO.class);
        verify(qaRecordService).pageRecords(captor.capture());
        QaRecordQueryDTO query = captor.getValue();
        assertThat(query.getAnswerStatus()).isEqualTo("COMPLETED");
        assertThat(query.getTopicId()).isEqualTo(6L);
        assertThat(query.getPageNo()).isEqualTo(3L);
        assertThat(query.getPageSize()).isEqualTo(15L);
    }

    @Test
    @DisplayName("GET /api/qa/citations returns paged citations and passes filters")
    void pageQaCitationsReturnsPagedCitationsAndPassesFilters() throws Exception {
        QaCitationVO citation = new QaCitationVO();
        citation.setId(6L);
        citation.setClaimId(5L);
        citation.setDocumentVersionId(4L);
        citation.setChunkRefId(3L);
        citation.setCitationNo(1);
        citation.setRetrievalRank(1);
        citation.setRelevanceScore(new BigDecimal("0.88"));
        citation.setSourceTitleSnapshot("中华人民共和国劳动合同法");
        citation.setQuoteSnapshot("建立劳动关系，应当订立书面劳动合同。");
        citation.setCreatedAt(LocalDateTime.of(2026, 7, 21, 10, 0));
        when(qaCitationService.pageCitations(any(QaCitationQueryDTO.class)))
                .thenReturn(new PageVO<>(List.of(citation), 1L, 1L, 10L, 1L));

        mockMvc.perform(get("/api/qa/citations")
                        .param("claim_id", "5")
                        .param("document_version_id", "4")
                        .param("chunk_ref_id", "3")
                        .param("page_no", "1")
                        .param("page_size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].id").value(6))
                .andExpect(jsonPath("$.data.records[0].claim_id").value(5))
                .andExpect(jsonPath("$.data.records[0].quote_snapshot").value("建立劳动关系，应当订立书面劳动合同。"));

        ArgumentCaptor<QaCitationQueryDTO> captor = ArgumentCaptor.forClass(QaCitationQueryDTO.class);
        verify(qaCitationService).pageCitations(captor.capture());
        QaCitationQueryDTO query = captor.getValue();
        assertThat(query.getClaimId()).isEqualTo(5L);
        assertThat(query.getDocumentVersionId()).isEqualTo(4L);
        assertThat(query.getChunkRefId()).isEqualTo(3L);
        assertThat(query.getPageNo()).isEqualTo(1L);
        assertThat(query.getPageSize()).isEqualTo(10L);
    }
}
