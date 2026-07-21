package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.service.AiPingService;
import com.laborlaw.ragkbdemo.vo.AiPingVO;
import com.laborlaw.ragkbdemo.vo.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiPingController {

    private final AiPingService aiPingService;

    public AiPingController(AiPingService aiPingService) {
        this.aiPingService = aiPingService;
    }

    @GetMapping("/ping")
    public ApiResponse<AiPingVO> ping() {
        return ApiResponse.success(aiPingService.ping());
    }
}
