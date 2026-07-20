package com.laborlaw.ragkbdemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class AdminAccountRegistry {

    private final List<ConfiguredAccount> accounts;

    public AdminAccountRegistry(
            @Value("${app.admin.accounts:}") String accountConfig,
            @Value("${app.admin.token:}") String legacyToken) {
        this.accounts = parseAccounts(accountConfig, legacyToken);
    }

    public Optional<AdminPrincipal> authenticate(String requestToken) {
        if (!StringUtils.hasText(requestToken)) {
            return Optional.empty();
        }

        return accounts.stream()
                .filter(account -> sameToken(account.token(), requestToken))
                .map(ConfiguredAccount::principal)
                .findFirst();
    }

    public boolean isConfigured() {
        return !accounts.isEmpty();
    }

    private List<ConfiguredAccount> parseAccounts(String accountConfig, String legacyToken) {
        List<ConfiguredAccount> parsed = new ArrayList<>();
        if (StringUtils.hasText(accountConfig)) {
            String[] entries = accountConfig.split(";");
            for (int index = 0; index < entries.length; index++) {
                String entry = entries[index].trim();
                if (!StringUtils.hasText(entry)) {
                    continue;
                }

                String[] fields = entry.split("\\|", -1);
                if (fields.length != 3 || !StringUtils.hasText(fields[0]) || !StringUtils.hasText(fields[1])) {
                    throw new IllegalStateException("Invalid admin account configuration at entry " + (index + 1));
                }

                Set<AdminRole> roles = parseRoles(fields[2], index + 1);
                parsed.add(new ConfiguredAccount(
                        fields[0].trim(),
                        fields[1].trim(),
                        new AdminPrincipal(fields[0].trim(), roles)
                ));
            }
        }

        if (parsed.isEmpty() && StringUtils.hasText(legacyToken)) {
            parsed.add(new ConfiguredAccount(
                    "legacy-admin",
                    legacyToken.trim(),
                    new AdminPrincipal("legacy-admin", Set.of(AdminRole.ADMIN))
            ));
        }

        return List.copyOf(parsed);
    }

    private Set<AdminRole> parseRoles(String rawRoles, int entryNumber) {
        if (!StringUtils.hasText(rawRoles)) {
            throw new IllegalStateException("Admin roles are required at entry " + entryNumber);
        }

        EnumSet<AdminRole> roles = EnumSet.noneOf(AdminRole.class);
        for (String rawRole : rawRoles.split(",")) {
            try {
                roles.add(AdminRole.valueOf(rawRole.trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException("Unknown admin role at entry " + entryNumber, ex);
            }
        }
        return Set.copyOf(roles);
    }

    private boolean sameToken(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }

    private record ConfiguredAccount(String username, String token, AdminPrincipal principal) {
    }
}