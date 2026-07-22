package com.laborlaw.ragkbdemo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laborlaw.ragkbdemo.entity.KnowledgeDocument;
import com.laborlaw.ragkbdemo.entity.LegalChunk;
import com.laborlaw.ragkbdemo.entity.LegalDocument;
import com.laborlaw.ragkbdemo.exception.ApiOperationException;
import com.laborlaw.ragkbdemo.mapper.KnowledgeDocumentMapper;
import com.laborlaw.ragkbdemo.mapper.LegalChunkMapper;
import com.laborlaw.ragkbdemo.mapper.LegalDocumentMapper;
import com.laborlaw.ragkbdemo.vo.LegalDocumentIndexResultVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LegalDocumentIndexServiceTest {

    @Test
    void indexReturnsExistingResultWhenIndexedChunkAlreadyExists() {
        KnowledgeDocumentMapper knowledgeDocumentMapper = mock(KnowledgeDocumentMapper.class);
        LegalDocumentMapper documentMapper = mock(LegalDocumentMapper.class);
        LegalChunkMapper chunkMapper = mock(LegalChunkMapper.class);
        EsIndexService esIndexService = mock(EsIndexService.class);

        LegalDocumentIndexService service = new LegalDocumentIndexService(knowledgeDocumentMapper, documentMapper, chunkMapper, esIndexService);
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(1L);
        document.setPublicId("PUB-1");
        document.setTitle("示例文档");
        document.setStatus("PUBLISHED");
        document.setScopeText("第一段内容");
        when(knowledgeDocumentMapper.selectById(1L)).thenReturn(document);
        when(chunkMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L, 2L, 0L);

        LegalDocumentIndexResultVO result = service.index(1L);

        assertThat(result.getDocumentId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("indexed");
        verify(esIndexService, never()).splitLegalDocument(any());
        verify(esIndexService, never()).embedText(any());
        verify(documentMapper, never()).selectById(any());
    }

    @Test
    void indexSplitsDocumentAndWritesChunksToEs() {
        KnowledgeDocumentMapper knowledgeDocumentMapper = mock(KnowledgeDocumentMapper.class);
        LegalDocumentMapper documentMapper = mock(LegalDocumentMapper.class);
        LegalChunkMapper chunkMapper = mock(LegalChunkMapper.class);
        EsIndexService esIndexService = mock(EsIndexService.class);

        LegalDocumentIndexService service = new LegalDocumentIndexService(knowledgeDocumentMapper, documentMapper, chunkMapper, esIndexService);
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(1L);
        document.setPublicId("PUB-1");
        document.setTitle("中华人民共和国劳动合同法");
        document.setCanonicalSourceUrl("https://example.test/doc");
        document.setDocumentType("LAW");
        document.setScopeText("第1条 为了保护劳动者。\n\n第二段自然段。");
        document.setAuthorityLevel(10);
        document.setStatus("PUBLISHED");
        when(knowledgeDocumentMapper.selectById(1L)).thenReturn(document);
        when(chunkMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L, 0L, 0L);
        when(esIndexService.contentVectorDimension()).thenReturn(2);
        when(esIndexService.splitLegalDocument(any())).thenReturn(List.of(
                new EsIndexService.LegalKnowledgeChunk(0, "第1条", null, List.of("contract"), "第1条 为了保护劳动者。"),
                new EsIndexService.LegalKnowledgeChunk(1, null, null, List.of(), "第二段自然段。")
        ));
        when(esIndexService.embedText(any())).thenReturn(List.of(0.1, 0.2, 0.3));
        when(chunkMapper.insert(any(LegalChunk.class))).thenAnswer(invocation -> {
            LegalChunk chunk = invocation.getArgument(0);
            chunk.setId(chunk.getChunkIndex().longValue() + 10);
            return 1;
        });
        when(chunkMapper.updateById(any(LegalChunk.class))).thenReturn(1);
        when(esIndexService.indexDocument(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(1));

        LegalDocumentIndexResultVO result = service.index(1L);

        assertThat(result.getChunkCount()).isEqualTo(2);
        assertThat(result.getIndexedCount()).isEqualTo(2);
        assertThat(result.getFailedCount()).isEqualTo(0);
        assertThat(result.getStatus()).isEqualTo("indexed");
        ArgumentCaptor<KnowledgeDocument> documentCaptor = ArgumentCaptor.forClass(KnowledgeDocument.class);
        verify(knowledgeDocumentMapper).updateById(documentCaptor.capture());
        assertThat(documentCaptor.getValue().getStatus()).isEqualTo("indexed");
        ArgumentCaptor<LegalChunk> captor = ArgumentCaptor.forClass(LegalChunk.class);
        verify(chunkMapper, org.mockito.Mockito.times(2)).insert(captor.capture());
        assertThat(captor.getAllValues()).hasSize(2);
        assertThat(captor.getAllValues()).allSatisfy(chunk -> assertThat(chunk.getContentHash()).hasSize(64));
    }

    @Test
    void indexRejectsDisabledDocument() {
        KnowledgeDocumentMapper knowledgeDocumentMapper = mock(KnowledgeDocumentMapper.class);
        LegalDocumentMapper documentMapper = mock(LegalDocumentMapper.class);
        LegalChunkMapper chunkMapper = mock(LegalChunkMapper.class);
        EsIndexService esIndexService = mock(EsIndexService.class);

        LegalDocumentIndexService service = new LegalDocumentIndexService(knowledgeDocumentMapper, documentMapper, chunkMapper, esIndexService);
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(1L);
        document.setStatus("disabled");
        when(knowledgeDocumentMapper.selectById(1L)).thenReturn(document);

        assertThatThrownBy(() -> service.index(1L))
                .isInstanceOf(ApiOperationException.class)
                .hasMessage("\u6587\u6863\u5df2\u7981\u7528\uff0c\u7981\u6b62\u7d22\u5f15");
    }

    @Test
    void indexFallsBackToLegacyLegalDocumentWhenKnowledgeDocumentMissing() {
        KnowledgeDocumentMapper knowledgeDocumentMapper = mock(KnowledgeDocumentMapper.class);
        LegalDocumentMapper documentMapper = mock(LegalDocumentMapper.class);
        LegalChunkMapper chunkMapper = mock(LegalChunkMapper.class);
        EsIndexService esIndexService = mock(EsIndexService.class);

        LegalDocumentIndexService service = new LegalDocumentIndexService(knowledgeDocumentMapper, documentMapper, chunkMapper, esIndexService);
        LegalDocument document = new LegalDocument();
        document.setId(8L);
        document.setGroupCode("G8");
        document.setSourceTitle("旧法文档");
        document.setSourceUrl("https://example.test/legacy");
        document.setDocType("LAW");
        document.setTopicTags("tag-a,tag-b");
        document.setEffectiveStatus("effective");
        document.setStatus("PUBLISHED");
        document.setRawText("第1条 旧法正文。");
        when(knowledgeDocumentMapper.selectById(8L)).thenReturn(null);
        when(documentMapper.selectById(8L)).thenReturn(document);
        when(chunkMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L, 0L, 0L);
        when(esIndexService.contentVectorDimension()).thenReturn(2);
        when(esIndexService.splitLegalDocument(document.getRawText())).thenReturn(List.of(
                new EsIndexService.LegalKnowledgeChunk(0, "第1条", null, List.of("tag-a"), "第1条 旧法正文。")
        ));
        when(esIndexService.embedText(any())).thenReturn(List.of(0.1, 0.2));
        when(chunkMapper.insert(any(LegalChunk.class))).thenAnswer(invocation -> {
            LegalChunk chunk = invocation.getArgument(0);
            chunk.setId(100L);
            return 1;
        });
        when(chunkMapper.updateById(any(LegalChunk.class))).thenReturn(1);
        when(esIndexService.indexDocument(any(), any(), any())).thenReturn("100");

        LegalDocumentIndexResultVO result = service.index(8L);

        assertThat(result.getChunkCount()).isEqualTo(1);
        assertThat(result.getIndexedCount()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo("indexed");
        verify(knowledgeDocumentMapper, org.mockito.Mockito.atLeastOnce()).selectById(8L);
        verify(documentMapper, org.mockito.Mockito.atLeastOnce()).selectById(8L);
    }
}
