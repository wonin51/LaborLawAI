package com.teddy.legal.service;

import com.teddy.legal.dto.LegalDocumentCreateRequest;
import com.teddy.legal.dto.LegalDocumentQueryRequest;
import com.teddy.legal.vo.LegalDocumentDetailVO;
import com.teddy.legal.vo.LegalDocumentSummaryVO;
import com.teddy.legal.vo.PageResult;

public interface LegalDocumentService {

    PageResult<LegalDocumentSummaryVO> page(LegalDocumentQueryRequest request);

    LegalDocumentDetailVO getById(Long id);

    LegalDocumentDetailVO create(LegalDocumentCreateRequest request);
}
