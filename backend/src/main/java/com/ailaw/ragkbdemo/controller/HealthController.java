package com.ailaw.ragkbdemo.controller;

import com.ailaw.ragkbdemo.vo.HealthVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Basic health check endpoint used by deployment, smoke tests, and later frontend integration.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public HealthVO health() {
        return new HealthVO("ok");
    }
}
