package com.java.fastfood.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserTest {

    @Test
    void hasRole_returnsTrueOnlyForAssignedRole() {
        CurrentUser user = new CurrentUser("alice", List.of("USER", "ADMIN"));

        assertThat(user.hasRole("ADMIN")).isTrue();
        assertThat(user.hasRole("MANAGER")).isFalse();
    }
}
