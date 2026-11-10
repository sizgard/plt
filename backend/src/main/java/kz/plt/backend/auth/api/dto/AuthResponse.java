package kz.plt.backend.auth.api.dto;

public record AuthResponse(
        Long id,
        String email,
        String role
) {
}
