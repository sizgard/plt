package kz.plt.backend.auth.domain.service;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kz.plt.backend.auth.domain.model.User;
import kz.plt.backend.auth.domain.port.UserRepository;
import kz.plt.backend.shared.security.AuthenticatedUser;
import kz.plt.backend.shared.security.TokenVerifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
class SessionTokenVerifier implements TokenVerifier{

    private final UserRepository userRepository;

    SessionTokenVerifier(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public Optional<AuthenticatedUser> verify(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if(session == null){
            return Optional.empty();
        }
        Object userObj = session.getAttribute(AuthenticatedUser.SESSION_KEY);
        if(userObj instanceof AuthenticatedUser sessionUser){
            Optional<User> actualUserOpt = userRepository.findById(sessionUser.id());
            if(actualUserOpt.isEmpty() || actualUserOpt.get().blocked()){
                session.invalidate();
                return Optional.empty();
            }
            User actualUSer = actualUserOpt.get();
            Map<String , Long> freshPermissions = userRepository.findPermissionsByUserId(actualUSer.id());
            return Optional.of(new AuthenticatedUser(
                    actualUSer.id(),
                    actualUSer.email(),
                    actualUSer.roles(),
                    freshPermissions
            ));
        }
        return Optional.empty();
    }
}