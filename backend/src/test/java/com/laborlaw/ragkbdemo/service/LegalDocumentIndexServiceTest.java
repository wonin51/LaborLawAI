package com.laborlaw.ragkbdemo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.laborlaw.ragkbdemo.entity.LegalChunk;
import com.laborlaw.ragkbdemo.entity.LegalDocument;
import com.laborlaw.ragkbdemo.exception.ApiOperationException;
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
        LegalDocumentMapper documentMapper = mock(LegalDocumentMapper.class);
        LegalChunkMapper chunkMapper = mock(LegalChunkMapper.class);
        EsIndexService esIndexService = mock(EsIndexService.class);

        LegalDocumentIndexService service = new LegalDocumentIndexService(documentMapper, chunkMapper, esIndexService);
        LegalDocument document = new LegalDocument();
        document.setId(1L);
        document.setStatus("PUBLISHED");
        when(documentMapper.selectById(1L)).thenReturn(document);
        when(chunkMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L, 2L, 0L);

        LegalDocumentIndexResultVO result = service.index(1L);

        assertThat(result.getDocumentId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("indexed");
        verify(esIndexService, never()).splitLegalDocument(any());
        verify(esIndexService, never()).embedText(any());
    }

    @Test
    void indexSplitsDocumentAndWritesChunksToEs() {
        LegalDocumentMapper documentMapper = mock(LegalDocumentMapper.class);
        LegalChunkMapper chunkMapper = mock(LegalChunkMapper.class);
        EsIndexService esIndexService = mock(EsIndexService.class);

        LegalDocumentIndexService service = new LegalDocumentIndexService(documentMapper, chunkMapper, esIndexService);
        LegalDocument document = new LegalDocument();
        document.setId(1L);
        document.setGroupCode("G1");
        document.setSourceTitle("Source title");
        document.setSourceUrl("https://example.test/doc");
        document.setDocType("LAW");
        document.setTopicTags("contract,wage");
        document.setEffectiveStatus("effective");
        document.setStatus("PUBLISHED");
        document.setRawText("第1条 为了保护劳动者。\n\n第二段自然段。");
        when(documentMapper.selectById(1L)).thenReturn(document);
        when(chunkMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L, 0L, 0L);
        when(esIndexService.contentVectorDimension()).thenReturn(2);
        when(esIndexService.splitLegalDocument(document.getRawText())).thenReturn(List.of(
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
        ArgumentCaptor<LegalDocument> documentCaptor = ArgumentCaptor.forClass(LegalDocument.class);
        verify(documentMapper).updateById(documentCaptor.capture());
        assertThat(documentCaptor.getValue().getStatus()).isEqualTo("indexed");
        ArgumentCaptor<LegalChunk> captor = ArgumentCaptor.forClass(LegalChunk.class);
        verify(chunkMapper, org.mockito.Mockito.times(2)).insert(captor.capture());
        assertThat(captor.getAllValues()).hasSize(2);
        assertThat(captor.getAllValues()).allSatisfy(chunk -> assertThat(chunk.getContentHash()).hasSize(64));
    }

    @Test
    void indexRejectsDisabledDocument() {
        LegalDocumentMapper documentMapper = mock(LegalDocumentMapper.class);
        LegalChunkMapper chunkMapper = mock(LegalChunkMapper.class);
        EsIndexService esIndexService = mock(EsIndexService.class);

        LegalDocumentIndexService service = new LegalDocumentIndexService(documentMapper, chunkMapper, esIndexService);
        LegalDocument document = new LegalDocument();
        document.setId(1L);
        document.setStatus("disabled");
        when(documentMapper.selectById(1L)).thenReturn(document);

        assertThatThrownBy(() -> service.index(1L))
                .isInstanceOf(ApiOperationException.class)
                .hasMessage("\u6587\u6863\u5df2\u7981\u7528\uff0c\u7981\u6b62\u7d22\u5f15");
    }
}
