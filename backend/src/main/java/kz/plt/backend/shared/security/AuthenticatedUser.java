package kz.plt.backend.shared.security;

import java.io.Serial;
import java.io.Serializable;

public record AuthenticatedUser(
        Long id,
        String email,
        String role
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}