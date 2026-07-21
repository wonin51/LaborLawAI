package com.laborlaw.ragkbdemo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laborlaw.ragkbdemo.dto.QaRecordQueryDTO;
import com.laborlaw.ragkbdemo.entity.QaAnswer;
import com.laborlaw.ragkbdemo.entity.QaMessage;
import com.laborlaw.ragkbdemo.mapper.QaAnswerMapper;
import com.laborlaw.ragkbdemo.mapper.QaMessageMapper;
import com.laborlaw.ragkbdemo.vo.PageVO;
import com.laborlaw.ragkbdemo.vo.QaRecordVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QaRecordService {

    private final QaAnswerMapper qaAnswerMapper;
    private final QaMessageMapper qaMessageMapper;

    public QaRecordService(QaAnswerMapper qaAnswerMapper, QaMessageMapper qaMessageMapper) {
        this.qaAnswerMapper = qaAnswerMapper;
        this.qaMessageMapper = qaMessageMapper;
    }

    /**
     * Queries QA records with filters and pagination.
     * Populates question field from qa_message by question_message_id.
     */
    public PageVO<QaRecordVO> pageRecords(QaRecordQueryDTO query) {
        long pageNo = normalizePageNo(query.getPageNo());
        long pageSize = normalizePageSize(query.getPageSize());
        Page<QaAnswer> page = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<QaAnswer> wrapper = new LambdaQueryWrapper<QaAnswer>()
                .eq(StringUtils.hasText(query.getAnswerStatus()), QaAnswer::getAnswerStatus, query.getAnswerStatus())
                .eq(query.getTopicId() != null, QaAnswer::getTopicId, query.getTopicId())
                .orderByDesc(QaAnswer::getId);

        Page<QaAnswer> result = qaAnswerMapper.selectPage(page, wrapper);

        // Batch fetch question messages
        Set<Long> questionMessageIds = result.getRecords().stream()
                .map(QaAnswer::getQuestionMessageId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, String> questionMap;
        if (!questionMessageIds.isEmpty()) {
            List<QaMessage> messages = qaMessageMapper.selectList(
                    new LambdaQueryWrapper<QaMessage>()
                            .in(QaMessage::getId, questionMessageIds));
            questionMap = messages.stream()
                    .collect(Collectors.toMap(QaMessage::getId, QaMessage::getContentRedacted));
        } else {
            questionMap = Map.of();
        }

        List<QaRecordVO> records = result.getRecords().stream()
                .map(answer -> toRecordVO(answer, questionMap.getOrDefault(answer.getQuestionMessageId(), "")))
                .toList();

        return new PageVO<>(records, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    private QaRecordVO toRecordVO(QaAnswer answer, String question) {
        if (answer == null) {
            return null;
        }
        QaRecordVO vo = new QaRecordVO();
        vo.setId(answer.getId());
        vo.setQuestion(question);
        vo.setConclusionSummary(answer.getConclusionSummary());
        vo.setAnswerStatus(answer.getAnswerStatus());
        vo.setActionSteps(answer.getActionSteps());
        vo.setEvidenceChecklist(answer.getEvidenceChecklist());
        vo.setCautions(answer.getCautions());
        vo.setCitationCoverage(answer.getCitationCoverage());
        vo.setCompletedAt(answer.getCompletedAt());
        vo.setCreatedAt(answer.getCreatedAt());
        vo.setUpdatedAt(answer.getUpdatedAt());
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
