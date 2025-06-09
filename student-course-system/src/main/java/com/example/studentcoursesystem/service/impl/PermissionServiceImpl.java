package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.Permission;
import com.example.studentcoursesystem.repository.PermissionRepository;
import com.example.studentcoursesystem.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public List<Permission> getPermissions(Long roleId) {
        return permissionRepository.getPermissionsById(roleId);
    }
}
