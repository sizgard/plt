package kz.plt.backend.shared.security;


import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public interface TokenVerifier {
    Optional<AuthenticatedUser> verify(HttpServletRequest request);
}

