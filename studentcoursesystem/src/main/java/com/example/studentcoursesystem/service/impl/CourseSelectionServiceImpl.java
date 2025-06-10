package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.Course;
import com.example.studentcoursesystem.entity.CourseSelection;
import com.example.studentcoursesystem.entity.Student;
import com.example.studentcoursesystem.repository.CourseRepository;
import com.example.studentcoursesystem.repository.CourseSelectionRepository;
import com.example.studentcoursesystem.repository.StudentRepository;
import com.example.studentcoursesystem.service.CourseSelectionService;
import com.example.studentcoursesystem.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CourseSelectionServiceImpl implements CourseSelectionService {

    private final CourseSelectionRepository courseSelectionRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseService courseService;

    @Override
    public CourseSelection selectCourse(@NonNull Long studentId, @NonNull Long courseId) {
        // 检查学生和课程是否存在
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在: " + studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在: " + courseId));

        // 检查课程是否启用
        if (course.getStatus() != 1) {
            throw new IllegalStateException("课程未启用，无法选课");
        }

        // 检查是否已经选过该课程
        Optional<CourseSelection> existing = courseSelectionRepository
                .findByStudentIdAndCourseId(studentId, courseId);
        if (existing.isPresent()) {
            throw new IllegalStateException("已经选择该课程");
        }

        // 检查先修条件
        if (!courseService.checkPrerequisites(student, course)) {
            throw new IllegalStateException("不满足课程先修条件");
        }

        // 创建选课记录
        CourseSelection selection = new CourseSelection();
        selection.setStudent(student);
        selection.setCourse(course);
        selection.setStatus(1); // 申请中
        selection.setLotteryResult(0); // 未抽签

        Instant now = Instant.now();
        selection.setGmtCreate(now);
        selection.setGmtModified(now);

        CourseSelection saved = courseSelectionRepository.save(selection);
        log.info("学生 {} 选择课程 {} 成功", student.getStudentNo(), course.getCourseCode());
        return saved;
    }

    @Override
    public void cancelSelection(@NonNull Long studentId, @NonNull Long courseId) {
        CourseSelection selection = courseSelectionRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("未找到选课记录"));

        // 检查是否已经中签
        if (selection.getStatus() == 2) {
            throw new IllegalStateException("已中签课程无法取消");
        }

        courseSelectionRepository.delete(selection);
        log.info("取消选课成功: 学生 {} - 课程 {}", studentId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseSelection> findByStudentId(@NonNull Long studentId) {
        return courseSelectionRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseSelection> findByCourseId(@NonNull Long courseId) {
        return courseSelectionRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseSelection> findSelectedStudents(@NonNull Long courseId) {
        return courseSelectionRepository.findByCourseIdAndStatus(courseId, 2);
    }

    @Override
    public void performLottery(@NonNull Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在: " + courseId));

        // 获取所有申请中的选课记录
        List<CourseSelection> selections = courseSelectionRepository
                .findByCourseIdAndStatus(courseId, 1);

        if (selections.isEmpty()) {
            log.info("课程 {} 没有待抽签的选课申请", course.getCourseCode());
            return;
        }

        int maxStudents = course.getMaxStudents();
        List<CourseSelection> shuffled = new ArrayList<>(selections);
        Collections.shuffle(shuffled);

        Instant now = Instant.now();

        // 中签的学生
        for (int i = 0; i < Math.min(maxStudents, shuffled.size()); i++) {
            CourseSelection selection = shuffled.get(i);
            selection.setStatus(2); // 已中签
            selection.setLotteryResult(1); // 中签
            selection.setGmtModified(now);
            courseSelectionRepository.save(selection);
        }

        // 未中签的学生
        for (int i = maxStudents; i < shuffled.size(); i++) {
            CourseSelection selection = shuffled.get(i);
            selection.setStatus(3); // 未中签
            selection.setLotteryResult(2); // 未中签
            selection.setGmtModified(now);
            courseSelectionRepository.save(selection);
        }

        log.info("课程 {} 抽签完成: {} 人中签, {} 人未中签",
                course.getCourseCode(),
                Math.min(maxStudents, shuffled.size()),
                Math.max(0, shuffled.size() - maxStudents));
    }

    @Override
    public void performCollegeLottery(@NonNull Long collegeId) {
        // 获取学院的所有启用课程
        List<Course> courses = courseRepository.findByCollegeIdAndStatus(collegeId, 1);

        for (Course course : courses) {
            try {
                performLottery(course.getId());
            } catch (Exception e) {
                log.error("课程 {} 抽签失败: {}", course.getCourseCode(), e.getMessage());
            }
        }

        log.info("学院 {} 批量抽签完成", collegeId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getCourseSelectionStatistics() {
        List<Course> allCourses = courseRepository.findAll();
        Map<Long, Long> statistics = new HashMap<>();

        for (Course course : allCourses) {
            Long count = courseSelectionRepository.countByCourseId(course.getId());
            statistics.put(course.getId(), count);
        }

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseSelection> findByStudentIdAndCourseId(@NonNull Long studentId, @NonNull Long courseId) {
        return courseSelectionRepository.findByStudentIdAndCourseId(studentId, courseId);
    }
}