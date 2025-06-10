package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.Score;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ScoreService {
    /**
     * 录入成绩
     */
    Score inputScore(@NonNull Long studentId, @NonNull Long courseId, @NonNull BigDecimal score);

    /**
     * 批量录入成绩
     */
    List<Score> batchInputScores(@NonNull List<Score> scores);

    /**
     * 更新成绩
     */
    Score updateScore(@NonNull Long id, @NonNull BigDecimal newScore);

    /**
     * 获取学生成绩列表
     */
    List<Score> findByStudentId(@NonNull Long studentId);

    Optional<Score> findById(@NonNull Long id);

    /**
     * 获取课程成绩列表
     */
    List<Score> findByCourseId(@NonNull Long courseId);

    /**
     * 获取成绩详情
     */
    Optional<Score> findByStudentIdAndCourseId(@NonNull Long studentId, @NonNull Long courseId);

    /**
     * 获取课程成绩统计
     */
    Map<String, Object> getCourseScoreStatistics(@NonNull Long courseId);

    /**
     * 获取成绩分段统计
     */
    Map<String, Integer> getScoreDistribution(@NonNull Long courseId);

    /**
     * 删除成绩
     */
    void deleteById(@NonNull Long id);
}