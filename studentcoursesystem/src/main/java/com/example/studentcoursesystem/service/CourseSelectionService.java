package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.CourseSelection;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CourseSelectionService {
    /**
     * 学生选课
     */
    CourseSelection selectCourse(@NonNull Long studentId, @NonNull Long courseId);

    /**
     * 取消选课
     */
    void cancelSelection(@NonNull Long studentId, @NonNull Long courseId);

    /**
     * 获取学生的选课列表
     */
    List<CourseSelection> findByStudentId(@NonNull Long studentId);

    /**
     * 获取课程的选课学生列表
     */
    List<CourseSelection> findByCourseId(@NonNull Long courseId);

    /**
     * 获取课程的中签学生列表
     */
    List<CourseSelection> findSelectedStudents(@NonNull Long courseId);

    /**
     * 执行抽签
     */
    void performLottery(@NonNull Long courseId);

    /**
     * 批量抽签（学院管理员）
     */
    void performCollegeLottery(@NonNull Long collegeId);

    /**
     * 统计每门课程的选课人数
     */
    Map<Long, Long> getCourseSelectionStatistics();

    /**
     * 获取选课详情
     */
    Optional<CourseSelection> findByStudentIdAndCourseId(@NonNull Long studentId, @NonNull Long courseId);
}