package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.Role;
import org.springframework.lang.NonNull;

import java.util.List;

public interface RoleService {
    List<Role> getRoles(@NonNull Long userId);
}
