package com.laborlaw.ragkbdemo.config;

import com.laborlaw.ragkbdemo.exception.AdminAuthException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminAccessInterceptorTest {

    @Test
    void rejectsMissingTokenOnReadRequests() {
        AdminAccessInterceptor interceptor = interceptorWith("alice|local-admin-token|KNOWLEDGE_READ,KNOWLEDGE_WRITE");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/knowledge/documents");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(AdminAuthException.class)
                .hasMessage("Admin authentication required");
    }

    @Test
    void acceptsReadRoleForReadRequests() {
        AdminAccessInterceptor interceptor = interceptorWith("reader|read-token|KNOWLEDGE_READ");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/knowledge/documents");
        request.addHeader("X-Admin-Token", "read-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatCode(() -> interceptor.preHandle(request, response, new Object()))
                .doesNotThrowAnyException();
        assertThat(request.getAttribute(AdminAccessInterceptor.ADMIN_PRINCIPAL_ATTRIBUTE))
                .isEqualTo(new AdminPrincipal("reader", java.util.Set.of(AdminRole.KNOWLEDGE_READ)));
    }

    @Test
    void rejectsReadRoleForWriteRequests() {
        AdminAccessInterceptor interceptor = interceptorWith("reader|read-token|KNOWLEDGE_READ");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/admin/knowledge/documents");
        request.addHeader("X-Admin-Token", "read-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> interceptor.preHandle(request, response, new Object()))
                .isInstanceOf(AdminAuthException.class)
                .hasMessage("Admin permission denied");
    }

    private AdminAccessInterceptor interceptorWith(String accounts) {
        return new AdminAccessInterceptor(new AdminAccountRegistry(accounts, ""));
    }
}