package com.example.studentcoursesystem.controller;

import com.example.studentcoursesystem.common.Result;
import com.example.studentcoursesystem.dto.StudentDTO;
import com.example.studentcoursesystem.entity.Student;
import com.example.studentcoursesystem.service.StudentService;
import com.example.studentcoursesystem.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Tag(name = "学生管理", description = "学生相关接口")
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "获取所有学生")
    @GetMapping
    @PreAuthorize("@permissionCheckService.canAccessAllStudents(authentication.principal.id)")
    public Result<List<Student>> getAllStudents() {
        // 如果是学院管理员，只返回本学院学生
        if (SecurityUtils.isCollegeAdmin()) {
            Long collegeId = SecurityUtils.getCurrentUserCollegeId();
            if (collegeId != null) {
                List<Student> students = studentService.findByCollegeId(collegeId);
                return Result.success(students);
            }
        }

        // 系统管理员返回所有学生
        List<Student> students = studentService.findAll();
        return Result.success(students);
    }

    @Operation(summary = "根据ID获取学生")
    @GetMapping("/{id}")
    @PreAuthorize("@permissionCheckService.canViewStudent(authentication.principal.id, #id)")
    public Result<Student> getStudentById(@PathVariable Long id) {
        return studentService.findById(id)
                .map(Result::success)
                .orElse(Result.error("学生不存在"));
    }

    @Operation(summary = "根据学号获取学生")
    @GetMapping("/no/{studentNo}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Result<Student> getStudentByNo(@PathVariable String studentNo) {
        Optional<Student> studentOpt = studentService.findByStudentNo(studentNo);
        if (studentOpt.isEmpty()) {
            return Result.error("学生不存在");
        }

        Student student = studentOpt.get();
        // 检查学院级别权限
        if (SecurityUtils.isCollegeAdmin() || SecurityUtils.isTeacher()) {
            if (!SecurityUtils.canAccessCollegeResource(student.getCollege().getId())) {
                return Result.error("无权限访问该学生信息");
            }
        }

        return Result.success(student);
    }

    @Operation(summary = "获取当前登录学生信息")
    @GetMapping("/current")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Student> getCurrentStudent() {
        Long userId = SecurityUtils.getCurrentUserId();
        return studentService.findByUserId(userId)
                .map(Result::success)
                .orElse(Result.error("当前用户不是学生"));
    }

    @Operation(summary = "根据学院获取学生列表")
    @GetMapping("/college/{collegeId}")
    @PreAuthorize("@permissionCheckService.canManageCollegeStudents(authentication.principal.id, #collegeId)")
    public Result<List<Student>> getStudentsByCollege(@PathVariable Long collegeId) {
        List<Student> students = studentService.findByCollegeId(collegeId);
        return Result.success(students);
    }

    @Operation(summary = "创建学生")
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')") // 只有系统管理员可以创建学生
    public Result<Student> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        try {
            // 这里需要实现用户创建逻辑
            return Result.error("需要实现用户创建逻辑");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新学生信息")
    @PutMapping("/{id}")  // 改为 PutMapping
    @PreAuthorize("@permissionCheckService.canViewStudent(authentication.principal.id, #id)")
    public Result<Student> updateStudent(@PathVariable Long id, @Valid @RequestBody Student student) {
        try {
            student.setId(id);
            Student updated = studentService.update(student);
            return Result.success(updated);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除学生")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')") // 只有系统管理员可以删除学生
    public Result<Void> deleteStudent(@PathVariable Long id) {
        try {
            studentService.deleteById(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "批量导入学生")
    @PostMapping("/batch")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<List<Student>> batchCreateStudents(@Valid @RequestBody List<StudentDTO> studentDTOs) {
        try {
            // 需要实现批量创建用户和学生的逻辑
            return Result.error("需要实现批量导入逻辑");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

}