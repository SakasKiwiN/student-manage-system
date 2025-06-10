package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.Permission;
import com.example.studentcoursesystem.entity.RolePermission;
import com.example.studentcoursesystem.repository.PermissionRepository;
import com.example.studentcoursesystem.repository.RolePermissionRepository;
import com.example.studentcoursesystem.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RolePermission> getRolePermissions(@NonNull Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId);
    }

    @Override
    public boolean addRolePermission(@NonNull Long roleId, @NonNull Long permissionId) {
        try {
            // 检查关联是否已存在
            if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
                log.warn("角色权限关联已存在: roleId={}, permissionId={}", roleId, permissionId);
                return false;
            }

            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            Instant now = Instant.now();
            rolePermission.setGmtCreate(now);
            rolePermission.setGmtModified(now);

            rolePermissionRepository.save(rolePermission);
            log.info("添加角色权限关联成功: roleId={}, permissionId={}", roleId, permissionId);
            return true;
        } catch (Exception e) {
            log.error("添加角色权限关联失败: roleId={}, permissionId={}", roleId, permissionId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> getPermissionsByRoleId(@NonNull Long roleId) {
        log.debug("获取角色权限: roleId={}", roleId);

        // 1. 获取角色权限关联
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(roleId);
        if (rolePermissions.isEmpty()) {
            log.debug("角色{}没有权限", roleId);
            return Collections.emptyList();
        }

        // 2. 获取权限ID列表
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());

        // 3. 批量查询权限
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        log.debug("角色{}共有{}个权限", roleId, permissions.size());

        return permissions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> getPermissionsByRoleIds(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        log.debug("批量获取角色权限: roleIds={}", roleIds);

        // 1. 获取所有角色的权限关联
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIdIn(roleIds);
        if (rolePermissions.isEmpty()) {
            log.debug("角色{}没有权限", roleIds);
            return Collections.emptyList();
        }

        // 2. 去重获取权限ID列表
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .distinct()
                .collect(Collectors.toList());

        // 3. 批量查询权限
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        log.debug("角色{}共有{}个不重复权限", roleIds, permissions.size());

        return permissions;
    }

    @Override
    public boolean removePermissionFromRole(@NonNull Long roleId, @NonNull Long permissionId) {
        try {
            // 检查关联是否存在
            if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
                log.warn("要删除的角色权限关联不存在: roleId={}, permissionId={}", roleId, permissionId);
                return false;
            }

            // 删除关联
            rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
            log.info("删除角色权限关联成功: roleId={}, permissionId={}", roleId, permissionId);
            return true;
        } catch (Exception e) {
            log.error("删除角色权限关联失败: roleId={}, permissionId={}", roleId, permissionId, e);
            return false;
        }
    }
}
