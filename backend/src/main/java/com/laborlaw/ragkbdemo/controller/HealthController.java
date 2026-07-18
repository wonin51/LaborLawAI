package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.vo.HealthVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public HealthVO health() {
        return new HealthVO("ok");
    }
}
