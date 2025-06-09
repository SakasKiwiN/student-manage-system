package com.example.studentcoursesystem.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JsonResult {
    private Integer code;
    private String msg;
    private Object data;
    public interface JsonResultCode{
        Integer ERROR = -1;
        Integer SUCCESS = 1;
    }
}