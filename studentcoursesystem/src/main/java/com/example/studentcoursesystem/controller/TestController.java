package com.example.studentcoursesystem.controller;

import com.example.studentcoursesystem.entity.User;
import com.example.studentcoursesystem.service.UserService;
import com.example.studentcoursesystem.vo.JsonResult;
import com.example.studentcoursesystem.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class TestController {
    @Autowired
    private UserService userService;
    @GetMapping("/view/{id}")
    public JsonResult getUserInfo(@PathVariable("id") Long id) {
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setPassword(user.getPassword());
        userVO.setTimestamp(user.getGmtModified().toEpochMilli());
        return new JsonResult(JsonResult.JsonResultCode.SUCCESS, "获取数据成功！", userVO);
    }

    @GetMapping("/list")
    public JsonResult getUserList() {
        try {
            List<UserVO> userList = userService.findAll();
            return new JsonResult(JsonResult.JsonResultCode.SUCCESS, "获取数据成功！", userList);
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult(JsonResult.JsonResultCode.ERROR, "获取数据失败！", null);
        }
    }
}