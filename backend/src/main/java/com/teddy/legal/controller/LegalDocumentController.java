package com.teddy.legal.controller;

import com.teddy.legal.dto.LegalDocumentCreateRequest;
import com.teddy.legal.dto.LegalDocumentQueryRequest;
import com.teddy.legal.service.LegalDocumentService;
import com.teddy.legal.vo.ApiResponse;
import com.teddy.legal.vo.LegalDocumentDetailVO;
import com.teddy.legal.vo.LegalDocumentSummaryVO;
import com.teddy.legal.vo.PageResult;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/legal-docs")
public class LegalDocumentController {

    @Resource
    private LegalDocumentService legalDocumentService;

    @GetMapping
    public ApiResponse<PageResult<LegalDocumentSummaryVO>> page(@Valid LegalDocumentQueryRequest request) {
        return ApiResponse.success(legalDocumentService.page(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<LegalDocumentDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.success(legalDocumentService.getById(id));
    }

    @PostMapping
    public ApiResponse<LegalDocumentDetailVO> create(@Valid @RequestBody LegalDocumentCreateRequest request) {
        return ApiResponse.success(legalDocumentService.create(request));
    }
}
