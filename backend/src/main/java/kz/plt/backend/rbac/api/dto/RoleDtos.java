package kz.plt.backend.rbac.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class RoleDtos {

    public record RoleResponse(
            Long id,
            String name,
            int priority,
            boolean isSystem,
            List<ModulePermissionDto> permissions
    ){}

    public record ModulePermissionDto(
            Long moduleId,
            String moduleSlug,
            long mask
    ){}

    public record CreateRoleRequest(
            @NotBlank(message = "Название роли обязательно")
            @Size(max = 50, message = "Название роли не должно превышать 50 символов")
            String name,
            Integer priority
    ){}
    public record UpdateRoleRequest(
            @NotBlank(message = "Название роли обязательно")
            @Size(max = 50, message = "Название роли не должно превышать 50 символов")
            String name,
            Integer priority
    ){}
    public record UpdatePermissionItem(
            @NotNull(message = "moduleId обязательный")
            Long moduleId,
            @NotNull(message = "permissionsMask обязателен")
            Long permissionsMask
    ){}
}
