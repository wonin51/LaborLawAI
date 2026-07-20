package com.laborlaw.ragkbdemo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuditInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminAuditInterceptor.class);

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {
        Object principalAttribute = request.getAttribute(AdminAccessInterceptor.ADMIN_PRINCIPAL_ATTRIBUTE);
        AdminPrincipal principal = principalAttribute instanceof AdminPrincipal value ? value : null;

        LOGGER.info(
                "Admin audit method={} path={} status={} remote={} actor={} roles={} outcome={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                request.getRemoteAddr(),
                principal == null ? "anonymous" : principal.username(),
                principal == null ? "[]" : principal.roles(),
                exception == null && response.getStatus() < 400 ? "success" : "error"
        );
    }
}