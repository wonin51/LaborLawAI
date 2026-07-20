package com.laborlaw.ragkbdemo.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AdminAuditInterceptorTest {

    @Test
    void writesStructuredAuditLogWithoutTokenValue() {
        Logger logger = (Logger) LoggerFactory.getLogger(AdminAuditInterceptor.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            AdminAuditInterceptor interceptor = new AdminAuditInterceptor();
            MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/admin/knowledge/documents/7");
            request.addHeader("X-Admin-Token", "secret-admin-token");
            request.setRemoteAddr("127.0.0.1");
            request.setAttribute(
                    AdminAccessInterceptor.ADMIN_PRINCIPAL_ATTRIBUTE,
                    new AdminPrincipal("editor", java.util.Set.of(AdminRole.KNOWLEDGE_READ, AdminRole.KNOWLEDGE_WRITE))
            );
            MockHttpServletResponse response = new MockHttpServletResponse();
            response.setStatus(200);

            interceptor.afterCompletion(request, response, new Object(), null);

            assertThat(appender.list).hasSize(1);
            String message = appender.list.get(0).getFormattedMessage();
            assertThat(message)
                    .contains("method=DELETE")
                    .contains("path=/api/admin/knowledge/documents/7")
                    .contains("status=200")
                    .contains("remote=127.0.0.1")
                    .contains("actor=editor")
                    .contains("roles=[")
                    .doesNotContain("secret-admin-token");
        } finally {
            logger.detachAppender(appender);
        }
    }
}