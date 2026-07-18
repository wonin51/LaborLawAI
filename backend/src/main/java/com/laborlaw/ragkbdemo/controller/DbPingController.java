package com.laborlaw.ragkbdemo.controller;

import com.laborlaw.ragkbdemo.service.DbPingService;
import com.laborlaw.ragkbdemo.vo.ApiResponse;
import com.laborlaw.ragkbdemo.vo.DbPingVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/db")
public class DbPingController {

    private final DbPingService dbPingService;

    public DbPingController(DbPingService dbPingService) {
        this.dbPingService = dbPingService;
    }

    @GetMapping("/ping")
    public ApiResponse<DbPingVO> ping() {
        try {
            return ApiResponse.success(dbPingService.ping());
        } catch (Exception ex) {
            return ApiResponse.error(500, ex.getMessage(), new DbPingVO(null, "error"));
        }
    }
}
