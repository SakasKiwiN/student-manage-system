package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.Permission;
import org.springframework.lang.NonNull;

import java.util.List;

public interface PermissionService {
    List<Permission> getPermissions(@NonNull Long roleId);
}
