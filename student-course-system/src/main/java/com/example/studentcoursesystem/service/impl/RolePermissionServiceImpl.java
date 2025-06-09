package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.Permission;
import com.example.studentcoursesystem.entity.RolePermission;
import com.example.studentcoursesystem.repository.PermissionRepository;
import com.example.studentcoursesystem.repository.RolePermissionRepository;
import com.example.studentcoursesystem.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class RolePermissionServiceImpl implements RolePermissionService {
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public List<RolePermission> getRolePermissions(@NonNull Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId);
    }

    @Override
    public boolean addRolePermission(@NonNull Long roleId, @NonNull Long permissionId) {
        try {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            Instant now = Instant.now();
            rolePermission.setGmtCreate(now);
            rolePermission.setGmtModified(now);
            rolePermissionRepository.save(rolePermission);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        System.out.println("Getting permissions for roleId: " + roleId);
        // 1. 获取角色权限关联
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(roleId);
        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 批量获取权限
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .toList();
        System.out.println("Found " + permissionIds.size() + " permissionIds for roleId: " + roleId);

        return permissionRepository.findAllById(permissionIds);
    }

    @Override
    public List<Permission> getPermissionsByRoleIds(Collection<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        System.out.println("Getting permissions for " + roleIds.size() + " roleIds");

        // 1. 批量获取角色权限关联
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleIdIn(roleIds);
        if (rolePermissions.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 批量获取权限
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .distinct()
                .toList();
        System.out.println("Found " + permissionIds.size() + " unique permissionIds for " + roleIds.size() + " roles");

        return permissionRepository.findAllById(permissionIds);
    }
}