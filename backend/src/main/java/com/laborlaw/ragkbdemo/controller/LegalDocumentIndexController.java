package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.service.LegalDocumentIndexService;
import com.laborlaw.ragkbdemo.vo.ApiResponse;
import com.laborlaw.ragkbdemo.vo.LegalDocumentIndexResultVO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legal-documents")
public class LegalDocumentIndexController {

    private final LegalDocumentIndexService legalDocumentIndexService;

    public LegalDocumentIndexController(LegalDocumentIndexService legalDocumentIndexService) {
        this.legalDocumentIndexService = legalDocumentIndexService;
    }

    @PostMapping("/{id}/index")
    public ApiResponse<LegalDocumentIndexResultVO> index(@PathVariable Long id) {
        return ApiResponse.success(legalDocumentIndexService.index(id));
    }
}
