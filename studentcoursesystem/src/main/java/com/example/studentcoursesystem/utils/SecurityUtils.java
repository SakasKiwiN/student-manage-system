package com.example.studentcoursesystem.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;

// 添加这些新的import语句
import java.util.Collection;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

import com.example.studentcoursesystem.entity.College;
import com.example.studentcoursesystem.entity.Course;
import com.example.studentcoursesystem.entity.Student;
import com.example.studentcoursesystem.entity.Teacher;

import com.example.studentcoursesystem.service.CollegeService;
import com.example.studentcoursesystem.service.CourseService;
import com.example.studentcoursesystem.service.StudentService;
import com.example.studentcoursesystem.service.TeacherService;
/**
 * Spring Security 工具类
 * 提供获取当前用户信息的便捷方法
 */
public class SecurityUtils {
    // 在SecurityUtils.java中添加以下方法：

    /**
     * 获取当前用户所属的学院ID（仅对学院管理员有效）
     */
    public static Long getCurrentUserCollegeId() {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }

        // 根据用户名规则判断所属学院
        if (username.equals("cs_admin")) {
            return 1L; // 计算机学院ID
        } else if (username.equals("ee_admin")) {
            return 2L; // 电子工程学院ID
        }

        // 对于教师和学生，需要通过查询获取
        if (isTeacher()) {
            Teacher teacher = getTeacherByUserId(getCurrentUserId());
            return teacher != null ? teacher.getCollege().getId() : null;
        } else if (isStudent()) {
            Student student = getStudentByUserId(getCurrentUserId());
            return student != null ? student.getCollege().getId() : null;
        }

        return null;
    }

    /**
     * 检查当前用户是否可以访问指定学院的资源
     */
    public static boolean canAccessCollegeResource(Long collegeId) {
        // 系统管理员可以访问所有学院资源
        if (isSysAdmin()) {
            return true;
        }

        // 学院管理员只能访问自己学院的资源
        if (isCollegeAdmin()) {
            Long userCollegeId = getCurrentUserCollegeId();
            return userCollegeId != null && userCollegeId.equals(collegeId);
        }

        // 教师只能访问自己学院的资源
        if (isTeacher()) {
            Long userCollegeId = getCurrentUserCollegeId();
            return userCollegeId != null && userCollegeId.equals(collegeId);
        }

        return false;
    }

    /**
     * 检查当前用户是否可以管理指定学院的学生
     */
    public static boolean canManageCollegeStudents(Long collegeId) {
        return isSysAdmin() || (isCollegeAdmin() && canAccessCollegeResource(collegeId));
    }

    /**
     * 检查当前用户是否可以管理指定学院的教师
     */
    public static boolean canManageCollegeTeachers(Long collegeId) {
        return isSysAdmin() || (isCollegeAdmin() && canAccessCollegeResource(collegeId));
    }

    /**
     * 检查当前用户是否可以管理指定课程
     */
    public static boolean canManageCourse(Long courseId) {
        if (isSysAdmin()) {
            return true;
        }

        try {
            // 通过ApplicationContext获取CourseService
            CourseService courseService = ApplicationContextUtils.getBeanSafely(CourseService.class);
            if (courseService == null) {
                return false;
            }

            Course course = courseService.findById(courseId).orElse(null);
            if (course == null) {
                return false;
            }

            // 学院管理员可以管理本学院的课程
            if (isCollegeAdmin()) {
                return canAccessCollegeResource(course.getCollege().getId());
            }

            // 教师只能管理自己的课程
            if (isTeacher()) {
                Long userId = getCurrentUserId();
                return course.getTeacher().getUser().getId().equals(userId);
            }

        } catch (Exception e) {
            return false;
        }

        return false;
    }

    /**
     * 获取当前用户可访问的学院ID列表
     */
    public static List<Long> getAccessibleCollegeIds() {
        if (isSysAdmin()) {
            // 系统管理员可以访问所有学院
            try {
                CollegeService collegeService = ApplicationContextUtils.getBeanSafely(CollegeService.class);
                if (collegeService != null) {
                    return collegeService.findAll().stream()
                            .map(College::getId)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }

        // 学院管理员、教师、学生只能访问自己的学院
        Long collegeId = getCurrentUserCollegeId();
        return collegeId != null ? List.of(collegeId) : Collections.emptyList();
    }

    // 辅助方法 - 通过用户ID获取教师信息
    private static Teacher getTeacherByUserId(Long userId) {
        try {
            TeacherService teacherService = ApplicationContextUtils.getBeanSafely(TeacherService.class);
            return teacherService != null ? teacherService.findByUserId(userId).orElse(null) : null;
        } catch (Exception e) {
            return null;
        }
    }

    // 辅助方法 - 通过用户ID获取学生信息
    private static Student getStudentByUserId(Long userId) {
        try {
            StudentService studentService = ApplicationContextUtils.getBeanSafely(StudentService.class);
            return studentService != null ? studentService.findByUserId(userId).orElse(null) : null;
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * 获取当前登录用户的Authentication对象
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录用户的用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

    /**
     * 获取当前用户的主要角色
     * 按优先级返回：SYS_ADMIN > COLLEGE_ADMIN > TEACHER > STUDENT
     */
    public static String getCurrentUserRole() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "ANONYMOUS";
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // 按优先级检查角色
        if (authorities.stream().anyMatch(auth ->
                auth.getAuthority().equals("ROLE_SYS_ADMIN") || auth.getAuthority().equals("SYS_ADMIN"))) {
            return "SYS_ADMIN";
        }

        if (authorities.stream().anyMatch(auth ->
                auth.getAuthority().equals("ROLE_COLLEGE_ADMIN") || auth.getAuthority().equals("COLLEGE_ADMIN"))) {
            return "COLLEGE_ADMIN";
        }

        if (authorities.stream().anyMatch(auth ->
                auth.getAuthority().equals("ROLE_TEACHER") || auth.getAuthority().equals("TEACHER"))) {
            return "TEACHER";
        }

        if (authorities.stream().anyMatch(auth ->
                auth.getAuthority().equals("ROLE_STUDENT") || auth.getAuthority().equals("STUDENT"))) {
            return "STUDENT";
        }

        // 如果没有匹配的角色，返回第一个权限
        return authorities.isEmpty() ? "UNKNOWN" : authorities.iterator().next().getAuthority();
    }

    /**
     * 获取当前用户的所有角色（字符串格式）
     */
    public static String getCurrentUserRoles() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "[]";
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.toString();
    }

    /**
     * 获取当前登录用户ID
     * 注意：这里假设用户名就是用户ID或者可以通过用户名查询到用户ID
     * 实际项目中可能需要从JWT Token中获取或通过其他方式获取
     */
    public static Long getCurrentUserId() {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }

        // 方式1：如果用户名是数字，直接转换（适用于学号登录的情况）
        try {
            return Long.parseLong(username);
        } catch (NumberFormatException e) {
            // 用户名不是数字，需要通过其他方式获取用户ID
        }

        // 方式2：通过ApplicationContext获取UserService来查询
        // 这里提供一个更安全的实现
        return getUserIdByUsername(username);
    }

    /**
     * 通过用户名获取用户ID
     * 这个方法需要根据实际项目的用户ID获取策略来实现
     */
    private static Long getUserIdByUsername(String username) {

        // 方式：通过缓存或数据库查询（推荐）
        return getUserIdFromDatabase(username);
    }

    /**
     * 从数据库获取用户ID（推荐方式）
     * 这个方法需要依赖UserService，但为了避免循环依赖，
     * 可以通过ApplicationContextUtils来获取bean
     */
    private static Long getUserIdFromDatabase(String username) {
        try {
            // 使用ApplicationContext获取UserService
            // UserService userService = ApplicationContextUtils.getBean(UserService.class);
            // Optional<User> user = userService.getByUsername(username);
            // return user.map(User::getId).orElse(null);

            // 临时实现：根据用户名模式判断
            if (username.matches("\\d+")) {
                // 如果是纯数字，可能是学号，假设用户ID与学号相关
                // 这里需要根据实际业务逻辑调整
                return parseUserIdFromStudentNo(username);
            } else if (username.startsWith("teacher_")) {
                // 教师用户名，需要查询数据库
                return parseUserIdFromTeacherUsername(username);
            } else if (username.contains("admin")) {
                // 管理员用户名
                return parseUserIdFromAdminUsername(username);
            }

            // 默认返回1（用于测试），实际应该返回null或抛出异常
            return 1L;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从学号解析用户ID
     */
    private static Long parseUserIdFromStudentNo(String studentNo) {
        // 根据数据库设计，学号为2021001的学生用户ID可能是7
        // 这里需要根据实际的用户ID分配策略来实现
        switch (studentNo) {
            case "2021001": return 7L;
            case "2021002": return 8L;
            case "2021003": return 9L;
            case "2021004": return 10L;
            default: return null;
        }
    }

    /**
     * 从教师用户名解析用户ID
     */
    private static Long parseUserIdFromTeacherUsername(String username) {
        switch (username) {
            case "teacher_wang": return 4L;
            case "teacher_li": return 5L;
            case "teacher_zhang": return 6L;
            default: return null;
        }
    }

    /**
     * 从管理员用户名解析用户ID
     */
    private static Long parseUserIdFromAdminUsername(String username) {
        switch (username) {
            case "sysadmin": return 1L;
            case "cs_admin": return 2L;
            case "ee_admin": return 3L;
            default: return null;
        }
    }

    /**
     * 判断是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 判断当前用户是否有指定角色
     * @param role 角色名，不需要ROLE_前缀
     */
    public static boolean hasRole(String role) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roleWithPrefix = "ROLE_" + role;

        return authorities.stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(roleWithPrefix) ||
                                authority.getAuthority().equals(role));
    }

    /**
     * 判断当前用户是否有指定权限
     */
    public static boolean hasPermission(String permission) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(permission));
    }

    /**
     * 获取当前用户的所有权限
     */
    public static Collection<? extends GrantedAuthority> getCurrentUserAuthorities() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return null;
        }
        return authentication.getAuthorities();
    }

    /**
     * 判断当前用户是否是系统管理员
     */
    public static boolean isSysAdmin() {
        return hasRole("SYS_ADMIN");
    }

    /**
     * 判断当前用户是否是学院管理员
     */
    public static boolean isCollegeAdmin() {
        return hasRole("COLLEGE_ADMIN");
    }

    /**
     * 判断当前用户是否是教师
     */
    public static boolean isTeacher() {
        return hasRole("TEACHER");
    }

    /**
     * 判断当前用户是否是学生
     */
    public static boolean isStudent() {
        return hasRole("STUDENT");
    }




}