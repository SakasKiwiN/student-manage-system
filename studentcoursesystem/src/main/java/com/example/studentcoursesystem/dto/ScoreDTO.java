package com.example.studentcoursesystem.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class ScoreDTO {
    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0", message = "成绩不能小于0")
    @DecimalMax(value = "100", message = "成绩不能大于100")
    @Digits(integer = 3, fraction = 2, message = "成绩格式不正确")
    private BigDecimal score;
}