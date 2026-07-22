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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LegalDocumentIndexService {

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_INDEXED = "indexed";
    private static final String STATUS_FAILED = "failed";
    private static final String STATUS_INDEX_FAILED = "index_failed";
    private static final String DOCUMENT_DISABLED = "disabled";

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final LegalDocumentMapper legalDocumentMapper;
    private final LegalChunkMapper legalChunkMapper;
    private final EsIndexService esIndexService;

    public LegalDocumentIndexService(
            KnowledgeDocumentMapper knowledgeDocumentMapper,
            LegalDocumentMapper legalDocumentMapper,
            LegalChunkMapper legalChunkMapper,
            EsIndexService esIndexService) {
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
        this.legalDocumentMapper = legalDocumentMapper;
        this.legalChunkMapper = legalChunkMapper;
        this.esIndexService = esIndexService;
    }

    @Transactional(rollbackFor = Exception.class)
    public LegalDocumentIndexResultVO index(Long id) {
        IndexSource source = resolveSource(id);
        if (source == null) {
            throw new ApiOperationException(404, "\u6cd5\u5f8b\u6587\u6863\u4e0d\u5b58\u5728");
        }
        if (DOCUMENT_DISABLED.equalsIgnoreCase(normalize(source.effectiveStatus()))) {
            throw new ApiOperationException(400, "\u6587\u6863\u5df2\u7981\u7528\uff0c\u7981\u6b62\u7d22\u5f15");
        }

        if (hasIndexedChunk(source)) {
            return summarize(source, STATUS_INDEXED);
        }

        String rawText = source.rawText();
        if (!StringUtils.hasText(rawText)) {
            throw new ApiOperationException(400, "raw_text\u4e0d\u80fd\u4e3a\u7a7a");
        }

        esIndexService.initIndex(EsIndexService.DEFAULT_INDEX_NAME);

        List<EsIndexService.LegalKnowledgeChunk> chunks = esIndexService.splitLegalDocument(rawText);
        int indexedCount = 0;
        int failedCount = 0;

        for (EsIndexService.LegalKnowledgeChunk chunk : chunks) {
            LegalChunk legalChunk = buildChunk(source, chunk);

            try {
                legalChunkMapper.insert(legalChunk);
            } catch (Exception ex) {
                failedCount++;
                continue;
            }

            try {
                List<Double> vector = truncateVector(esIndexService.embedText(chunk.content()));
                String esDocId = String.valueOf(legalChunk.getId());
                esIndexService.indexDocument(EsIndexService.DEFAULT_INDEX_NAME, esDocId, buildEsPayload(source, legalChunk, vector));
                legalChunk.setEsDocId(esDocId);
                legalChunk.setStatus(STATUS_INDEXED);
                legalChunkMapper.updateById(legalChunk);
                indexedCount++;
            } catch (Exception ex) {
                failedCount++;
                legalChunk.setStatus(STATUS_FAILED);
                legalChunkMapper.updateById(legalChunk);
            }
        }

        String finalStatus = failedCount == 0 ? STATUS_INDEXED : STATUS_INDEX_FAILED;
        updateSourceStatus(source, finalStatus);

        return buildResult(id, chunks.size(), indexedCount, failedCount, finalStatus);
    }

    private IndexSource resolveSource(Long id) {
        KnowledgeDocument knowledgeDocument = knowledgeDocumentMapper.selectById(id);
        if (knowledgeDocument != null) {
            return buildKnowledgeSource(knowledgeDocument);
        }

        LegalDocument legalDocument = legalDocumentMapper.selectById(id);
        if (legalDocument != null) {
            return buildLegacySource(legalDocument);
        }

        return null;
    }

    private boolean hasIndexedChunk(IndexSource source) {
        return legalChunkMapper.selectCount(new LambdaQueryWrapper<LegalChunk>()
                .eq(LegalChunk::getDocumentId, source.documentId())
                .eq(StringUtils.hasText(source.groupCode()), LegalChunk::getGroupCode, source.groupCode())
                .eq(LegalChunk::getStatus, STATUS_INDEXED)) > 0;
    }

    private LegalDocumentIndexResultVO summarize(IndexSource source, String status) {
        int chunkCount = Math.toIntExact(legalChunkMapper.selectCount(new LambdaQueryWrapper<LegalChunk>()
                .eq(LegalChunk::getDocumentId, source.documentId())
                .eq(StringUtils.hasText(source.groupCode()), LegalChunk::getGroupCode, source.groupCode())));
        int indexedCount = Math.toIntExact(legalChunkMapper.selectCount(new LambdaQueryWrapper<LegalChunk>()
                .eq(LegalChunk::getDocumentId, source.documentId())
                .eq(StringUtils.hasText(source.groupCode()), LegalChunk::getGroupCode, source.groupCode())
                .eq(LegalChunk::getStatus, STATUS_INDEXED)));
        int failedCount = Math.toIntExact(legalChunkMapper.selectCount(new LambdaQueryWrapper<LegalChunk>()
                .eq(LegalChunk::getDocumentId, source.documentId())
                .eq(StringUtils.hasText(source.groupCode()), LegalChunk::getGroupCode, source.groupCode())
                .eq(LegalChunk::getStatus, STATUS_FAILED)));
        return buildResult(source.documentId(), chunkCount, indexedCount, failedCount, status);
    }

    private LegalChunk buildChunk(IndexSource source, EsIndexService.LegalKnowledgeChunk chunk) {
        LegalChunk legalChunk = new LegalChunk();
        legalChunk.setGroupCode(source.groupCode());
        legalChunk.setDocumentId(source.documentId());
        legalChunk.setChunkIndex(chunk.chunkIndex());
        legalChunk.setArticleNo(chunk.articleNo());
        legalChunk.setSectionTitle(chunk.sectionTitle());
        legalChunk.setTopicTags(resolveTopicTags(source.topicTags(), chunk.topicTags()));
        legalChunk.setContent(chunk.content());
        legalChunk.setContentHash(sha256(chunk.content()));
        legalChunk.setSourceUrl(source.sourceUrl());
        legalChunk.setAuthorityLevel(source.authorityLevel());
        legalChunk.setEffectiveStatus(source.effectiveStatus());
        legalChunk.setStatus(STATUS_PENDING);
        legalChunk.setCreatedAt(LocalDateTime.now());
        return legalChunk;
    }

    private Map<String, Object> buildEsPayload(IndexSource source, LegalChunk legalChunk, List<Double> vector) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", legalChunk.getId());
        payload.put("group_code", legalChunk.getGroupCode());
        payload.put("document_id", legalChunk.getDocumentId());
        payload.put("article_no", legalChunk.getArticleNo());
        payload.put("section_title", legalChunk.getSectionTitle());
        payload.put("topic_tags", splitTopicTags(legalChunk.getTopicTags()));
        payload.put("content", legalChunk.getContent());
        payload.put("content_vector", vector);
        payload.put("source_title", source.sourceTitle());
        payload.put("source_url", legalChunk.getSourceUrl());
        payload.put("authority_level", legalChunk.getAuthorityLevel());
        payload.put("effective_status", legalChunk.getEffectiveStatus());
        payload.put("created_at", legalChunk.getCreatedAt());
        return payload;
    }

    private LegalDocumentIndexResultVO buildResult(Long documentId, int chunkCount, int indexedCount, int failedCount, String status) {
        LegalDocumentIndexResultVO vo = new LegalDocumentIndexResultVO();
        vo.setDocumentId(documentId);
        vo.setChunkCount(chunkCount);
        vo.setIndexedCount(indexedCount);
        vo.setFailedCount(failedCount);
        vo.setStatus(status);
        return vo;
    }

    private String resolveTopicTags(String documentTags, List<String> chunkTags) {
        if (chunkTags != null && !chunkTags.isEmpty()) {
            return String.join(",", chunkTags);
        }
        return normalize(documentTags);
    }

    private List<String> splitTopicTags(String topicTags) {
        if (!StringUtils.hasText(topicTags)) {
            return List.of();
        }
        return Arrays.stream(topicTags.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toUnmodifiableList());
    }

    private String mapAuthorityLevel(String docType) {
        String normalized = normalize(docType).toUpperCase();
        return switch (normalized) {
            case "LAW" -> "1";
            case "JUDICIAL_INTERPRETATION" -> "2";
            case "LOCAL_LAW" -> "3";
            case "POLICY" -> "4";
            case "REGULATION" -> "5";
            default -> "9";
        };
    }

    private IndexSource buildKnowledgeSource(KnowledgeDocument document) {
        return new IndexSource(
                true,
                document.getId(),
                firstNonBlank(document.getPublicId(), String.valueOf(document.getId())),
                document.getTitle(),
                document.getCanonicalSourceUrl(),
                document.getAuthorityLevel() == null ? "" : String.valueOf(document.getAuthorityLevel()),
                document.getScopeText(),
                normalize(document.getStatus()),
                joinNonEmpty(document.getTitle(), document.getScopeText())
        );
    }

    private IndexSource buildLegacySource(LegalDocument document) {
        return new IndexSource(
                false,
                document.getId(),
                normalize(document.getGroupCode()),
                document.getSourceTitle(),
                document.getSourceUrl(),
                mapAuthorityLevel(document.getDocType()),
                document.getTopicTags(),
                normalize(document.getEffectiveStatus()),
                document.getRawText()
        );
    }

    private void updateSourceStatus(IndexSource source, String status) {
        if (source.knowledgeDocument()) {
            KnowledgeDocument document = knowledgeDocumentMapper.selectById(source.documentId());
            if (document != null) {
                document.setStatus(status);
                knowledgeDocumentMapper.updateById(document);
            }
            return;
        }

        LegalDocument document = legalDocumentMapper.selectById(source.documentId());
        if (document != null) {
            document.setStatus(status);
            legalDocumentMapper.updateById(document);
        }
    }

    private List<Double> truncateVector(List<Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return List.of();
        }
        int maxDimension = esIndexService.contentVectorDimension();
        if (vector.size() <= maxDimension) {
            return List.copyOf(vector);
        }
        return List.copyOf(vector.subList(0, maxDimension));
    }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(Objects.requireNonNullElse(content, "").getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to calculate content hash", ex);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.hasText(first) ? first.trim() : normalize(second);
    }

    private String joinNonEmpty(String... values) {
        return Arrays.stream(values)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining("\n\n"));
    }

    private record IndexSource(
            boolean knowledgeDocument,
            Long documentId,
            String groupCode,
            String sourceTitle,
            String sourceUrl,
            String authorityLevel,
            String topicTags,
            String effectiveStatus,
            String rawText) {
    }
}
