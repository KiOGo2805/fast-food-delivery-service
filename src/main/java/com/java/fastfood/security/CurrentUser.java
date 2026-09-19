package com.java.fastfood.security;

import java.util.List;

public record CurrentUser(String username, List<String> roles) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
