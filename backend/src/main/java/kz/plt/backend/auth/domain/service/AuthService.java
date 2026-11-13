package kz.plt.backend.auth.domain.service;

import kz.plt.backend.auth.domain.exception.InvalidCredentialsException;
import kz.plt.backend.auth.domain.exception.UserAlreadyExistsException;
import kz.plt.backend.auth.domain.model.User;
import kz.plt.backend.auth.domain.port.UserRepository;
import kz.plt.backend.shared.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public AuthService(UserRepository userRepository, PasswordHasher passwordHasher){
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public AuthenticatedUser register(String name, String email, String password) {
        if(userRepository.existsByEmail(email)){
            throw new UserAlreadyExistsException(email);
        }
        String passwordHash = passwordHasher.hash(password);
        User newUser = User.createNew(name, email, passwordHash, Set.of("USER"));
        User savedUser = userRepository.save(newUser);
        return createSessionForUser(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser login(String email, String password){
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if(user.blocked() || !passwordHasher.matches(password, user.password())){
            throw new InvalidCredentialsException();
        }
        return createSessionForUser(user);
    }
    private AuthenticatedUser createSessionForUser(User user){
        Map<String, Long> permissions = userRepository.findPermissionsByUserId(user.id());
        return new AuthenticatedUser(user.id(), user.email(), user.roles(), permissions);
    }
}
