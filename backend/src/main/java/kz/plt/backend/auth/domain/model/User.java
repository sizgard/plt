package kz.plt.backend.auth.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.Collections;

public record User(
        Long id,
        String name,
        String email,
        String password,
        boolean blocked,
        Set<String> roles,
        Instant createdAt) {
    public static User createNew(String name, String email, String passwordHash, Set<String> roles) {
        return new User(null, name, email, passwordHash, false, roles, Instant.now());
    }

    public User {
        roles = roles == null ? Collections.emptySet() : Collections.unmodifiableSet(roles);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
