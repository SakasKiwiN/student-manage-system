package com.example.studentcoursesystem.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class CourseSelectionDTO {
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
}