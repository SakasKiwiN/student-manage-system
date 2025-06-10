package com.example.studentcoursesystem.repository;

import com.example.studentcoursesystem.entity.Courseware;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursewareRepository extends JpaRepository<Courseware, Long> {
    List<Courseware> findByCourseId(Long courseId);
    List<Courseware> findByCourseIdOrderByUploadTimeDesc(Long courseId);
}