package kz.plt.backend.rbac.infrastructure.bootstrap;

import kz.plt.backend.shared.security.Action;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RbacDataInitializer implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public RbacDataInitializer(JdbcTemplate jdbc){
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args){
        initModules();
        initRoles();
        initAdminPermissions();
        initDefaultAdminUser();
    }

    private void initModules(){
        String sql = """
                INSERT INTO modules (slug, title)
                VALUES(? ,? ) as new_val
                ON DUPLICATE KEY UPDATE title = new_val.title
                """;
        jdbc.update(sql , "rbac", "Управление доступом");
        jdbc.update(sql, "users", "Пользователи");
    }
    private void initRoles(){
        String sql = """
                INSERT INTO roles(name , priority, is_system)
                VALUES(? ,? , TRUE)
                ON DUPLICATE KEY UPDATE is_system = TRUE
                """;
        jdbc.update(sql, "ADMIN", 100);
        jdbc.update(sql, "USER", 0);
    }
    private void initAdminPermissions(){
        String sql = """
                INSERT IGNORE INTO role_permissions(role_id , module_id ,permissions_mask)
                SELECT r.id , m.id , ?
                FROM roles r 
                CROSS JOIN modules m
                WHERE r.name = 'ADMIN'
                """;
        jdbc.update(sql, Action.ALL.mask());
    }

    private void initDefaultAdminUser() {
        String checkSql = "SELECT COUNT(1) FROM users WHERE email = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, "admin@plt.kz");

        if (count == null || count == 0) {
            String insertUserSql = """                                                                                                                                                                                        
                        INSERT IGNORE INTO users (name, email, password, blocked)                                                                                                                                                        
                        VALUES ('Administrator', 'admin@plt.kz', ?, FALSE)                                                                                                                                                        
                        """;
            jdbc.update(insertUserSql, passwordEncoder.encode("admin"));
        }

        String linkRoleSql = """                                                                                                                                                                                              
                    INSERT IGNORE INTO user_roles (user_id, role_id)                                                                                                                                                              
                    SELECT u.id, r.id                                                                                                                                                                                             
                    FROM users u                                                                                                                                                                                                  
                    CROSS JOIN roles r                                                                                                                                                                                            
                    WHERE u.email = 'admin@plt.kz' AND r.name = 'ADMIN'                                                                                                                                                           
                    """;
        jdbc.update(linkRoleSql);
    }
}