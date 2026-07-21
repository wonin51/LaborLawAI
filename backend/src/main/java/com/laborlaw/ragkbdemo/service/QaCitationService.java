package com.laborlaw.ragkbdemo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laborlaw.ragkbdemo.dto.QaCitationQueryDTO;
import com.laborlaw.ragkbdemo.entity.QaAnswerCitation;
import com.laborlaw.ragkbdemo.mapper.QaAnswerCitationMapper;
import com.laborlaw.ragkbdemo.vo.PageVO;
import com.laborlaw.ragkbdemo.vo.QaCitationVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QaCitationService {

    private final QaAnswerCitationMapper qaAnswerCitationMapper;

    public QaCitationService(QaAnswerCitationMapper qaAnswerCitationMapper) {
        this.qaAnswerCitationMapper = qaAnswerCitationMapper;
    }

    /**
     * Queries QA answer citations with filters and pagination.
     */
    public PageVO<QaCitationVO> pageCitations(QaCitationQueryDTO query) {
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        Page<QaAnswerCitation> page = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<QaAnswerCitation> wrapper = new LambdaQueryWrapper<QaAnswerCitation>()
                .eq(query.getClaimId() != null, QaAnswerCitation::getClaimId, query.getClaimId())
                .eq(query.getDocumentVersionId() != null, QaAnswerCitation::getDocumentVersionId, query.getDocumentVersionId())
                .eq(query.getChunkRefId() != null, QaAnswerCitation::getChunkRefId, query.getChunkRefId())
                .orderByDesc(QaAnswerCitation::getId);

        Page<QaAnswerCitation> result = qaAnswerCitationMapper.selectPage(page, wrapper);
        List<QaCitationVO> records = result.getRecords().stream()
                .map(this::toCitationVO)
                .toList();
        return new PageVO<>(records, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    private QaCitationVO toCitationVO(QaAnswerCitation citation) {
        if (citation == null) {
            return null;
        }
        QaCitationVO vo = new QaCitationVO();
        vo.setId(citation.getId());
        vo.setClaimId(citation.getClaimId());
        vo.setDocumentVersionId(citation.getDocumentVersionId());
        vo.setChunkRefId(citation.getChunkRefId());
        vo.setCitationNo(citation.getCitationNo());
        vo.setRetrievalRank(citation.getRetrievalRank());
        vo.setRelevanceScore(citation.getRelevanceScore());
        vo.setSourceTitleSnapshot(citation.getSourceTitleSnapshot());
        vo.setSourceLabelSnapshot(citation.getSourceLabelSnapshot());
        vo.setSectionSnapshot(citation.getSectionSnapshot());
        vo.setArticleNoSnapshot(citation.getArticleNoSnapshot());
        vo.setSourceUrlSnapshot(citation.getSourceUrlSnapshot());
        vo.setQuoteSnapshot(citation.getQuoteSnapshot());
        vo.setCreatedAt(citation.getCreatedAt());
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
