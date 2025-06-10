package com.example.studentcoursesystem.controller;

import com.example.studentcoursesystem.common.Result;
import com.example.studentcoursesystem.dto.ScoreDTO;
import com.example.studentcoursesystem.entity.*;
import com.example.studentcoursesystem.service.*;
import com.example.studentcoursesystem.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "成绩管理", description = "成绩相关接口")
@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final PermissionCheckService permissionCheckService;
    private final CourseService courseService;
    @Operation(summary = "录入成绩")
    @PostMapping
    @PreAuthorize("@permissionCheckService.canInputCourseScore(authentication.principal.id, #scoreDTO.courseId)")
    public Result<Score> inputScore(@Valid @RequestBody ScoreDTO scoreDTO) {
        try {
            Score score = scoreService.inputScore(
                    scoreDTO.getStudentId(),
                    scoreDTO.getCourseId(),
                    scoreDTO.getScore()
            );
            return Result.success("成绩录入成功", score);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


    @Operation(summary = "批量录入成绩")
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Result<List<Score>> batchInputScores(@Valid @RequestBody List<ScoreDTO> scoreDTOs) {
        try {
            // 检查每个成绩的录入权限
            Long userId = SecurityUtils.getCurrentUserId();
            for (ScoreDTO dto : scoreDTOs) {
                if (!permissionCheckService.canInputCourseScore(userId, dto.getCourseId())) {
                    return Result.error("无权限录入课程ID为 " + dto.getCourseId() + " 的成绩");
                }
            }

            // 转换DTO为Entity列表
            List<Score> scores = scoreDTOs.stream()
                    .map(dto -> {
                        Score score = new Score();
                        // 这里需要设置学生和课程实体，简化处理
                        return score;
                    })
                    .toList();

            List<Score> saved = scoreService.batchInputScores(scores);
            return Result.success("批量录入成绩成功", saved);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新成绩")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Result<Score> updateScore(@PathVariable Long id, @RequestParam BigDecimal score) {
        try {
            // 检查成绩记录的权限
            Optional<Score> existingScoreOpt = scoreService.findById(id);
            if (existingScoreOpt.isEmpty()) {
                return Result.error("成绩记录不存在");
            }

            Score existingScore = existingScoreOpt.get();
            Long userId = SecurityUtils.getCurrentUserId();
            if (!permissionCheckService.canInputCourseScore(userId, existingScore.getCourse().getId())) {
                return Result.error("无权限修改该成绩");
            }

            Score updated = scoreService.updateScore(id, score);
            return Result.success("成绩更新成功", updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取我的成绩")
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<List<Score>> getMyScores() {
        Long userId = SecurityUtils.getCurrentUserId();
        Student student = studentService.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("当前用户不是学生"));

        List<Score> scores = scoreService.findByStudentId(student.getId());
        return Result.success(scores);
    }

    @Operation(summary = "获取学生成绩")
    @GetMapping("/student/{studentId}")
    @PreAuthorize("@permissionCheckService.canViewStudentScore(authentication.principal.id, #studentId)")
    public Result<List<Score>> getStudentScores(@PathVariable Long studentId) {
        List<Score> scores = scoreService.findByStudentId(studentId);

        // 如果是教师，只返回自己课程的成绩
        if (SecurityUtils.isTeacher()) {
            Long userId = SecurityUtils.getCurrentUserId();
            Teacher teacher = teacherService.findByUserId(userId).orElse(null);
            if (teacher != null) {
                List<Course> teacherCourses = courseService.findByTeacherId(teacher.getId());
                scores = scores.stream()
                        .filter(score -> teacherCourses.stream()
                                .anyMatch(course -> course.getId().equals(score.getCourse().getId())))
                        .collect(Collectors.toList());
            }
        }

        return Result.success(scores);
    }

    @Operation(summary = "获取课程成绩列表")
    @GetMapping("/course/{courseId}")
    @PreAuthorize("@permissionCheckService.canViewCourseStudents(authentication.principal.id, #courseId)")
    public Result<List<Score>> getCourseScores(@PathVariable Long courseId) {
        List<Score> scores = scoreService.findByCourseId(courseId);
        return Result.success(scores);
    }

    @Operation(summary = "获取课程成绩统计")
    @GetMapping("/course/{courseId}/statistics")
    @PreAuthorize("@permissionCheckService.canViewCourseStudents(authentication.principal.id, #courseId)")
    public Result<Map<String, Object>> getCourseScoreStatistics(@PathVariable Long courseId) {
        Map<String, Object> statistics = scoreService.getCourseScoreStatistics(courseId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取成绩分布")
    @GetMapping("/course/{courseId}/distribution")
    @PreAuthorize("@permissionCheckService.canViewCourseStudents(authentication.principal.id, #courseId)")
    public Result<Map<String, Integer>> getScoreDistribution(@PathVariable Long courseId) {
        Map<String, Integer> distribution = scoreService.getScoreDistribution(courseId);
        return Result.success(distribution);
    }

    @Operation(summary = "删除成绩记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'COLLEGE_ADMIN')")
    public Result<Void> deleteScore(@PathVariable Long id) {
        try {
            // 检查成绩记录是否存在
            Optional<Score> existingScoreOpt = scoreService.findById(id);
            if (existingScoreOpt.isEmpty()) {
                return Result.error("成绩记录不存在");
            }

            // 如果是学院管理员，需要检查是否有权限删除该成绩
            if (SecurityUtils.isCollegeAdmin()) {
                Long userId = SecurityUtils.getCurrentUserId();
                Score existingScore = existingScoreOpt.get();
                if (!permissionCheckService.canInputCourseScore(userId, existingScore.getCourse().getId())) {
                    return Result.error("无权限删除该成绩记录");
                }
            }

            scoreService.deleteById(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}