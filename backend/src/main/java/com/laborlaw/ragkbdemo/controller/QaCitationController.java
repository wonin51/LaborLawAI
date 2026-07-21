package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.dto.QaCitationQueryDTO;
import com.laborlaw.ragkbdemo.service.QaCitationService;
import com.laborlaw.ragkbdemo.vo.ApiResponse;
import com.laborlaw.ragkbdemo.vo.PageVO;
import com.laborlaw.ragkbdemo.vo.QaCitationVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qa")
public class QaCitationController {

    private final QaCitationService qaCitationService;

    public QaCitationController(QaCitationService qaCitationService) {
        this.qaCitationService = qaCitationService;
    }

    /**
     * Queries QA answer citations with filters and pagination.
     */
    @GetMapping("/citations")
    public ApiResponse<PageVO<QaCitationVO>> pageCitations(
            @RequestParam(value = "claim_id", required = false) Long claimId,
            @RequestParam(value = "document_version_id", required = false) Long documentVersionId,
            @RequestParam(value = "chunk_ref_id", required = false) Long chunkRefId,
            @RequestParam(value = "page_no", required = false, defaultValue = "1") Long pageNo,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Long pageSize) {
        QaCitationQueryDTO query = new QaCitationQueryDTO();
        query.setClaimId(claimId);
        query.setDocumentVersionId(documentVersionId);
        query.setChunkRefId(chunkRefId);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        return ApiResponse.success(qaCitationService.pageCitations(query));
    }
}
