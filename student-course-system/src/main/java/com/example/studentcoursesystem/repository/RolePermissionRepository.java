package com.example.studentcoursesystem.repository;

import com.example.studentcoursesystem.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRoleId(Long roleId);
    List<RolePermission> findByRoleIdIn(Collection<Long> roleIds);
}
