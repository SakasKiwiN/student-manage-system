package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.Role;
import com.example.studentcoursesystem.entity.UserRole;
import com.example.studentcoursesystem.repository.RoleRepository;
import com.example.studentcoursesystem.repository.UserRoleRepository;
import com.example.studentcoursesystem.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserRoleServiceImpl implements UserRoleService {
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<UserRole> getUserRoles(@NonNull Long userId) {
        return userRoleRepository.findByUserId(userId);
    }

    @Override
    public boolean addUserRole(@NonNull Long userId, @NonNull Long roleId) {
        try {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            Instant now = Instant.now();
            userRole.setGmtCreate(now);
            userRole.setGmtModified(now);
            userRoleRepository.save(userRole);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        System.out.println("Getting roles for userId: " + userId);
        // 1. 获取用户角色关联
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 批量获取角色
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .toList();
        System.out.println("Found " + roleIds.size() + " roleIds for userId: " + userId);

        return roleRepository.findAllById(roleIds);
    }
}