package kz.plt.backend.rbac.domain.service;


import kz.plt.backend.rbac.api.dto.ModuleDto;
import kz.plt.backend.rbac.api.dto.RoleDtos;
import kz.plt.backend.rbac.domain.model.Role;
import kz.plt.backend.rbac.domain.port.RbacRepository;
import kz.plt.backend.shared.error.ConflictException;
import kz.plt.backend.shared.error.NotFoundException;
import kz.plt.backend.shared.security.Action;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RbacService {
    private final RbacRepository rbacRepository;

    public RbacService(RbacRepository rbacRepository){
        this.rbacRepository = rbacRepository;}

    @Transactional(readOnly = true)
    public List<ModuleDto> getAllModules(){
        return rbacRepository.findAllModules();
    }

    @Transactional(readOnly = true)
    public List<RoleDtos.RoleResponse> getAllRolesWithPermissions(){
        return rbacRepository.findAllRolesWithPermissions();
    }

    @Transactional
    public Role createRole(RoleDtos.CreateRoleRequest request){
        if (request == null){
            throw new ConflictException("Тело запроса не может быть пустым");
        }
        String name = request.name() != null ? request.name().trim() : "";
        if(name.isEmpty()){
            throw new ConflictException("Название роли не может быть пустым");
        }
        if(rbacRepository.existsRoleByName(name)){
            throw new ConflictException("Роль с таким названием уже существует");
        }
        int priority = request.priority() != null ? request.priority() : 0;
        try {
            Long id = rbacRepository.createRole(name, priority);
            return new Role(id, name ,priority, false);
        } catch (DuplicateKeyException ex){
            throw new ConflictException("Роль с таким названием уже существует");
        }
    }

    @Transactional
    public void updateRole(Long roleID, RoleDtos.UpdateRoleRequest request){
        if(request == null){
            throw new ConflictException("Тело запроса не может быть пустым");
        }
        Role role = rbacRepository.findRoleById(roleID)
                .orElseThrow(() -> new NotFoundException("Роль не найдена"));
        String newName = request.name() != null ? request.name().trim() : role.name();
        if (newName.isEmpty()){
            throw new ConflictException("Название роли пустое");
        }
        if(role.isSystem() && !newName.equals(role.name())){
            throw new ConflictException("Нельзя изменять имя системной роли");
        }
        if (!newName.equalsIgnoreCase(role.name()) && rbacRepository.existsRoleByName(newName)){
            throw new ConflictException("Роль с таким название уже существует");
        }
        int newPriority = request.priority() != null ? request.priority() : role.priority();
        try {
            rbacRepository.updateRole(roleID, newName, newPriority);
        } catch (DuplicateKeyException e){
            throw new ConflictException("Роль с таким названием уже существует");
        }
    }
    @Transactional
    public void updateRolePermissions(Long roleId, List<RoleDtos.UpdatePermissionItem> items){
        Role role = rbacRepository.findRoleById(roleId)
                .orElseThrow(() -> new NotFoundException("Роль не найдена"));
        if (items == null || items.isEmpty()) {
            return;
        }

        Set<Long> existingModuleIds = rbacRepository.findAllModules().stream()
                .map(ModuleDto::id)
                .collect(Collectors.toSet());

        Set<Long> seenModuleIds = new HashSet<>();
        long allowedMask = Action.ALL.mask();
        for(RoleDtos.UpdatePermissionItem item : items){
            if(item == null || item.moduleId() == null){
                throw new ConflictException("Элемент прав и moduleId не могут быть пустыми");
            }
            if(!seenModuleIds.add(item.moduleId())){
                throw new ConflictException("Дублирующийся moduleId в списке: " + item.moduleId());
            }
            if (!existingModuleIds.contains(item.moduleId())){
                throw new NotFoundException("Модуль не найден: " + item.moduleId());
            }
            long mask = item.permissionsMask() != null ? item.permissionsMask() : 0L;
            if ((mask & ~allowedMask) != 0){
                throw new ConflictException("Недопустимая битовая маска прав: " + mask);
            }
        }

        if(role.isSystem() && "ADMIN".equalsIgnoreCase(role.name())){
            Long rbacModuleId = rbacRepository.findModuleIdBySlug("rbac").orElse(null);
            if(rbacModuleId != null){
                long requiredMask = Action.READ.mask() | Action.UPDATE.mask();
                for(RoleDtos.UpdatePermissionItem item : items){
                    if(rbacModuleId.equals(item.moduleId())){
                    long mask = item.permissionsMask() != null ? item.permissionsMask() : 0L;
                    if ((mask & requiredMask) != requiredMask){
                        throw new ConflictException("Нельзя снимать права READ/UPDATE на модуль rbac у роли ADMIN");
                    }
                }}
            }
        }
        rbacRepository.updateRolePermissions(roleId, items);
    }

    @Transactional
    public void deleteRole(Long roleId){
        Role role = rbacRepository.findRoleById(roleId)
                .orElseThrow(() -> new NotFoundException("Роль не найдна"));
        if(role.isSystem()){
            throw new ConflictException("Нельзя удалять системную роль");
        }
        rbacRepository.deleteRole(roleId);
    }
}
