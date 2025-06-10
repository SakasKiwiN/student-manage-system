package com.example.studentcoursesystem.repository;

import com.example.studentcoursesystem.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByTeacherNo(String teacherNo);
    Optional<Teacher> findByUserId(Long userId);
    List<Teacher> findByCollegeId(Long collegeId);
    boolean existsByTeacherNo(String teacherNo);
}