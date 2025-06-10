package com.example.studentcoursesystem.repository;

import com.example.studentcoursesystem.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentNo(String studentNo);
    Optional<Student> findByUserId(Long userId);
    List<Student> findByCollegeId(Long collegeId);
    boolean existsByStudentNo(String studentNo);

    @Query("SELECT s FROM Student s WHERE s.college.id = :collegeId AND s.grade = :grade")
    List<Student> findByCollegeIdAndGrade(@Param("collegeId") Long collegeId, @Param("grade") String grade);
}