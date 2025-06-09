package com.example.studentcoursesystem.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
    private Long id;
    private String username;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;
    private Long timestamp;
}