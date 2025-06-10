package com.example.studentcoursesystem.controller;

import com.example.studentcoursesystem.common.Result;
import com.example.studentcoursesystem.dto.CourseDTO;
import com.example.studentcoursesystem.dto.PrerequisiteDTO;
import com.example.studentcoursesystem.entity.Course;
import com.example.studentcoursesystem.entity.Student;
import com.example.studentcoursesystem.entity.Teacher;  // 添加Teacher导入
import com.example.studentcoursesystem.entity.College; // 添加College导入
import com.example.studentcoursesystem.service.CourseService;
import com.example.studentcoursesystem.service.StudentService;
import com.example.studentcoursesystem.service.TeacherService; // 添加TeacherService导入
import com.example.studentcoursesystem.service.CollegeService; // 添加CollegeService导入
import com.example.studentcoursesystem.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "课程管理", description = "课程相关接口")
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final StudentService studentService;
    private final TeacherService teacherService; // 添加TeacherService注入
    private final CollegeService collegeService; // 添加CollegeService注入

    @Operation(summary = "获取所有课程")
    @GetMapping
    public Result<List<Course>> getAllCourses() {
        List<Course> courses = courseService.findAll();
        return Result.success(courses);
    }

    @Operation(summary = "获取启用的课程")
    @GetMapping("/enabled")
    public Result<List<Course>> getEnabledCourses() {
        List<Course> courses = courseService.findEnabledCourses();
        return Result.success(courses);
    }

    @Operation(summary = "根据ID获取课程")
    @GetMapping("/{id}")
    public Result<Course> getCourseById(@PathVariable Long id) {
        return courseService.findById(id)
                .map(Result::success)
                .orElse(Result.error("课程不存在"));
    }

    @Operation(summary = "根据课程编码获取课程")
    @GetMapping("/code/{courseCode}")
    public Result<Course> getCourseByCourseCode(@PathVariable String courseCode) {
        return courseService.findByCourseCode(courseCode)
                .map(Result::success)
                .orElse(Result.error("课程不存在"));
    }

    @Operation(summary = "根据教师获取课程列表")
    @GetMapping("/teacher/{teacherId}")
    public Result<List<Course>> getCoursesByTeacher(@PathVariable Long teacherId) {
        List<Course> courses = courseService.findByTeacherId(teacherId);
        return Result.success(courses);
    }

    @Operation(summary = "根据学院获取课程列表")
    @GetMapping("/college/{collegeId}")
    @PreAuthorize("@permissionCheckService.canManageCollegeStudents(authentication.principal.id, #collegeId)")
    public Result<List<Course>> getCoursesByCollege(@PathVariable Long collegeId) {
        List<Course> courses = courseService.findByCollegeId(collegeId);
        return Result.success(courses);
    }

    @Operation(summary = "创建课程")
    @PostMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Result<Course> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        try {
            // 转换DTO为Entity
            Course course = convertToEntity(courseDTO);

            // 权限检查
            if (SecurityUtils.isCollegeAdmin()) {
                Long userCollegeId = SecurityUtils.getCurrentUserCollegeId();
                if (course.getCollege() == null || !course.getCollege().getId().equals(userCollegeId)) {
                    return Result.error("只能创建本学院的课程");
                }
            } else if (SecurityUtils.isTeacher()) {
                Long userId = SecurityUtils.getCurrentUserId();
                if (course.getTeacher() == null || !course.getTeacher().getUser().getId().equals(userId)) {
                    return Result.error("只能创建自己授课的课程");
                }
            }

            Course created = courseService.create(course);
            return Result.success(created);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新课程")
    @PutMapping("/{id}")
    @PreAuthorize("@permissionCheckService.canManageCourse(authentication.principal.id, #id)")
    public Result<Course> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDTO courseDTO) {
        try {
            // 获取原课程信息检查权限
            Course existingCourse = courseService.findById(id).orElse(null);
            if (existingCourse == null) {
                return Result.error("课程不存在");
            }

            Course course = convertToEntity(courseDTO);
            course.setId(id);

            // 学院管理员不能将课程转移到其他学院
            if (SecurityUtils.isCollegeAdmin()) {
                Long userCollegeId = SecurityUtils.getCurrentUserCollegeId();
                if (course.getCollege() != null && !course.getCollege().getId().equals(userCollegeId)) {
                    return Result.error("不能将课程转移到其他学院");
                }
            }

            // 教师不能修改课程的授课教师
            if (SecurityUtils.isTeacher()) {
                Long userId = SecurityUtils.getCurrentUserId();
                if (course.getTeacher() != null && !course.getTeacher().getUser().getId().equals(userId)) {
                    return Result.error("不能修改课程的授课教师");
                }
            }

            Course updated = courseService.update(course);
            return Result.success(updated);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionCheckService.canManageCourse(authentication.principal.id, #id)")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteById(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "设置先修课程")
    @PostMapping("/{id}/prerequisites")
    @PreAuthorize("@permissionCheckService.canManageCourse(authentication.principal.id, #id)")
    public Result<Void> setPrerequisites(@PathVariable Long id, @Valid @RequestBody PrerequisiteDTO prerequisiteDTO) {
        try {
            courseService.setPrerequisites(id, prerequisiteDTO.getPrerequisiteIds());
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "检查学生是否满足先修条件")
    @GetMapping("/{id}/check-prerequisites")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Boolean> checkPrerequisites(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Student student = studentService.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("当前用户不是学生"));

        Course course = courseService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));

        boolean satisfied = courseService.checkPrerequisites(student, course);
        return Result.success(satisfied);
    }

    /**
     * 将CourseDTO转换为Course实体
     */
    private Course convertToEntity(CourseDTO dto) {
        Course course = new Course();
        course.setCourseCode(dto.getCourseCode());
        course.setName(dto.getName());
        course.setCredits(dto.getCredits());
        course.setMaxStudents(dto.getMaxStudents());
        course.setDescription(dto.getDescription());
        course.setStatus(dto.getStatus());

        // 设置教师
        if (dto.getTeacherId() != null) {
            Teacher teacher = teacherService.findById(dto.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("教师不存在: " + dto.getTeacherId()));
            course.setTeacher(teacher);
        }

        // 设置学院
        if (dto.getCollegeId() != null) {
            College college = collegeService.findById(dto.getCollegeId())
                    .orElseThrow(() -> new IllegalArgumentException("学院不存在: " + dto.getCollegeId()));
            course.setCollege(college);
        }

        return course;
    }
}