package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.Course;
import com.example.studentcoursesystem.entity.Student;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    /**
     * 获取所有课程
     */
    List<Course> findAll();

    /**
     * 根据ID获取课程
     */
    Optional<Course> findById(@NonNull Long id);

    /**
     * 根据课程编码获取课程
     */
    Optional<Course> findByCourseCode(@NonNull String courseCode);

    /**
     * 根据教师ID获取课程列表
     */
    List<Course> findByTeacherId(@NonNull Long teacherId);

    /**
     * 根据学院ID获取课程列表
     */
    List<Course> findByCollegeId(@NonNull Long collegeId);

    /**
     * 获取启用的课程
     */
    List<Course> findEnabledCourses();

    /**
     * 创建课程
     */
    Course create(@NonNull Course course);

    /**
     * 更新课程信息
     */
    Course update(@NonNull Course course);

    /**
     * 删除课程
     */
    void deleteById(@NonNull Long id);

    /**
     * 设置课程先修关系
     */
    void setPrerequisites(@NonNull Long courseId, @NonNull List<Long> prerequisiteIds);

    /**
     * 检查学生是否满足先修条件
     */
    boolean checkPrerequisites(@NonNull Student student, @NonNull Course course);
}