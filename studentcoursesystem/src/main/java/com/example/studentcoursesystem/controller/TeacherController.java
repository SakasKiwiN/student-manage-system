package com.example.studentcoursesystem.controller;

import com.example.studentcoursesystem.common.Result;
import com.example.studentcoursesystem.entity.Teacher;
import com.example.studentcoursesystem.service.TeacherService;
import com.example.studentcoursesystem.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Tag(name = "教师管理", description = "教师相关接口")
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @Operation(summary = "获取所有教师")
    @GetMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'COLLEGE_ADMIN')")
    public Result<List<Teacher>> getAllTeachers() {
        // 如果是学院管理员，只返回本学院教师
        if (SecurityUtils.isCollegeAdmin()) {
            Long collegeId = SecurityUtils.getCurrentUserCollegeId();
            if (collegeId != null) {
                List<Teacher> teachers = teacherService.findByCollegeId(collegeId);
                return Result.success(teachers);
            }
            return Result.error("无法确定所属学院");
        }

        // 系统管理员返回所有教师
        List<Teacher> teachers = teacherService.findAll();
        return Result.success(teachers);
    }

    @Operation(summary = "根据ID获取教师")
    @GetMapping("/{id}")
    @PreAuthorize("@permissionCheckService.canViewTeacher(authentication.principal.id, #id)")
    public Result<Teacher> getTeacherById(@PathVariable Long id) {
        return teacherService.findById(id)
                .map(Result::success)
                .orElse(Result.error("教师不存在"));
    }

    @Operation(summary = "获取当前登录教师信息")
    @GetMapping("/current")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Teacher> getCurrentTeacher() {
        Long userId = SecurityUtils.getCurrentUserId();
        return teacherService.findByUserId(userId)
                .map(Result::success)
                .orElse(Result.error("当前用户不是教师"));
    }

    @Operation(summary = "根据学院获取教师列表")
    @GetMapping("/college/{collegeId}")
    @PreAuthorize("@permissionCheckService.canManageCollegeTeachers(authentication.principal.id, #collegeId)")
    public Result<List<Teacher>> getTeachersByCollege(@PathVariable Long collegeId) {
        List<Teacher> teachers = teacherService.findByCollegeId(collegeId);
        return Result.success(teachers);
    }

    @Operation(summary = "创建教师")
    @PostMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'COLLEGE_ADMIN')")
    public Result<Teacher> createTeacher(@Valid @RequestBody Teacher teacher) {
        try {
            // 学院管理员只能创建本学院的教师
            if (SecurityUtils.isCollegeAdmin()) {
                Long userCollegeId = SecurityUtils.getCurrentUserCollegeId();
                if (teacher.getCollege() == null || !teacher.getCollege().getId().equals(userCollegeId)) {
                    return Result.error("只能创建本学院的教师");
                }
            }

            Teacher created = teacherService.create(teacher);
            return Result.success(created);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新教师信息")
    @PutMapping("/{id}")
    @PreAuthorize("@permissionCheckService.canViewTeacher(authentication.principal.id, #id)")
    public Result<Teacher> updateTeacher(@PathVariable Long id, @Valid @RequestBody Teacher teacher) {
        try {
            // 获取原教师信息检查权限
            Teacher existingTeacher = teacherService.findById(id).orElse(null);
            if (existingTeacher == null) {
                return Result.error("教师不存在");
            }

            // 学院管理员不能将教师转移到其他学院
            if (SecurityUtils.isCollegeAdmin()) {
                Long userCollegeId = SecurityUtils.getCurrentUserCollegeId();
                if (teacher.getCollege() != null && !teacher.getCollege().getId().equals(userCollegeId)) {
                    return Result.error("不能将教师转移到其他学院");
                }
                if (!existingTeacher.getCollege().getId().equals(userCollegeId)) {
                    return Result.error("无权限修改该教师信息");
                }
            }

            teacher.setId(id);
            Teacher updated = teacherService.update(teacher);
            return Result.success(updated);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除教师")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'COLLEGE_ADMIN')")
    public Result<Void> deleteTeacher(@PathVariable Long id) {
        try {
            // 学院管理员只能删除本学院的教师
            if (SecurityUtils.isCollegeAdmin()) {
                Teacher teacher = teacherService.findById(id).orElse(null);
                if (teacher == null) {
                    return Result.error("教师不存在");
                }

                Long userCollegeId = SecurityUtils.getCurrentUserCollegeId();
                if (!teacher.getCollege().getId().equals(userCollegeId)) {
                    return Result.error("无权限删除该教师");
                }
            }

            teacherService.deleteById(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}