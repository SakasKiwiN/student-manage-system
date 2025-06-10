package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.Student;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface StudentService {
    /**
     * 获取所有学生
     */
    List<Student> findAll();

    /**
     * 根据ID获取学生
     */
    Optional<Student> findById(@NonNull Long id);

    /**
     * 根据学号获取学生
     */
    Optional<Student> findByStudentNo(@NonNull String studentNo);

    /**
     * 根据用户ID获取学生
     */
    Optional<Student> findByUserId(@NonNull Long userId);

    /**
     * 根据学院ID获取学生列表
     */
    List<Student> findByCollegeId(@NonNull Long collegeId);

    /**
     * 创建学生
     */
    Student create(@NonNull Student student);

    /**
     * 更新学生信息
     */
    Student update(@NonNull Student student);

    /**
     * 删除学生
     */
    void deleteById(@NonNull Long id);

    /**
     * 批量导入学生
     */
    List<Student> batchCreate(@NonNull List<Student> students);
}