package com.laborlaw.ragkbdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.laborlaw.ragkbdemo.mapper")
@SpringBootApplication
public class RagKbDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagKbDemoApplication.class, args);
    }
}
