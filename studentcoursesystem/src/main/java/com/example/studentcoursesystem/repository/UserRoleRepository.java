package com.example.studentcoursesystem.repository;

import com.example.studentcoursesystem.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUserId(Long userId);
    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}


