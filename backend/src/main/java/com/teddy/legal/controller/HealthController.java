package com.teddy.legal.controller;

import com.teddy.legal.vo.HealthStatusVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public HealthStatusVO health() {
        return new HealthStatusVO("ok");
    }
}
