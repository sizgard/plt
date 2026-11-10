package kz.plt.backend.auth.api.dto;

import java.util.Set;

public record AuthResponse(
        Long id,
        String email,
        Set<String> roles
) {
}
