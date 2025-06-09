package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.Role;
import com.example.studentcoursesystem.repository.RoleRepository;
import com.example.studentcoursesystem.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<Role> getRoles(@NonNull Long userId) {
        return roleRepository.getRolesById(userId);
    }
}
