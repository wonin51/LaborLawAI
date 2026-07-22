package com.laborlaw.ragkbdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminAccessInterceptor adminAccessInterceptor;
    private final AdminAuditInterceptor adminAuditInterceptor;

    public WebMvcConfig(
            AdminAccessInterceptor adminAccessInterceptor,
            AdminAuditInterceptor adminAuditInterceptor) {
        this.adminAccessInterceptor = adminAccessInterceptor;
        this.adminAuditInterceptor = adminAuditInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("Allow")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuditInterceptor)
                .addPathPatterns("/api/admin/**");
        registry.addInterceptor(adminAccessInterceptor)
                .addPathPatterns("/api/admin/**");
    }
}