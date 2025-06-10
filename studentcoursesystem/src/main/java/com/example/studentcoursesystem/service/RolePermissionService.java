package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.Permission;
import com.example.studentcoursesystem.entity.RolePermission;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;

public interface RolePermissionService {
    /**
     * 获取角色的所有权限关联
     * @param roleId 角色ID
     * @return 角色权限关联列表
     */
    List<RolePermission> getRolePermissions(@NonNull Long roleId);

    /**
     * 为角色添加权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     * @return 是否添加成功
     */
    boolean addRolePermission(@NonNull Long roleId, @NonNull Long permissionId);

    /**
     * 获取角色对应的权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByRoleId(@NonNull Long roleId);

    boolean removePermissionFromRole(Long roleId, Long permissionId);

    /**
     * 批量获取多个角色对应的权限列表
     * @param roleIds 角色ID集合
     * @return 权限列表
     */
    List<Permission> getPermissionsByRoleIds(Collection<Long> roleIds);
}