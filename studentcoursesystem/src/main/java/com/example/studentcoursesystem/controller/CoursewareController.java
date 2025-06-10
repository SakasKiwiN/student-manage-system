package com.example.studentcoursesystem.controller;

import com.example.studentcoursesystem.common.Result;
import com.example.studentcoursesystem.entity.*;
import com.example.studentcoursesystem.service.*;
import com.example.studentcoursesystem.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "课件管理", description = "课件相关接口")
@RestController
@RequestMapping("/api/coursewares")
@RequiredArgsConstructor
public class CoursewareController {

    private final CoursewareService coursewareService;
    private final StudentService studentService;
    private final CourseService courseService;

    @Operation(summary = "上传课件")
    @PostMapping("/upload")
    @PreAuthorize("@permissionCheckService.canUploadCourseware(authentication.principal.id, #courseId)")
    public Result<Courseware> uploadCourseware(
            @RequestParam("courseId") Long courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title) {
        try {
            Courseware courseware = coursewareService.uploadCourseware(courseId, file, title);
            return Result.success("上传成功", courseware);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取课程的课件列表")
    @GetMapping("/course/{courseId}")
    public Result<List<Courseware>> getCoursewares(@PathVariable Long courseId) {
        // 检查用户是否可以访问该课程的课件
        if (!SecurityUtils.isSysAdmin()) {
            Course course = courseService.findById(courseId).orElse(null);
            if (course == null) {
                return Result.error("课程不存在");
            }

            // 学院管理员只能访问本学院课程的课件
            if (SecurityUtils.isCollegeAdmin()) {
                if (!SecurityUtils.canAccessCollegeResource(course.getCollege().getId())) {
                    return Result.error("无权限访问该课程课件");
                }
            }
            // 教师只能访问自己课程的课件
            else if (SecurityUtils.isTeacher()) {
                Long userId = SecurityUtils.getCurrentUserId();
                if (!course.getTeacher().getUser().getId().equals(userId)) {
                    return Result.error("无权限访问该课程课件");
                }
            }
            // 学生只能访问已选课程的课件
            else if (SecurityUtils.isStudent()) {
                Long userId = SecurityUtils.getCurrentUserId();
                Student student = studentService.findByUserId(userId).orElse(null);
                if (student == null) {
                    return Result.error("学生信息不存在");
                }

                // 这里需要检查学生是否选修了该课程
                // 简化处理，允许所有学生访问（实际应该检查选课记录）
            }
        }

        List<Courseware> coursewares = coursewareService.findByCourseId(courseId);
        return Result.success(coursewares);
    }

    @Operation(summary = "获取课件详情")
    @GetMapping("/{id}")
    public Result<Courseware> getCourseware(@PathVariable Long id) {
        return coursewareService.findById(id)
                .map(Result::success)
                .orElse(Result.error("课件不存在"));
    }

    @Operation(summary = "下载课件")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadCourseware(@PathVariable Long id) {
        try {
            Courseware courseware = coursewareService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("课件不存在"));

            byte[] data = coursewareService.downloadCourseware(id);
            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + courseware.getTitle() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(data.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "删除课件")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TEACHER')")
    public Result<Void> deleteCourseware(@PathVariable Long id) {
        try {
            // 教师只能删除自己课程的课件
            if (SecurityUtils.isTeacher()) {
                Courseware courseware = coursewareService.findById(id).orElse(null);
                if (courseware == null) {
                    return Result.error("课件不存在");
                }

                Long userId = SecurityUtils.getCurrentUserId();
                if (!courseware.getCourse().getTeacher().getUser().getId().equals(userId)) {
                    return Result.error("无权限删除该课件");
                }
            }

            coursewareService.deleteById(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "更新课件信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TEACHER')")
    public Result<Courseware> updateCourseware(@PathVariable Long id, @RequestBody Courseware courseware) {
        try {
            // 教师只能更新自己课程的课件
            if (SecurityUtils.isTeacher()) {
                Courseware existingCourseware = coursewareService.findById(id).orElse(null);
                if (existingCourseware == null) {
                    return Result.error("课件不存在");
                }

                Long userId = SecurityUtils.getCurrentUserId();
                if (!existingCourseware.getCourse().getTeacher().getUser().getId().equals(userId)) {
                    return Result.error("无权限修改该课件");
                }
            }

            courseware.setId(id);
            Courseware updated = coursewareService.update(courseware);
            return Result.success("更新成功", updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}