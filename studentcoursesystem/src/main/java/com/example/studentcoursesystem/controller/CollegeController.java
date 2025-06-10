package com.example.studentcoursesystem.controller;

import com.example.studentcoursesystem.common.Result;
import com.example.studentcoursesystem.entity.College;
import com.example.studentcoursesystem.service.CollegeService;
import com.example.studentcoursesystem.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Tag(name = "学院管理", description = "学院相关接口")
@RestController
@RequestMapping("/api/colleges")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeService collegeService;

    @Operation(summary = "获取所有学院")
    @GetMapping
    public Result<List<College>> getAllColleges() {
        // 学院管理员只能查看自己的学院
        if (SecurityUtils.isCollegeAdmin()) {
            Long collegeId = SecurityUtils.getCurrentUserCollegeId();
            if (collegeId != null) {
                Optional<College> college = collegeService.findById(collegeId);
                if (college.isPresent()) {
                    return Result.success(List.of(college.get()));
                }
            }
            return Result.success(List.of());
        }

        // 其他角色可以查看所有学院
        List<College> colleges = collegeService.findAll();
        return Result.success(colleges);
    }


    @Operation(summary = "根据ID获取学院")
    @GetMapping("/{id}")
    public Result<College> getCollegeById(@PathVariable Long id) {
        // 学院管理员只能查看自己的学院
        if (SecurityUtils.isCollegeAdmin()) {
            Long userCollegeId = SecurityUtils.getCurrentUserCollegeId();
            if (!id.equals(userCollegeId)) {
                return Result.error("无权限访问该学院信息");
            }
        }

        return collegeService.findById(id)
                .map(Result::success)
                .orElse(Result.error("学院不存在"));
    }

    @Operation(summary = "根据编码获取学院")
    @GetMapping("/code/{code}")
    public Result<College> getCollegeByCode(@PathVariable String code) {
        Optional<College> collegeOpt = collegeService.findByCode(code);
        if (collegeOpt.isEmpty()) {
            return Result.error("学院不存在");
        }

        College college = collegeOpt.get();
        // 学院管理员只能查看自己的学院
        if (SecurityUtils.isCollegeAdmin()) {
            Long userCollegeId = SecurityUtils.getCurrentUserCollegeId();
            if (!college.getId().equals(userCollegeId)) {
                return Result.error("无权限访问该学院信息");
            }
        }

        return Result.success(college);
    }

    @Operation(summary = "创建学院")
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')") // 修复：原来是 hasRole('ADMIN')
    public Result<College> createCollege(@Valid @RequestBody College college) {
        try {
            College created = collegeService.create(college);
            return Result.success(created);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新学院")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<College> updateCollege(@PathVariable Long id, @Valid @RequestBody College college) {
        try {
            college.setId(id);
            College updated = collegeService.update(college);
            return Result.success(updated);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除学院")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Void> deleteCollege(@PathVariable Long id) {
        try {
            collegeService.deleteById(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}