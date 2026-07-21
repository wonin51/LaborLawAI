package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.dto.QaRecordQueryDTO;
import com.laborlaw.ragkbdemo.service.QaRecordService;
import com.laborlaw.ragkbdemo.vo.ApiResponse;
import com.laborlaw.ragkbdemo.vo.PageVO;
import com.laborlaw.ragkbdemo.vo.QaRecordVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qa")
public class QaRecordController {

    private final QaRecordService qaRecordService;

    public QaRecordController(QaRecordService qaRecordService) {
        this.qaRecordService = qaRecordService;
    }

    /**
     * Queries QA records with filters and pagination.
     */
    @GetMapping("/records")
    public ApiResponse<PageVO<QaRecordVO>> pageRecords(
            @RequestParam(value = "answer_status", required = false) String answerStatus,
            @RequestParam(value = "topic_id", required = false) Long topicId,
            @RequestParam(value = "page_no", required = false, defaultValue = "1") Long pageNo,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Long pageSize) {
        QaRecordQueryDTO query = new QaRecordQueryDTO();
        query.setAnswerStatus(answerStatus);
        query.setTopicId(topicId);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        return ApiResponse.success(qaRecordService.pageRecords(query));
    }
}
