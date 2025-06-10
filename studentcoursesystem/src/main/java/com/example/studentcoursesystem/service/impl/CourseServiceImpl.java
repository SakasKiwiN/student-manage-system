package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.Course;
import com.example.studentcoursesystem.entity.Score;
import com.example.studentcoursesystem.entity.Student;
import com.example.studentcoursesystem.repository.CourseRepository;
import com.example.studentcoursesystem.repository.ScoreRepository;
import com.example.studentcoursesystem.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final ScoreRepository scoreRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Course> findById(@NonNull Long id) {
        return courseRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Course> findByCourseCode(@NonNull String courseCode) {
        return courseRepository.findByCourseCode(courseCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findByTeacherId(@NonNull Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findByCollegeId(@NonNull Long collegeId) {
        return courseRepository.findByCollegeId(collegeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findEnabledCourses() {
        return courseRepository.findByStatus(1);
    }

    @Override
    public Course create(@NonNull Course course) {
        if (courseRepository.existsByCourseCode(course.getCourseCode())) {
            throw new IllegalArgumentException("课程编码已存在: " + course.getCourseCode());
        }

        Instant now = Instant.now();
        course.setGmtCreate(now);
        course.setGmtModified(now);

        Course saved = courseRepository.save(course);
        log.info("创建课程成功: {} - {}", saved.getCourseCode(), saved.getName());
        return saved;
    }

    @Override
    public Course update(@NonNull Course course) {
        if (!courseRepository.existsById(course.getId())) {
            throw new IllegalArgumentException("课程不存在: " + course.getId());
        }

        course.setGmtModified(Instant.now());
        Course updated = courseRepository.save(course);
        log.info("更新课程信息成功: {} - {}", updated.getCourseCode(), updated.getName());
        return updated;
    }

    @Override
    public void deleteById(@NonNull Long id) {
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException("课程不存在: " + id);
        }

        courseRepository.deleteById(id);
        log.info("删除课程成功: {}", id);
    }

    @Override
    public void setPrerequisites(@NonNull Long courseId, @NonNull List<Long> prerequisiteIds) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在: " + courseId));

        List<Course> prerequisites = courseRepository.findAllById(prerequisiteIds);
        if (prerequisites.size() != prerequisiteIds.size()) {
            throw new IllegalArgumentException("部分先修课程不存在");
        }

        course.setPrerequisites(prerequisites);
        courseRepository.save(course);
        log.info("设置先修课程成功: {}", courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkPrerequisites(@NonNull Student student, @NonNull Course course) {
        List<Course> prerequisites = course.getPrerequisites();
        if (prerequisites == null || prerequisites.isEmpty()) {
            return true;
        }

        for (Course prerequisite : prerequisites) {
            Optional<Score> score = scoreRepository.findByStudentIdAndCourseId(
                    student.getId(), prerequisite.getId());

            // 如果没有成绩或成绩低于60分，则不满足先修条件
            if (score.isEmpty() || score.get().getScore().compareTo(new BigDecimal("60")) < 0) {
                log.debug("学生 {} 未满足课程 {} 的先修条件: {}",
                        student.getStudentNo(), course.getCourseCode(), prerequisite.getCourseCode());
                return false;
            }
        }

        return true;
    }
}