package com.example.studentcoursesystem.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class PrerequisiteDTO {
    @NotNull(message = "先修课程ID列表不能为空")
    private List<Long> prerequisiteIds;
}