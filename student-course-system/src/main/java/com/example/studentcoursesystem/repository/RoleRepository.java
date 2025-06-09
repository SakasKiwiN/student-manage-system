package com.example.studentcoursesystem.repository;

import com.example.studentcoursesystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> getRolesById(Long id);
}
