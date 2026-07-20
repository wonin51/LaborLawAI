package com.laborlaw.ragkbdemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String[] LOCAL_ORIGINS = {
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:4173",
            "http://127.0.0.1:4173"
    };

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
                .allowedOrigins(LOCAL_ORIGINS)
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