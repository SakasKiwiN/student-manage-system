package com.example.studentcoursesystem.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class CourseDTO {
    @NotBlank(message = "课程编码不能为空")
    private String courseCode;

    @NotBlank(message = "课程名称不能为空")
    private String name;

    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    @NotNull(message = "学院ID不能为空")
    private Long collegeId;

    @NotNull(message = "学分不能为空")
    @Min(value = 1, message = "学分必须大于0")
    private Integer credits;

    @NotNull(message = "最大学生数不能为空")
    @Min(value = 1, message = "最大学生数必须大于0")
    private Integer maxStudents;

    private String description;

    private Integer status = 1;

    private List<Long> prerequisiteIds;
}