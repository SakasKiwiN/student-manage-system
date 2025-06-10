package com.example.studentcoursesystem.repository;

import com.example.studentcoursesystem.entity.CourseSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Long> {
    Optional<CourseSelection> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<CourseSelection> findByStudentId(Long studentId);
    List<CourseSelection> findByCourseId(Long courseId);
    List<CourseSelection> findByCourseIdAndStatus(Long courseId, Integer status);

    @Query("SELECT cs FROM CourseSelection cs WHERE cs.course.id = :courseId AND cs.lotteryResult = :lotteryResult")
    List<CourseSelection> findByCourseIdAndLotteryResult(@Param("courseId") Long courseId, @Param("lotteryResult") Integer lotteryResult);

    @Query("SELECT COUNT(cs) FROM CourseSelection cs WHERE cs.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(cs) FROM CourseSelection cs WHERE cs.course.id = :courseId AND cs.status = :status")
    Long countByCourseIdAndStatus(@Param("courseId") Long courseId, @Param("status") Integer status);
}