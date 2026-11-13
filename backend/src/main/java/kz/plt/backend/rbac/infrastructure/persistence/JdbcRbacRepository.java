package kz.plt.backend.rbac.infrastructure.persistence;


import kz.plt.backend.rbac.domain.model.Role;
import kz.plt.backend.rbac.api.dto.ModuleDto;
import kz.plt.backend.rbac.api.dto.RoleDtos;
import kz.plt.backend.rbac.domain.port.RbacRepository;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Repository
class JdbcRbacRepository implements RbacRepository{
    private final JdbcTemplate jdbc;
    JdbcRbacRepository(JdbcTemplate jdbc){
        this.jdbc = jdbc;
    }

    @Override
    public List<ModuleDto> findAllModules(){
        String sql = "SELECT id, slug, title FROM modules ORDER BY id asc";
        return jdbc.query(sql, (rs, rowNum) -> new ModuleDto(
                rs.getLong("id"),
                rs.getString("slug"),
                rs.getString("title")
        ));
    }

    @Override
    public List<RoleDtos.RoleResponse> findAllRolesWithPermissions(){
        String sql = """
                SELECT r.id AS role_id, r.name AS role_name, r.priority, r.is_system,
                m.id AS module_id, m.slug AS module_slug,
                COALESCE(rp.permissions_mask, 0) AS mask
                FROM roles r 
                CROSS JOIN modules m 
                LEFT JOIN role_permissions rp ON r.id = rp.role_id AND m.id = rp.module_id
                ORDER BY r.priority DESC, r.id ASC, m.id ASC    
                """;
        return jdbc.query(sql, rs -> {
           Map<Long, RoleBuilder> map= new LinkedHashMap<>();
           while (rs.next()){
               Long roleId = rs.getLong("role_id");
               RoleBuilder builder = map.computeIfAbsent(roleId, id -> {
                   try {
                       return new RoleBuilder(
                               roleId,
                               rs.getString("role_name"),
                               rs.getInt("priority"),
                               rs.getBoolean("is_system")
                       );
                   } catch (SQLException e ){
                       throw new RuntimeException(e);
                   }
               });
               builder.permissions.add(new RoleDtos.ModulePermissionDto(
                       rs.getLong("module_id"),
                       rs.getString("module_slug"),
                       rs.getLong("mask")
               ));
           }
           return map.values().stream().map(RoleBuilder::build).toList();
        });
    }

    @Override
    public Optional<Role> findRoleById(Long id){
        String sql = "SELECT id,name, priority, is_system FROM roles WHERE id = ?";
        List<Role> list = jdbc.query(sql, (rs, rowNum) -> new Role(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getInt("priority"),
                rs.getBoolean("is_system")
        ), id);
        return list.stream().findFirst();
    }

    @Override
    public boolean existsRoleByName(String name){
        String sql = "SELECT COUNT(1) FROM roles WHERE name = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class , name);
        return count != null && count > 0;
    }

    @Override
    public Long createRole(String name, int priority){
        String sql = "INSERT INTO roles (name, priority, is_system) VALUES(?, ? , FALSE)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql , Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setInt(2, priority);
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    @Override
    public void updateRole(Long id, String name, int priority){
        String sql = "UPDATE roles SET NAME = ?, priority = ? WHERE ID = ?";
        jdbc.update(sql, name, priority, id);
    }

    @Override
    public void deleteRole(Long id) {
        String sql = "DELETE FROM roles where id = ?";
        jdbc.update(sql, id);
    }

    @Override
    public Optional<Long> findModuleIdBySlug(String slug){
        String sql = "SELECT * FROM modules where slug = ?";
        List<Long> list = jdbc.query(sql, (rs, rowNum) -> rs.getLong("id"), slug);
        return list.stream().findFirst();
    }

    @Override
    public void updateRolePermissions(Long roldId, List<RoleDtos.UpdatePermissionItem> permissions){
        if(permissions == null || permissions.isEmpty()){
            return;
        }
        String sql = """
                INSERT INTO role_permissions (role_id, module_id ,permissions_mask) 
                VALUES (?,?,?) AS new_val
                ON DUPLICATE KEY UPDATE permissions_mask = new_val.permissions_mask
                """;
        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                RoleDtos.UpdatePermissionItem item = permissions.get(i);
                ps.setLong(1, roldId);
                ps.setLong(2, item.moduleId());
                ps.setLong(3, item.permissionsMask() != null ? item.permissionsMask() : 0L);
            }

            @Override
            public int getBatchSize() {
                return permissions.size();
            }
        });
    }

    private static class RoleBuilder{
        final Long id;
        final String name;
        final int priority;
        final boolean isSystem;
        final List<RoleDtos.ModulePermissionDto> permissions = new ArrayList<>();

        RoleBuilder(Long id, String name, int priority, boolean isSystem){
            this.id = id;
            this.name = name;
            this.priority = priority;
            this.isSystem = isSystem;
        }

        RoleDtos.RoleResponse build(){
            return new RoleDtos.RoleResponse(id, name ,priority, isSystem, permissions);
        }
    }
}