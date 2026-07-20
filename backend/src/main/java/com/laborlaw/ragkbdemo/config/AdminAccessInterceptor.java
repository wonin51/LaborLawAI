package com.laborlaw.ragkbdemo.config;

import com.laborlaw.ragkbdemo.exception.AdminAuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAccessInterceptor implements HandlerInterceptor {

    public static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";
    public static final String ADMIN_PRINCIPAL_ATTRIBUTE = AdminAccessInterceptor.class.getName() + ".principal";

    private final AdminAccountRegistry accountRegistry;

    public AdminAccessInterceptor(AdminAccountRegistry accountRegistry) {
        this.accountRegistry = accountRegistry;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!accountRegistry.isConfigured()) {
            throw new AdminAuthException(503, "Admin token is not configured");
        }

        String requestToken = request.getHeader(ADMIN_TOKEN_HEADER);
        AdminPrincipal principal = accountRegistry.authenticate(requestToken)
                .orElseThrow(() -> new AdminAuthException(401, "Admin authentication required"));
        request.setAttribute(ADMIN_PRINCIPAL_ATTRIBUTE, principal);

        AdminRole requiredRole = requiredRole(request.getMethod());
        if (!principal.hasRole(requiredRole)) {
            throw new AdminAuthException(403, "Admin permission denied");
        }

        return true;
    }

    private AdminRole requiredRole(String method) {
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            return AdminRole.KNOWLEDGE_READ;
        }
        if (StringUtils.hasText(method)) {
            return AdminRole.KNOWLEDGE_WRITE;
        }
        return AdminRole.ADMIN;
    }
}