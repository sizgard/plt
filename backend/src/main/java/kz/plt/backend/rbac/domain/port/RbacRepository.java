package kz.plt.backend.rbac.domain.port;

import kz.plt.backend.rbac.api.dto.ModuleDto;
import kz.plt.backend.rbac.api.dto.RoleDtos;
import kz.plt.backend.rbac.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface RbacRepository {
    List<ModuleDto> findAllModules();
    List<RoleDtos.RoleResponse> findAllRolesWithPermissions();
    Optional<Role> findRoleById(Long id);
    boolean existsRoleByName(String name);
    Long createRole(String name, int priority);
    void updateRole(Long id, String name, int priority);
    void deleteRole(Long id);
    void updateRolePermissions(Long roleId, List<RoleDtos.UpdatePermissionItem> permissions);
    Optional<Long> findModuleIdBySlug(String slug);
}