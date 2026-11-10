package kz.plt.backend.auth.domain.service;

import jakarta.servlet.http.HttpSession;
import kz.plt.backend.auth.domain.exception.InvalidCredentialsException;
import kz.plt.backend.auth.domain.exception.UserAlreadyExistsException;
import kz.plt.backend.auth.domain.model.User;
import kz.plt.backend.auth.domain.port.UserRepository;
import kz.plt.backend.shared.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    public AuthService(UserRepository userRepository, PasswordHasher passwordHasher){
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }
    public AuthenticatedUser register(String name, String email, String password, HttpSession session){
        if(userRepository.existsByEmail(email)){
            throw new UserAlreadyExistsException(email);
        }
        String passwordHash = passwordHasher.hash(password);
        User newUser = User.createNew(name, email, passwordHash, Set.of("USER"));
        User savedUser = userRepository.save(newUser);
        return createSessionForUser(savedUser, session);
    }
    public AuthenticatedUser login(String email, String password, HttpSession session){
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if(user.blocked() || !passwordHasher.matches(password, user.password())){
            throw new InvalidCredentialsException();
        }
        return createSessionForUser(user, session);
    }
    public void logout(HttpSession session){
        if(session != null){
            session.invalidate();
        }
    }
    private AuthenticatedUser createSessionForUser(User user, HttpSession session){
        String primaryRole = user.roles().isEmpty() ? "USER" : user.roles().iterator().next();
        AuthenticatedUser authUser = new AuthenticatedUser(user.id(), user.email(), primaryRole);
        session.setAttribute(SessionTokenVerifier.AUTH_USER_ATTR, authUser);
        return authUser;
    }
}
