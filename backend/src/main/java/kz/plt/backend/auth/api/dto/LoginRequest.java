package kz.plt.backend.auth.api.dto;

public record LoginRequest(
        String email,
        String password
) {
}
