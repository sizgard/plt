package kz.plt.backend.auth.domain.port;

import kz.plt.backend.auth.domain.model.User;

import java.util.Map;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Map<String , Long> findPermissionsByUserId(Long userId);
}
