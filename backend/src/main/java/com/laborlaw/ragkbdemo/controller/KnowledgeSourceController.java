package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.dto.KbDocumentQueryDTO;
import com.laborlaw.ragkbdemo.dto.KbChunkRefQueryDTO;
import com.laborlaw.ragkbdemo.service.KnowledgeSourceService;
import com.laborlaw.ragkbdemo.vo.ApiResponse;
import com.laborlaw.ragkbdemo.vo.KbDocumentQueryVO;
import com.laborlaw.ragkbdemo.vo.KbChunkRefVO;
import com.laborlaw.ragkbdemo.vo.PageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeSourceController {

    private final KnowledgeSourceService knowledgeSourceService;

    public KnowledgeSourceController(KnowledgeSourceService knowledgeSourceService) {
        this.knowledgeSourceService = knowledgeSourceService;
    }

    /**
     * Queries knowledge documents with filters and pagination.
     */
    @GetMapping("/documents")
    public ApiResponse<PageVO<KbDocumentQueryVO>> pageDocuments(
            @RequestParam(required = false) String title,
            @RequestParam(value = "document_type", required = false) String documentType,
            @RequestParam(required = false) String status,
            @RequestParam(value = "page_no", required = false, defaultValue = "1") Long pageNo,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Long pageSize) {
        KbDocumentQueryDTO query = new KbDocumentQueryDTO();
        query.setTitle(title);
        query.setDocumentType(documentType);
        query.setStatus(status);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        return ApiResponse.success(knowledgeSourceService.pageDocuments(query));
    }

    /**
     * Queries knowledge chunk refs with filters and pagination.
     */
    @GetMapping("/chunk-refs")
    public ApiResponse<PageVO<KbChunkRefVO>> pageChunkRefs(
            @RequestParam(value = "document_version_id", required = false) Long documentVersionId,
            @RequestParam(required = false) String status,
            @RequestParam(value = "chunk_key", required = false) String chunkKey,
            @RequestParam(value = "section_path", required = false) String sectionPath,
            @RequestParam(value = "page_no", required = false, defaultValue = "1") Long pageNo,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Long pageSize) {
        KbChunkRefQueryDTO query = new KbChunkRefQueryDTO();
        query.setDocumentVersionId(documentVersionId);
        query.setStatus(status);
        query.setChunkKey(chunkKey);
        query.setSectionPath(sectionPath);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        return ApiResponse.success(knowledgeSourceService.pageChunkRefs(query));
    }
}
