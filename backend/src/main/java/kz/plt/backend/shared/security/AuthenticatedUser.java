package kz.plt.backend.shared.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

public record AuthenticatedUser(
        Long id,
        String email,
        Set<String> roles
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final String SESSION_KEY = "AUTH_USER";

    public AuthenticatedUser {
        roles = ( roles == null || roles.isEmpty() ? Set.of() : Set.copyOf(roles));
    }

    public boolean hasRole(String role){
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(role));
    }
}