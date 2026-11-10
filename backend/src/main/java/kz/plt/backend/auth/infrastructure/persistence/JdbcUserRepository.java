package kz.plt.backend.auth.infrastructure.persistence;

import kz.plt.backend.auth.domain.model.User;
import kz.plt.backend.auth.domain.port.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;

@Repository
class JdbcUserRepository implements UserRepository{
    private final JdbcTemplate jdbc;
    JdbcUserRepository(JdbcTemplate jdbc){
        this.jdbc = jdbc;
    }
    @Override
    public boolean existsByEmail(String email){
        String sql = "SELECT COUNT(1) FROM users WHERE email = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
    @Override
    public Optional<User> findById(Long id) {
        String sql = """
                SELECT u.id, u.name, u.email, u.password, u.blocked, u.created_at, r.name AS role_name 
                FROM users u
                LEFT JOIN user_roles ur ON u.id = ur.user_id
                LEFT JOIN roles r ON ur.role_id = r.id
                WHERE u.id = ?
                """;
        return queryUsers(sql, id).stream().findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = """
                SELECT u.id, u.name, u.email, u.password, u.blocked, u.created_at, r.name AS role_name
                FROM users u
                LEFT JOIN user_roles ur ON u.id = ur.user_id
                LEFT JOIN roles r ON ur.role_id = r.id
                WHERE u.email = ?
                """;
        return queryUsers(sql, email).stream().findFirst();
    }

    @Override
    public User save(User user) {
        if (user.id() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            String sql = "INSERT INTO users (name, email, password, blocked, created_at) VALUES (?, ?, ?, ?, ?)";
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.name());
                ps.setString(2, user.email());
                ps.setString(3, user.password());
                ps.setBoolean(4, user.blocked());
                ps.setTimestamp(5, Timestamp.from(user.createdAt()));
                return ps;
            }, keyHolder);

            Long newId = Objects.requireNonNull(keyHolder.getKey()).longValue();

            if (!user.roles().isEmpty()) {
                String rolesSql = """
                        INSERT INTO user_roles (user_id, role_id)
                        SELECT ?, id FROM roles WHERE name = ?
                        """;
                for (String role : user.roles()) {
                    jdbc.update(rolesSql, newId, role);
                }
            }
            return new User(newId, user.name(), user.email(), user.password(), user.blocked(), user.roles(), user.createdAt());
        }

        String updateSql = "UPDATE users SET name = ?, password = ?, blocked = ? WHERE id = ?";
        jdbc.update(updateSql, user.name(), user.password(), user.blocked(), user.id());
        return user;
    }

    private List<User> queryUsers(String sql, Object... params) {
        return jdbc.query(sql, rs -> {
            Map<Long, UserBuilder> map = new LinkedHashMap<>();
            while (rs.next()) {
                Long id = rs.getLong("id");
                UserBuilder builder = map.computeIfAbsent(id, k -> {
                    try {
                        return new UserBuilder(
                                id,
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("password"),
                                rs.getBoolean("blocked"),
                                rs.getTimestamp("created_at").toInstant()
                        );
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                String role = rs.getString("role_name");
                if (role != null) {
                    builder.roles.add(role);
                }
            }
            return map.values().stream().map(UserBuilder::build).toList();
        }, params);
    }

    private static class UserBuilder {
        final Long id;
        final String name;
        final String email;
        final String password;
        final boolean blocked;
        final java.time.Instant createdAt;
        final Set<String> roles = new HashSet<>();

        UserBuilder(Long id,String name,String email, String password, boolean blocked, java.time.Instant createdAt){
            this.id = id;
            this.name = name;
            this.email = email;
            this.password = password;
            this.blocked = blocked;
            this.createdAt = createdAt;
        }
        User build(){
            return new User(id,name, email, password, blocked, roles, createdAt);
        }
    }
}

