package com.example.legalassistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class LegalContractAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(LegalContractAssistantApplication.class, args);
    }
}
