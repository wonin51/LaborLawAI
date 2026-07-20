package com.laborlaw.ragkbdemo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laborlaw.ragkbdemo.dto.KnowledgeDocumentCreateDTO;
import com.laborlaw.ragkbdemo.dto.KnowledgeDocumentPageQueryDTO;
import com.laborlaw.ragkbdemo.dto.KnowledgeDocumentStatusUpdateDTO;
import com.laborlaw.ragkbdemo.dto.KnowledgeDocumentUpdateDTO;
import com.laborlaw.ragkbdemo.entity.KnowledgeDocument;
import com.laborlaw.ragkbdemo.mapper.KnowledgeDocumentMapper;
import com.laborlaw.ragkbdemo.vo.KnowledgeDocumentVO;
import com.laborlaw.ragkbdemo.vo.PageVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.List;

@Service
public class KnowledgeDocumentService {

    private static final String PUBLIC_ID_ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final int PUBLIC_ID_LENGTH = 26;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;

    public KnowledgeDocumentService(KnowledgeDocumentMapper knowledgeDocumentMapper) {
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
    }

    /**
     * Creates knowledge document metadata without handling file upload or parsing.
     * :param dto: Request data used to create document metadata.
     * :return: Created document metadata for API response.
     */
    public KnowledgeDocumentVO create(KnowledgeDocumentCreateDTO dto) {
        KnowledgeDocument document = new KnowledgeDocument();
        document.setPublicId(generatePublicId());
        document.setTitle(dto.getTitle());
        document.setDocumentType(dto.getDocumentType());
        document.setIssuingAuthority(dto.getIssuingAuthority());
        document.setCanonicalSourceUrl(dto.getCanonicalSourceUrl());
        document.setJurisdictionCode(defaultText(dto.getJurisdictionCode(), "CN"));
        document.setScopeText(dto.getScopeText());
        document.setAuthorityLevel(dto.getAuthorityLevel() == null ? 100 : dto.getAuthorityLevel());
        document.setStatus(defaultText(dto.getStatus(), "DRAFT"));
        document.setLockVersion(0);

        knowledgeDocumentMapper.insert(document);
        return toVO(knowledgeDocumentMapper.selectById(document.getId()));
    }

    /**
     * Queries knowledge documents with optional filters and bounded pagination.
     * :param query: Query conditions and pagination parameters.
     * :return: Paged document metadata.
     */
    public PageVO<KnowledgeDocumentVO> page(KnowledgeDocumentPageQueryDTO query) {
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        Page<KnowledgeDocument> page = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<KnowledgeDocument>()
                .like(StringUtils.hasText(query.getTitle()), KnowledgeDocument::getTitle, query.getTitle())
                .eq(StringUtils.hasText(query.getDocumentType()), KnowledgeDocument::getDocumentType, query.getDocumentType())
                .eq(StringUtils.hasText(query.getStatus()), KnowledgeDocument::getStatus, query.getStatus())
                .like(StringUtils.hasText(query.getIssuingAuthority()), KnowledgeDocument::getIssuingAuthority, query.getIssuingAuthority())
                .eq(StringUtils.hasText(query.getJurisdictionCode()), KnowledgeDocument::getJurisdictionCode, query.getJurisdictionCode())
                .orderByDesc(KnowledgeDocument::getUpdatedAt)
                .orderByDesc(KnowledgeDocument::getId);

        Page<KnowledgeDocument> result = knowledgeDocumentMapper.selectPage(page, wrapper);
        List<KnowledgeDocumentVO> records = result.getRecords().stream().map(this::toVO).toList();
        return new PageVO<>(records, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    /**
     * Finds a knowledge document by primary key.
     * :param id: Document primary key.
     * :return: Document metadata, or null when the document does not exist.
     */
    public KnowledgeDocumentVO getById(Long id) {
        return toVO(knowledgeDocumentMapper.selectById(id));
    }

    /**
     * Updates editable metadata fields for an existing knowledge document.
     * :param id: Document primary key.
     * :param dto: New metadata values.
     * :return: Updated document metadata, or null when the document does not exist.
     */
    public KnowledgeDocumentVO update(Long id, KnowledgeDocumentUpdateDTO dto) {
        KnowledgeDocument document = knowledgeDocumentMapper.selectById(id);
        if (document == null) {
            return null;
        }

        document.setTitle(dto.getTitle());
        document.setDocumentType(dto.getDocumentType());
        document.setIssuingAuthority(dto.getIssuingAuthority());
        document.setCanonicalSourceUrl(dto.getCanonicalSourceUrl());
        document.setJurisdictionCode(defaultText(dto.getJurisdictionCode(), "CN"));
        document.setScopeText(dto.getScopeText());
        document.setAuthorityLevel(dto.getAuthorityLevel() == null ? 100 : dto.getAuthorityLevel());
        document.setStatus(defaultText(dto.getStatus(), "DRAFT"));

        knowledgeDocumentMapper.updateById(document);
        return toVO(knowledgeDocumentMapper.selectById(id));
    }

    /**
     * Deletes a knowledge document by primary key.
     * :param id: Document primary key.
     * :return: true when deleted, null when the document does not exist.
     */
    public Boolean deleteById(Long id) {
        if (knowledgeDocumentMapper.selectById(id) == null) {
            return null;
        }

        try {
            knowledgeDocumentMapper.deleteById(id);
            return Boolean.TRUE;
        } catch (Exception ex) {
            throw new IllegalStateException("Delete knowledge document failed: " + rootMessage(ex), ex);
        }
    }

    /**
     * Checks whether a knowledge document exists.
     * :param id: Document primary key.
     * :return: true when the document exists; false otherwise.
     */
    public boolean exists(Long id) {
        return knowledgeDocumentMapper.selectCount(new LambdaQueryWrapper<KnowledgeDocument>()
                .eq(KnowledgeDocument::getId, id)) > 0;
    }

    /**
     * Updates only the status field for an existing knowledge document.
     * :param id: Document primary key.
     * :param dto: Status update request data.
     * :return: Updated document metadata, or null when the document does not exist.
     */
    public KnowledgeDocumentVO updateStatus(Long id, KnowledgeDocumentStatusUpdateDTO dto) {
        KnowledgeDocument document = knowledgeDocumentMapper.selectById(id);
        if (document == null) {
            return null;
        }

        document.setStatus(dto.getStatus());
        knowledgeDocumentMapper.updateById(document);
        return toVO(knowledgeDocumentMapper.selectById(id));
    }

    private KnowledgeDocumentVO toVO(KnowledgeDocument document) {
        if (document == null) {
            return null;
        }

        KnowledgeDocumentVO vo = new KnowledgeDocumentVO();
        vo.setId(document.getId());
        vo.setPublicId(document.getPublicId());
        vo.setTitle(document.getTitle());
        vo.setDocumentType(document.getDocumentType());
        vo.setIssuingAuthority(document.getIssuingAuthority());
        vo.setCanonicalSourceUrl(document.getCanonicalSourceUrl());
        vo.setJurisdictionCode(document.getJurisdictionCode());
        vo.setScopeText(document.getScopeText());
        vo.setAuthorityLevel(document.getAuthorityLevel());
        vo.setCurrentVersionId(document.getCurrentVersionId());
        vo.setStatus(document.getStatus());
        vo.setCreatedBy(document.getCreatedBy());
        vo.setLockVersion(document.getLockVersion());
        vo.setCreatedAt(document.getCreatedAt());
        vo.setUpdatedAt(document.getUpdatedAt());
        return vo;
    }

    private String generatePublicId() {
        for (int attempt = 0; attempt < 3; attempt++) {
            String publicId = randomPublicId();
            Long count = knowledgeDocumentMapper.selectCount(new LambdaQueryWrapper<KnowledgeDocument>()
                    .eq(KnowledgeDocument::getPublicId, publicId));
            if (count == 0) {
                return publicId;
            }
        }
        return randomPublicId();
    }

    private String randomPublicId() {
        StringBuilder builder = new StringBuilder(PUBLIC_ID_LENGTH);
        for (int i = 0; i < PUBLIC_ID_LENGTH; i++) {
            builder.append(PUBLIC_ID_ALPHABET.charAt(RANDOM.nextInt(PUBLIC_ID_ALPHABET.length())));
        }
        return builder.toString();
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private long normalizePageNo(Long pageNo) {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    private long normalizePageSize(Long pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? throwable.getMessage() : current.getMessage();
    }
}
