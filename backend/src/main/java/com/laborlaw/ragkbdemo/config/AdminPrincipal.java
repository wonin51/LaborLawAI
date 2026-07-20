package com.laborlaw.ragkbdemo.config;

import java.util.Set;

public record AdminPrincipal(String username, Set<AdminRole> roles) {

    public AdminPrincipal {
        roles = Set.copyOf(roles);
    }

    public boolean hasRole(AdminRole role) {
        return roles.contains(AdminRole.ADMIN) || roles.contains(role);
    }
}