package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.Teacher;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface TeacherService {
    /**
     * 获取所有教师
     */
    List<Teacher> findAll();

    /**
     * 根据ID获取教师
     */
    Optional<Teacher> findById(@NonNull Long id);

    /**
     * 根据教师编号获取教师
     */
    Optional<Teacher> findByTeacherNo(@NonNull String teacherNo);

    /**
     * 根据用户ID获取教师
     */
    Optional<Teacher> findByUserId(@NonNull Long userId);

    /**
     * 根据学院ID获取教师列表
     */
    List<Teacher> findByCollegeId(@NonNull Long collegeId);

    /**
     * 创建教师
     */
    Teacher create(@NonNull Teacher teacher);

    /**
     * 更新教师信息
     */
    Teacher update(@NonNull Teacher teacher);

    /**
     * 删除教师
     */
    void deleteById(@NonNull Long id);
}