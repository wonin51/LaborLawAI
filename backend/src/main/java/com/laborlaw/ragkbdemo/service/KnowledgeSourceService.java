package com.laborlaw.ragkbdemo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laborlaw.ragkbdemo.dto.KbDocumentQueryDTO;
import com.laborlaw.ragkbdemo.dto.KbChunkRefQueryDTO;
import com.laborlaw.ragkbdemo.entity.KbChunkRef;
import com.laborlaw.ragkbdemo.entity.KnowledgeDocument;
import com.laborlaw.ragkbdemo.mapper.KbChunkRefMapper;
import com.laborlaw.ragkbdemo.mapper.KnowledgeDocumentMapper;
import com.laborlaw.ragkbdemo.vo.KbChunkRefVO;
import com.laborlaw.ragkbdemo.vo.KbDocumentQueryVO;
import com.laborlaw.ragkbdemo.vo.PageVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class KnowledgeSourceService {

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final KbChunkRefMapper kbChunkRefMapper;

    public KnowledgeSourceService(KnowledgeDocumentMapper knowledgeDocumentMapper,
                                   KbChunkRefMapper kbChunkRefMapper) {
        this.knowledgeDocumentMapper = knowledgeDocumentMapper;
        this.kbChunkRefMapper = kbChunkRefMapper;
    }

    /**
     * Queries knowledge documents with filters and pagination.
     */
    public PageVO<KbDocumentQueryVO> pageDocuments(KbDocumentQueryDTO query) {
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        Page<KnowledgeDocument> page = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<KnowledgeDocument>()
                .like(StringUtils.hasText(query.getTitle()), KnowledgeDocument::getTitle, query.getTitle())
                .eq(StringUtils.hasText(query.getDocumentType()), KnowledgeDocument::getDocumentType, query.getDocumentType())
                .eq(StringUtils.hasText(query.getStatus()), KnowledgeDocument::getStatus, query.getStatus())
                .orderByDesc(KnowledgeDocument::getId);

        Page<KnowledgeDocument> result = knowledgeDocumentMapper.selectPage(page, wrapper);
        List<KbDocumentQueryVO> records = result.getRecords().stream()
                .map(this::toDocumentVO)
                .toList();
        return new PageVO<>(records, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    /**
     * Queries knowledge chunk refs with filters and pagination.
     */
    public PageVO<KbChunkRefVO> pageChunkRefs(KbChunkRefQueryDTO query) {
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        Page<KbChunkRef> page = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<KbChunkRef> wrapper = new LambdaQueryWrapper<KbChunkRef>()
                .eq(query.getDocumentVersionId() != null, KbChunkRef::getDocumentVersionId, query.getDocumentVersionId())
                .eq(StringUtils.hasText(query.getStatus()), KbChunkRef::getStatus, query.getStatus())
                .like(StringUtils.hasText(query.getChunkKey()), KbChunkRef::getChunkKey, query.getChunkKey())
                .like(StringUtils.hasText(query.getSectionPath()), KbChunkRef::getSectionPath, query.getSectionPath())
                .orderByDesc(KbChunkRef::getId);

        Page<KbChunkRef> result = kbChunkRefMapper.selectPage(page, wrapper);
        List<KbChunkRefVO> records = result.getRecords().stream()
                .map(this::toChunkRefVO)
                .toList();
        return new PageVO<>(records, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    private KbDocumentQueryVO toDocumentVO(KnowledgeDocument doc) {
        if (doc == null) {
            return null;
        }
        KbDocumentQueryVO vo = new KbDocumentQueryVO();
        vo.setId(doc.getId());
        vo.setPublicId(doc.getPublicId());
        vo.setTitle(doc.getTitle());
        vo.setDocumentType(doc.getDocumentType());
        vo.setIssuingAuthority(doc.getIssuingAuthority());
        vo.setJurisdictionCode(doc.getJurisdictionCode());
        vo.setScopeText(doc.getScopeText());
        vo.setAuthorityLevel(doc.getAuthorityLevel());
        vo.setStatus(doc.getStatus());
        vo.setCreatedAt(doc.getCreatedAt());
        vo.setUpdatedAt(doc.getUpdatedAt());
        return vo;
    }

    private KbChunkRefVO toChunkRefVO(KbChunkRef chunk) {
        if (chunk == null) {
            return null;
        }
        KbChunkRefVO vo = new KbChunkRefVO();
        vo.setId(chunk.getId());
        vo.setDocumentVersionId(chunk.getDocumentVersionId());
        vo.setChunkKey(chunk.getChunkKey());
        vo.setChunkNo(chunk.getChunkNo());
        vo.setSectionPath(chunk.getSectionPath());
        vo.setArticleNo(chunk.getArticleNo());
        vo.setTokenCount(chunk.getTokenCount());
        vo.setStatus(chunk.getStatus());
        vo.setCreatedAt(chunk.getCreatedAt());
        return vo;
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
}
