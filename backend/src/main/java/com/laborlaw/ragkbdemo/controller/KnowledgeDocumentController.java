package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.dto.KnowledgeDocumentCreateDTO;
import com.laborlaw.ragkbdemo.dto.KnowledgeDocumentPageQueryDTO;
import com.laborlaw.ragkbdemo.dto.KnowledgeDocumentStatusUpdateDTO;
import com.laborlaw.ragkbdemo.dto.KnowledgeDocumentUpdateDTO;
import com.laborlaw.ragkbdemo.service.KnowledgeDocumentService;
import com.laborlaw.ragkbdemo.vo.ApiResponse;
import com.laborlaw.ragkbdemo.vo.KnowledgeDocumentVO;
import com.laborlaw.ragkbdemo.vo.OptionsVO;
import com.laborlaw.ragkbdemo.vo.PageVO;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/admin/knowledge/documents")
public class KnowledgeDocumentController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeDocumentController.class);
    private static final String NOT_FOUND_MESSAGE = "Knowledge document not found";
    private static final List<String> COLLECTION_METHODS = List.of("POST", "GET", "OPTIONS");
    private static final List<String> ITEM_METHODS = List.of("GET", "PUT", "DELETE", "HEAD", "PATCH", "OPTIONS");

    private final KnowledgeDocumentService knowledgeDocumentService;

    public KnowledgeDocumentController(KnowledgeDocumentService knowledgeDocumentService) {
        this.knowledgeDocumentService = knowledgeDocumentService;
    }

    /**
     * Creates knowledge document metadata.
     * :param dto: Request body containing required and optional document metadata.
     * :return: Unified response containing the created document metadata.
     */
    @PostMapping
    public ApiResponse<KnowledgeDocumentVO> create(@Valid @RequestBody KnowledgeDocumentCreateDTO dto) {
        log.info("Admin create knowledge document: title={}, documentType={}, status={}", dto.getTitle(), dto.getDocumentType(), dto.getStatus());
        return ApiResponse.success(knowledgeDocumentService.create(dto));
    }

    /**
     * Queries knowledge document metadata with filters and pagination.
     * :param title: Optional title keyword.
     * :param documentType: Optional exact document type.
     * :param status: Optional exact document status.
     * :param issuingAuthority: Optional issuing authority keyword.
     * :param jurisdictionCode: Optional exact jurisdiction code.
     * :param pageNo: Page number starting from 1.
     * :param pageSize: Page size capped by the service.
     * :return: Unified response containing paged document metadata.
     */
    @GetMapping
    public ApiResponse<PageVO<KnowledgeDocumentVO>> page(
            @RequestParam(required = false) String title,
            @RequestParam(value = "document_type", required = false) String documentType,
            @RequestParam(required = false) String status,
            @RequestParam(value = "issuing_authority", required = false) String issuingAuthority,
            @RequestParam(value = "jurisdiction_code", required = false) String jurisdictionCode,
            @RequestParam(required = false, defaultValue = "1") Long pageNo,
            @RequestParam(required = false, defaultValue = "10") Long pageSize) {
        KnowledgeDocumentPageQueryDTO query = new KnowledgeDocumentPageQueryDTO();
        query.setTitle(title);
        query.setDocumentType(documentType);
        query.setStatus(status);
        query.setIssuingAuthority(issuingAuthority);
        query.setJurisdictionCode(jurisdictionCode);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        return ApiResponse.success(knowledgeDocumentService.page(query));
    }

    /**
     * Gets knowledge document detail by primary key.
     * :param id: Document primary key.
     * :return: Unified response containing document detail or not-found information.
     */
    @GetMapping("/{id}")
    public ApiResponse<KnowledgeDocumentVO> getById(@PathVariable Long id) {
        KnowledgeDocumentVO document = knowledgeDocumentService.getById(id);
        if (document == null) {
            return ApiResponse.error(404, NOT_FOUND_MESSAGE, null);
        }
        return ApiResponse.success(document);
    }

    /**
     * Updates editable metadata fields for a knowledge document.
     * :param id: Document primary key.
     * :param dto: Request body containing new metadata values.
     * :return: Unified response containing updated document metadata or not-found information.
     */
    @PutMapping("/{id}")
    public ApiResponse<KnowledgeDocumentVO> update(
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeDocumentUpdateDTO dto) {
        log.info("Admin update knowledge document: id={}, title={}, documentType={}, status={}", id, dto.getTitle(), dto.getDocumentType(), dto.getStatus());
        KnowledgeDocumentVO document = knowledgeDocumentService.update(id, dto);
        if (document == null) {
            return ApiResponse.error(404, NOT_FOUND_MESSAGE, null);
        }
        return ApiResponse.success(document);
    }

    /**
     * Deletes knowledge document metadata.
     * :param id: Document primary key.
     * :return: Unified response indicating whether deletion succeeded.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        log.info("Admin delete knowledge document: id={}", id);
        Boolean deleted = knowledgeDocumentService.deleteById(id);
        if (deleted == null) {
            return ApiResponse.error(404, NOT_FOUND_MESSAGE, null);
        }
        return ApiResponse.success(true);
    }

    /**
     * Checks whether a knowledge document exists without returning a response body.
     * :param id: Document primary key.
     * :return: HTTP 200 when the document exists, otherwise HTTP 404.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> head(@PathVariable Long id) {
        return knowledgeDocumentService.exists(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Returns HTTP methods supported by the document collection resource.
     * :return: Response with Allow header and method list body.
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<ApiResponse<OptionsVO>> optionsCollection() {
        return ResponseEntity.ok()
                .header(HttpHeaders.ALLOW, String.join(",", COLLECTION_METHODS))
                .body(ApiResponse.success(new OptionsVO(COLLECTION_METHODS)));
    }

    /**
     * Returns HTTP methods supported by a single document resource.
     * :param id: Document primary key used to match the item resource path.
     * :return: Response with Allow header and method list body.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.OPTIONS)
    public ResponseEntity<ApiResponse<OptionsVO>> optionsItem(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header(HttpHeaders.ALLOW, String.join(",", ITEM_METHODS))
                .body(ApiResponse.success(new OptionsVO(ITEM_METHODS)));
    }

    /**
     * Updates only the status field for a knowledge document.
     * :param id: Document primary key.
     * :param dto: Request body containing the new status.
     * :return: Unified response containing updated document metadata or not-found information.
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<KnowledgeDocumentVO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeDocumentStatusUpdateDTO dto) {
        log.info("Admin update knowledge document status: id={}, status={}", id, dto.getStatus());
        KnowledgeDocumentVO document = knowledgeDocumentService.updateStatus(id, dto);
        if (document == null) {
            return ApiResponse.error(404, NOT_FOUND_MESSAGE, null);
        }
        return ApiResponse.success(document);
    }
}
