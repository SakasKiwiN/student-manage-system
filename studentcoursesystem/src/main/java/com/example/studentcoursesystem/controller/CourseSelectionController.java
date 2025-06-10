package com.example.studentcoursesystem.controller;
import java.util.stream.Collectors;
import com.example.studentcoursesystem.common.Result;
import com.example.studentcoursesystem.dto.CourseSelectionDTO;
import com.example.studentcoursesystem.entity.*;
import com.example.studentcoursesystem.service.*;
import com.example.studentcoursesystem.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "选课管理", description = "选课相关接口")
@RestController
@RequestMapping("/api/course-selections")
@RequiredArgsConstructor
public class CourseSelectionController {

    private final CourseSelectionService courseSelectionService;
    private final StudentService studentService;
    private final CourseService courseService;
    private final TeacherService teacherService;
    @Operation(summary = "学生选课")
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public Result<CourseSelection> selectCourse(@Valid @RequestBody CourseSelectionDTO dto) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Student student = studentService.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("当前用户不是学生"));

            CourseSelection selection = courseSelectionService.selectCourse(student.getId(), dto.getCourseId());
            return Result.success("选课成功", selection);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "取消选课")
    @DeleteMapping("/course/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> cancelSelection(@PathVariable Long courseId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Student student = studentService.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("当前用户不是学生"));

            courseSelectionService.cancelSelection(student.getId(), courseId);
            return Result.success("取消选课成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取我的选课列表")
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<List<CourseSelection>> getMySelections() {
        Long userId = SecurityUtils.getCurrentUserId();
        Student student = studentService.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("当前用户不是学生"));

        List<CourseSelection> selections = courseSelectionService.findByStudentId(student.getId());
        return Result.success(selections);
    }

    @Operation(summary = "获取课程选课学生列表")
    @GetMapping("/course/{courseId}")
    @PreAuthorize("@permissionCheckService.canViewCourseStudents(authentication.principal.id, #courseId)")
    public Result<List<CourseSelection>> getCourseSelections(@PathVariable Long courseId) {
        List<CourseSelection> selections = courseSelectionService.findByCourseId(courseId);
        return Result.success(selections);
    }

    @Operation(summary = "获取课程中签学生列表")
    @GetMapping("/course/{courseId}/selected")
    @PreAuthorize("@permissionCheckService.canViewCourseStudents(authentication.principal.id, #courseId)")
    public Result<List<CourseSelection>> getSelectedStudents(@PathVariable Long courseId) {
        List<CourseSelection> selections = courseSelectionService.findSelectedStudents(courseId);
        return Result.success(selections);
    }

    @Operation(summary = "执行课程抽签")
    @PostMapping("/course/{courseId}/lottery")
    @PreAuthorize("@permissionCheckService.canPerformCourseLottery(authentication.principal.id, #courseId)")
    public Result<Void> performLottery(@PathVariable Long courseId) {
        try {
            courseSelectionService.performLottery(courseId);
            return Result.success("抽签完成", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "批量抽签（学院）")
    @PostMapping("/college/{collegeId}/lottery")
    @PreAuthorize("@permissionCheckService.canPerformCollegeLottery(authentication.principal.id, #collegeId)")
    public Result<Void> performCollegeLottery(@PathVariable Long collegeId) {
        try {
            courseSelectionService.performCollegeLottery(collegeId);
            return Result.success("批量抽签完成", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取选课统计")
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Result<Map<Long, Long>> getCourseSelectionStatistics() {
        Map<Long, Long> statistics = courseSelectionService.getCourseSelectionStatistics();

        // 如果是学院管理员或教师，需要过滤只显示相关课程的统计
        if (SecurityUtils.isCollegeAdmin()) {
            Long collegeId = SecurityUtils.getCurrentUserCollegeId();
            if (collegeId != null) {
                List<Course> collegeCourses = courseService.findByCollegeId(collegeId);
                Map<Long, Long> filteredStatistics = statistics.entrySet().stream()
                        .filter(entry -> collegeCourses.stream()
                                .anyMatch(course -> course.getId().equals(entry.getKey())))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                return Result.success(filteredStatistics);
            }
        } else if (SecurityUtils.isTeacher()) {
            Long userId = SecurityUtils.getCurrentUserId();
            Teacher teacher = teacherService.findByUserId(userId).orElse(null);
            if (teacher != null) {
                List<Course> teacherCourses = courseService.findByTeacherId(teacher.getId());
                Map<Long, Long> filteredStatistics = statistics.entrySet().stream()
                        .filter(entry -> teacherCourses.stream()
                                .anyMatch(course -> course.getId().equals(entry.getKey())))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                return Result.success(filteredStatistics);
            }
        }

        return Result.success(statistics);
    }
}