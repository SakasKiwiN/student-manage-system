package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.Course;
import com.example.studentcoursesystem.entity.CourseSelection;
import com.example.studentcoursesystem.entity.Score;
import com.example.studentcoursesystem.entity.Student;
import com.example.studentcoursesystem.repository.CourseRepository;
import com.example.studentcoursesystem.repository.CourseSelectionRepository;
import com.example.studentcoursesystem.repository.ScoreRepository;
import com.example.studentcoursesystem.repository.StudentRepository;
import com.example.studentcoursesystem.service.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScoreServiceImpl implements ScoreService {

    private final ScoreRepository scoreRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseSelectionRepository courseSelectionRepository;

    @Override
    public Score inputScore(@NonNull Long studentId, @NonNull Long courseId, @NonNull BigDecimal score) {
        // 验证成绩范围
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("成绩必须在0-100之间");
        }

        // 检查学生和课程是否存在
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在: " + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在: " + courseId));

        // 检查学生是否选了这门课且已中签
        CourseSelection selection = courseSelectionRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("学生未选择该课程"));

        if (selection.getStatus() != 2) {
            throw new IllegalStateException("学生未中签该课程，无法录入成绩");
        }

        // 检查是否已有成绩
        Optional<Score> existing = scoreRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (existing.isPresent()) {
            throw new IllegalStateException("该学生该课程成绩已存在，请使用更新功能");
        }

        // 创建成绩记录
        Score scoreEntity = new Score();
        scoreEntity.setStudent(student);
        scoreEntity.setCourse(course);
        scoreEntity.setScore(score);

        Instant now = Instant.now();
        scoreEntity.setGmtCreate(now);
        scoreEntity.setGmtModified(now);

        Score saved = scoreRepository.save(scoreEntity);
        log.info("录入成绩成功: 学生 {} - 课程 {} - 成绩 {}",
                student.getStudentNo(), course.getCourseCode(), score);
        return saved;
    }

    @Override
    public List<Score> batchInputScores(@NonNull List<Score> scores) {
        Instant now = Instant.now();

        for (Score score : scores) {
            // 验证每个成绩
            if (score.getScore().compareTo(BigDecimal.ZERO) < 0 ||
                    score.getScore().compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("成绩必须在0-100之间");
            }

            score.setGmtCreate(now);
            score.setGmtModified(now);
        }

        List<Score> saved = scoreRepository.saveAll(scores);
        log.info("批量录入成绩成功: {} 条", saved.size());
        return saved;
    }

    @Override
    public Score updateScore(@NonNull Long id, @NonNull BigDecimal newScore) {
        // 验证成绩范围
        if (newScore.compareTo(BigDecimal.ZERO) < 0 || newScore.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("成绩必须在0-100之间");
        }

        Score score = scoreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("成绩记录不存在: " + id));

        BigDecimal oldScore = score.getScore();
        score.setScore(newScore);
        score.setGmtModified(Instant.now());

        Score updated = scoreRepository.save(score);
        log.info("更新成绩成功: {} -> {}", oldScore, newScore);
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Score> findById(@NonNull Long id) {
        log.debug("根据ID查询成绩记录: {}", id);
        return scoreRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Score> findByStudentId(@NonNull Long studentId) {
        return scoreRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Score> findByCourseId(@NonNull Long courseId) {
        return scoreRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Score> findByStudentIdAndCourseId(@NonNull Long studentId, @NonNull Long courseId) {
        return scoreRepository.findByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCourseScoreStatistics(@NonNull Long courseId) {
        List<Score> scores = scoreRepository.findByCourseId(courseId);
        Map<String, Object> statistics = new HashMap<>();

        if (scores.isEmpty()) {
            statistics.put("count", 0);
            statistics.put("average", 0);
            statistics.put("max", 0);
            statistics.put("min", 0);
            statistics.put("passRate", 0);
            return statistics;
        }

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal max = scores.get(0).getScore();
        BigDecimal min = scores.get(0).getScore();
        long passCount = 0;

        for (Score score : scores) {
            BigDecimal value = score.getScore();
            sum = sum.add(value);

            if (value.compareTo(max) > 0) {
                max = value;
            }
            if (value.compareTo(min) < 0) {
                min = value;
            }
            if (value.compareTo(new BigDecimal("60")) >= 0) {
                passCount++;
            }
        }

        BigDecimal average = sum.divide(new BigDecimal(scores.size()), 2, RoundingMode.HALF_UP);
        double passRate = (double) passCount / scores.size() * 100;

        statistics.put("count", scores.size());
        statistics.put("average", average);
        statistics.put("max", max);
        statistics.put("min", min);
        statistics.put("passRate", String.format("%.2f%%", passRate));

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getScoreDistribution(@NonNull Long courseId) {
        List<Score> scores = scoreRepository.findByCourseId(courseId);
        Map<String, Integer> distribution = new HashMap<>();

        // 初始化分段
        distribution.put("0-59", 0);
        distribution.put("60-69", 0);
        distribution.put("70-79", 0);
        distribution.put("80-89", 0);
        distribution.put("90-100", 0);

        for (Score score : scores) {
            BigDecimal value = score.getScore();

            if (value.compareTo(new BigDecimal("60")) < 0) {
                distribution.merge("0-59", 1, Integer::sum);
            } else if (value.compareTo(new BigDecimal("70")) < 0) {
                distribution.merge("60-69", 1, Integer::sum);
            } else if (value.compareTo(new BigDecimal("80")) < 0) {
                distribution.merge("70-79", 1, Integer::sum);
            } else if (value.compareTo(new BigDecimal("90")) < 0) {
                distribution.merge("80-89", 1, Integer::sum);
            } else {
                distribution.merge("90-100", 1, Integer::sum);
            }
        }

        return distribution;
    }

    @Override
    public void deleteById(@NonNull Long id) {
        if (!scoreRepository.existsById(id)) {
            throw new IllegalArgumentException("成绩记录不存在: " + id);
        }

        scoreRepository.deleteById(id);
        log.info("删除成绩记录成功: {}", id);
    }
}