package com.example.studentcoursesystem.repository;

import com.example.studentcoursesystem.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> getPermissionsById(Long id);
}

