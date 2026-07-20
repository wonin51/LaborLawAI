package com.teddy.legal.controller;

import com.teddy.legal.service.DbPingService;
import com.teddy.legal.vo.ApiResponse;
import com.teddy.legal.vo.DbPingVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DbPingController {

    @Resource
    private DbPingService dbPingService;

    @GetMapping("/db/ping")
    public ApiResponse<DbPingVO> ping() {
        return ApiResponse.success(dbPingService.ping());
    }
}
