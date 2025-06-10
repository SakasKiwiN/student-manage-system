package com.example.studentcoursesystem.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class MessageDTO {
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private Long courseId;
}