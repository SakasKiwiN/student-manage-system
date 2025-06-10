package com.example.studentcoursesystem.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class StudentDTO {
    @NotBlank(message = "学号不能为空")
    private String studentNo;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotNull(message = "学院ID不能为空")
    private Long collegeId;

    @NotBlank(message = "年级不能为空")
    private String grade;

    @NotBlank(message = "班级不能为空")
    private String className;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}