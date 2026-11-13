package kz.plt.backend.rbac.api;

import jakarta.validation.Valid;
import kz.plt.backend.rbac.api.dto.ModuleDto;
import kz.plt.backend.rbac.api.dto.RoleDtos;
import kz.plt.backend.rbac.domain.model.Role;
import kz.plt.backend.rbac.domain.service.RbacService;
import kz.plt.backend.shared.security.Action;
import kz.plt.backend.shared.security.RequirePermission;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/rbac")
public class RbacController {
    private final RbacService rbacService;

    public RbacController(RbacService rbacService){
        this.rbacService = rbacService;
    }

    @GetMapping("/modules")
    @RequirePermission(module = "rbac" , action = Action.READ)
    public List<ModuleDto> getModules(){
        return rbacService.getAllModules();
    }

    @GetMapping("/roles")
    @RequirePermission(module = "rbac", action = Action.READ)
    public List<RoleDtos.RoleResponse> getRoles(){
        return rbacService.getAllRolesWithPermissions();
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(module = "rbac", action = Action.CREATE)
    public Role createRole(@Valid @RequestBody RoleDtos.CreateRoleRequest request){
        return rbacService.createRole(request);
    }

    @PutMapping("/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(module = "rbac", action = Action.UPDATE)
    public void updateRole(@PathVariable Long roleId, @Valid @RequestBody RoleDtos.UpdateRoleRequest request){
        rbacService.updateRole(roleId, request);
    }
    @PutMapping("/roles/{roleId}/permissions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(module = "rbac" , action = Action.UPDATE)
    public void updateRolePermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody List<RoleDtos.UpdatePermissionItem> items
    ){
        rbacService.updateRolePermissions(roleId, items);
    }

    @DeleteMapping("/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(module = "rbac", action = Action.DELETE)
    public void deleteRoel(@PathVariable Long roleId){
        rbacService.deleteRole(roleId);
    }
}
