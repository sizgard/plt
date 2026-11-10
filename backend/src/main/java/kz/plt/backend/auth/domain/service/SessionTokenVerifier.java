package kz.plt.backend.auth.domain.service;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kz.plt.backend.shared.security.AuthenticatedUser;
import kz.plt.backend.shared.security.TokenVerifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class SessionTokenVerifier implements TokenVerifier{
    @Override
    public Optional<AuthenticatedUser> verify(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if(session == null){
            return Optional.empty();
        }
        Object user = session.getAttribute(AuthenticatedUser.SESSION_KEY);
        if(user instanceof AuthenticatedUser authenticatedUser){
            return Optional.of(authenticatedUser);
        }
        return Optional.empty();
    }
}