package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.service.EsIndexService;
import com.laborlaw.ragkbdemo.vo.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/es")
public class EsIndexController {

    private final EsIndexService esIndexService;

    public EsIndexController(EsIndexService esIndexService) {
        this.esIndexService = esIndexService;
    }

    @PostMapping("/init-index")
    public ApiResponse<String> initIndex(
            @RequestParam(value = "index_name", required = false, defaultValue = EsIndexService.DEFAULT_INDEX_NAME)
            String indexName) {
        return ApiResponse.success(esIndexService.initIndex(indexName));
    }
}
