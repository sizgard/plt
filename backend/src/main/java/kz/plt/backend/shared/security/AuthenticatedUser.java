package kz.plt.backend.shared.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public record AuthenticatedUser(
        Long id,
        String email,
        Set<String> roles,
        Map<String , Long> permissions
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final String SESSION_KEY = "AUTH_USER";

    public AuthenticatedUser {
        roles = ( roles == null || roles.isEmpty() ? Set.of() : Set.copyOf(roles));
        permissions = (permissions == null || permissions.isEmpty() ? Map.of() : Map.copyOf(permissions));
    }

    public AuthenticatedUser(Long id, String email, Set<String> roles){
        this(id, email, roles, Map.of());
    }

    public boolean hasRole(String role){
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(role));
    }

    public boolean hasPermission(String moduleSlug, Action action){
        if (action == null || moduleSlug == null){
            return false;
        }
        long mask = permissions.getOrDefault(moduleSlug, 0L);
        return (mask & action.mask()) != 0;
    }
}