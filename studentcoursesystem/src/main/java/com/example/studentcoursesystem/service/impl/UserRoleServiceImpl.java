package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.Role;
import com.example.studentcoursesystem.entity.UserRole;
import com.example.studentcoursesystem.repository.RoleRepository;
import com.example.studentcoursesystem.repository.UserRoleRepository;
import com.example.studentcoursesystem.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserRole> getUserRoles(@NonNull Long userId) {
        return userRoleRepository.findByUserId(userId);
    }

    @Override
    public boolean addUserRole(@NonNull Long userId, @NonNull Long roleId) {
        try {
            // 检查关联是否已存在
            if (userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
                log.warn("用户角色关联已存在: userId={}, roleId={}", userId, roleId);
                return false;
            }

            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            Instant now = Instant.now();
            userRole.setGmtCreate(now);
            userRole.setGmtModified(now);

            userRoleRepository.save(userRole);
            log.info("添加用户角色关联成功: userId={}, roleId={}", userId, roleId);
            return true;
        } catch (Exception e) {
            log.error("添加用户角色关联失败: userId={}, roleId={}", userId, roleId, e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> getRolesByUserId(@NonNull Long userId) {
        log.debug("获取用户角色: userId={}", userId);

        // 1. 获取用户角色关联
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        if (userRoles.isEmpty()) {
            log.debug("用户{}没有角色", userId);
            return Collections.emptyList();
        }

        // 2. 批量获取角色
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .toList();
        log.debug("用户{}的角色ID列表: {}", userId, roleIds);

        List<Role> roles = roleRepository.findAllById(roleIds);
        log.debug("用户{}共有{}个角色", userId, roles.size());

        return roles;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(@NonNull Long userId, @NonNull String roleName) {
        log.debug("检查用户是否具有角色: userId={}, roleName={}", userId, roleName);

        List<Role> roles = getRolesByUserId(userId);
        boolean hasRole = roles.stream()
                .anyMatch(role -> roleName.equals(role.getName()));

        log.debug("用户{}{}角色{}", userId, hasRole ? "具有" : "不具有", roleName);
        return hasRole;
    }

    @Override
    public void removeRoleFromUser(@NonNull Long userId, @NonNull Long roleId) {
        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);
        log.info("移除用户角色关联成功: userId={}, roleId={}", userId, roleId);
    }
}