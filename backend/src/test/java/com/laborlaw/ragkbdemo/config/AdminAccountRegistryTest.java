package com.laborlaw.ragkbdemo.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminAccountRegistryTest {

    @Test
    void parsesAccountsAndMatchesRolesWithoutExposingTokens() {
        AdminAccountRegistry registry = new AdminAccountRegistry(
                "alice|read-token|KNOWLEDGE_READ;editor|write-token|KNOWLEDGE_READ,KNOWLEDGE_WRITE",
                ""
        );

        AdminPrincipal principal = registry.authenticate("write-token").orElseThrow();

        assertThat(principal.username()).isEqualTo("editor");
        assertThat(principal.hasRole(AdminRole.KNOWLEDGE_READ)).isTrue();
        assertThat(principal.hasRole(AdminRole.KNOWLEDGE_WRITE)).isTrue();
        assertThat(principal.toString()).doesNotContain("write-token");
    }

    @Test
    void keepsLegacyTokenAsFullAdminForBackwardCompatibility() {
        AdminAccountRegistry registry = new AdminAccountRegistry("", "legacy-token");

        AdminPrincipal principal = registry.authenticate("legacy-token").orElseThrow();

        assertThat(principal.username()).isEqualTo("legacy-admin");
        assertThat(principal.hasRole(AdminRole.ADMIN)).isTrue();
    }
}