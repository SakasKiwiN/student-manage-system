package com.example.studentcoursesystem.repository;

import com.example.studentcoursesystem.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    List<Course> findByTeacherId(Long teacherId);
    List<Course> findByCollegeId(Long collegeId);
    List<Course> findByStatus(Integer status);
    boolean existsByCourseCode(String courseCode);

    @Query("SELECT c FROM Course c WHERE c.college.id = :collegeId AND c.status = :status")
    List<Course> findByCollegeIdAndStatus(@Param("collegeId") Long collegeId, @Param("status") Integer status);
}