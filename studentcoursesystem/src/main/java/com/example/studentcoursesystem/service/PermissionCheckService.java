package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.*;
import com.example.studentcoursesystem.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限检查服务类 - 实现细粒度权限控制
 */
@Service
@RequiredArgsConstructor
public class PermissionCheckService {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final CourseService courseService;
    private final CourseSelectionService courseSelectionService;

    // ==================== 学院级别权限检查 ====================

    /**
     * 检查是否可以访问所有学生列表
     */
    public boolean canAccessAllStudents(Long userId) {
        return SecurityUtils.isSysAdmin();
    }

    /**
     * 检查是否可以管理指定学院的学生
     */
    public boolean canManageCollegeStudents(Long userId, Long collegeId) {
        if (SecurityUtils.isSysAdmin()) {
            return true;
        }

        if (SecurityUtils.isCollegeAdmin()) {
            return SecurityUtils.canAccessCollegeResource(collegeId);
        }

        return false;
    }

    /**
     * 检查是否可以查看指定学生信息
     */
    public boolean canViewStudent(Long userId, Long studentId) {
        if (SecurityUtils.isSysAdmin()) {
            return true;
        }

        // 学生只能查看自己
        if (SecurityUtils.isStudent()) {
            Student student = studentService.findByUserId(userId).orElse(null);
            return student != null && student.getId().equals(studentId);
        }

        // 学院管理员和教师可以查看本学院学生
        if (SecurityUtils.isCollegeAdmin() || SecurityUtils.isTeacher()) {
            Student student = studentService.findById(studentId).orElse(null);
            if (student != null) {
                return SecurityUtils.canAccessCollegeResource(student.getCollege().getId());
            }
        }

        return false;
    }

    /**
     * 检查是否可以管理指定学院的教师
     */
    public boolean canManageCollegeTeachers(Long userId, Long collegeId) {
        if (SecurityUtils.isSysAdmin()) {
            return true;
        }

        if (SecurityUtils.isCollegeAdmin()) {
            return SecurityUtils.canAccessCollegeResource(collegeId);
        }

        return false;
    }

    /**
     * 检查是否可以查看指定教师信息
     */
    public boolean canViewTeacher(Long userId, Long teacherId) {
        if (SecurityUtils.isSysAdmin()) {
            return true;
        }

        // 教师可以查看自己
        if (SecurityUtils.isTeacher()) {
            Teacher teacher = teacherService.findByUserId(userId).orElse(null);
            return teacher != null && teacher.getId().equals(teacherId);
        }

        // 学院管理员可以查看本学院教师
        if (SecurityUtils.isCollegeAdmin()) {
            Teacher teacher = teacherService.findById(teacherId).orElse(null);
            if (teacher != null) {
                return SecurityUtils.canAccessCollegeResource(teacher.getCollege().getId());
            }
        }

        return false;
    }

    // ==================== 课程级别权限检查 ====================

    /**
     * 检查是否可以管理指定课程
     */
    public boolean canManageCourse(Long userId, Long courseId) {
        if (SecurityUtils.isSysAdmin()) {
            return true;
        }

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) {
            return false;
        }

        // 学院管理员可以管理本学院课程
        if (SecurityUtils.isCollegeAdmin()) {
            return SecurityUtils.canAccessCollegeResource(course.getCollege().getId());
        }

        // 教师可以管理自己的课程
        if (SecurityUtils.isTeacher()) {
            return course.getTeacher().getUser().getId().equals(userId);
        }

        return false;
    }

    /**
     * 检查是否可以查看课程学生列表
     */
    public boolean canViewCourseStudents(Long userId, Long courseId) {
        if (SecurityUtils.isSysAdmin()) {
            return true;
        }

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) {
            return false;
        }

        // 学院管理员可以查看本学院课程的学生
        if (SecurityUtils.isCollegeAdmin()) {
            return SecurityUtils.canAccessCollegeResource(course.getCollege().getId());
        }

        // 教师可以查看自己课程的学生
        if (SecurityUtils.isTeacher()) {
            return course.getTeacher().getUser().getId().equals(userId);
        }

        return false;
    }

    /**
     * 检查是否可以上传课程课件
     */
    public boolean canUploadCourseware(Long userId, Long courseId) {
        if (SecurityUtils.isSysAdmin()) {
            return true;
        }

        // 只有课程的授课教师可以上传课件
        if (SecurityUtils.isTeacher()) {
            Course course = courseService.findById(courseId).orElse(null);
            return course != null && course.getTeacher().getUser().getId().equals(userId);
        }

        return false;
    }

    // ==================== 成绩管理权限检查 ====================

    /**
     * 检查是否可以录入指定课程的成绩
     */
    public boolean canInputCourseScore(Long userId, Long courseId) {
        if (SecurityUtils.isSysAdmin()) {
            return true;
        }

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) {
            return false;
        }

        // 学院管理员可以录入本学院课程成绩
        if (SecurityUtils.isCollegeAdmin()) {
            return SecurityUtils.canAccessCollegeResource(course.getCollege().getId());
        }

        // 教师只能录入自己课程的成绩
        if (SecurityUtils.isTeacher()) {
            return course.getTeacher().getUser().getId().equals(userId);
        }

        return false;
    }

    /**
     * 检查是否可以查看指定学生的成绩
     */
    public boolean canViewStudentScore(Long userId, Long studentId) {
        if (SecurityUtils.isSysAdmin()) {
            return true;
        }

        // 学生只能查看自己的成绩
        if (SecurityUtils.isStudent()) {
            Student student = studentService.findByUserId(userId).orElse(null);
            return student != null && student.getId().equals(studentId);
        }

        // 学院管理员可以查看本学院学生成绩
        if (SecurityUtils.isCollegeAdmin()) {
            Student student = studentService.findById(studentId).orElse(null);
            if (student != null) {
                return SecurityUtils.canAccessCollegeResource(student.getCollege().getId());
            }
        }

        // 教师可以查看选修自己课程的学生成绩
        if (SecurityUtils.isTeacher()) {
            Teacher teacher = teacherService.findByUserId(userId).orElse(null);
            if (teacher != null) {
                Student student = studentService.findById(studentId).orElse(null);
                if (student != null) {
                    // 检查教师是否教授该学生的任何课程
                    List<Course> teacherCourses = courseService.findByTeacherId(teacher.getId());
                    return teacherCourses.stream().anyMatch(course ->
                            hasStudentSelectedCourse(student.getId(), course.getId()));
                }
            }
        }

        return false;
    }

    // ==================== 选课管理权限检查 ====================

    /**
     * 检查是否可以执行课程抽签
     */
    public boolean canPerformCourseLottery(Long userId, Long courseId) {
        if (SecurityUtils.isSysAdmin()) {
            return true;
        }

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) {
            return false;
        }

        // 学院管理员可以对本学院课程进行抽签
        if (SecurityUtils.isCollegeAdmin()) {
            return SecurityUtils.canAccessCollegeResource(course.getCollege().getId());
        }

        // 教师可以对自己的课程进行抽签
        if (SecurityUtils.isTeacher()) {
            return course.getTeacher().getUser().getId().equals(userId);
        }

        return false;
    }

    /**
     * 检查是否可以执行学院批量抽签
     */
    public boolean canPerformCollegeLottery(Long userId, Long collegeId) {
        if (SecurityUtils.isSysAdmin()) {
            return true;
        }

        // 只有学院管理员可以执行本学院的批量抽签
        if (SecurityUtils.isCollegeAdmin()) {
            return SecurityUtils.canAccessCollegeResource(collegeId);
        }

        return false;
    }

    // ==================== 消息管理权限检查 ====================

    /**
     * 检查是否可以向指定用户发送消息
     */
    public boolean canSendMessageToUser(Long senderId, Long receiverId) {
        if (SecurityUtils.isSysAdmin()) {
            return true;
        }

        // 教师可以向选修自己课程的学生发送消息
        if (SecurityUtils.isTeacher()) {
            Teacher teacher = teacherService.findByUserId(senderId).orElse(null);
            Student student = studentService.findByUserId(receiverId).orElse(null);

            if (teacher != null && student != null) {
                List<Course> teacherCourses = courseService.findByTeacherId(teacher.getId());
                return teacherCourses.stream().anyMatch(course ->
                        hasStudentSelectedCourse(student.getId(), course.getId()));
            }
        }

        // 学生可以向任课教师发送消息
        if (SecurityUtils.isStudent()) {
            Student student = studentService.findByUserId(senderId).orElse(null);
            Teacher teacher = teacherService.findByUserId(receiverId).orElse(null);

            if (student != null && teacher != null) {
                List<Course> teacherCourses = courseService.findByTeacherId(teacher.getId());
                return teacherCourses.stream().anyMatch(course ->
                        hasStudentSelectedCourse(student.getId(), course.getId()));
            }
        }

        return false;
    }

    // ==================== 辅助方法 ====================

    /**
     * 检查学生是否选修了指定课程
     */
    private boolean hasStudentSelectedCourse(Long studentId, Long courseId) {
        return courseSelectionService.findByStudentIdAndCourseId(studentId, courseId).isPresent();
    }

    /**
     * 获取用户可以访问的学院ID列表
     */
    public List<Long> getAccessibleCollegeIds(Long userId) {
        return SecurityUtils.getAccessibleCollegeIds();
    }
}