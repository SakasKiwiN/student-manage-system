package com.example.studentcoursesystem.repository;

import com.example.studentcoursesystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> getUserByUsername(String username);
}

