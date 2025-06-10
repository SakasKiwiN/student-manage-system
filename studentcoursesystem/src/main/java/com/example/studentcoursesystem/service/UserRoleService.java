package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.Role;
import com.example.studentcoursesystem.entity.UserRole;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UserRoleService {
    /**
     * 获取用户的所有角色关联
     */
    List<UserRole> getUserRoles(@NonNull Long userId);

    /**
     * 为用户添加角色
     */
    boolean addUserRole(@NonNull Long userId, @NonNull Long roleId);

    /**
     * 获取用户的所有角色
     */
    List<Role> getRolesByUserId(@NonNull Long userId);

    /**
     * 检查用户是否具有指定角色
     * @param userId 用户ID
     * @param roleName 角色名称
     * @return 是否具有该角色
     */
    boolean hasRole(@NonNull Long userId, @NonNull String roleName);

    /**
     * 为用户移除角色
     */
    void removeRoleFromUser(@NonNull Long userId, @NonNull Long roleId);
}
