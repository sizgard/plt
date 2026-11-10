package kz.plt.backend.auth.api.dto;

public record RegisterRequest(
        String name,
        String email,
        String password
) {
}
