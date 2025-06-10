package com.example.studentcoursesystem.repository;

import com.example.studentcoursesystem.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    Optional<Score> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<Score> findByStudentId(Long studentId);
    List<Score> findByCourseId(Long courseId);

    @Query("SELECT s FROM Score s WHERE s.course.id = :courseId AND s.score >= :minScore AND s.score <= :maxScore")
    List<Score> findByCourseIdAndScoreBetween(@Param("courseId") Long courseId,
                                              @Param("minScore") BigDecimal minScore,
                                              @Param("maxScore") BigDecimal maxScore);

    @Query("SELECT COUNT(s) FROM Score s WHERE s.course.id = :courseId AND s.score >= 60")
    Long countPassedByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT AVG(s.score) FROM Score s WHERE s.course.id = :courseId")
    BigDecimal getAverageScoreByCourseId(@Param("courseId") Long courseId);
}