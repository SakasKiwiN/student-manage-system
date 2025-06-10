package com.example.studentcoursesystem.controller;

import com.example.studentcoursesystem.common.Result;
import com.example.studentcoursesystem.dto.ChangePasswordDTO;
import com.example.studentcoursesystem.entity.User;
import com.example.studentcoursesystem.service.UserService;
import com.example.studentcoursesystem.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "认证管理", description = "登录认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        try {
            if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
                return Result.error("两次输入的密码不一致");
            }

            String username = SecurityUtils.getCurrentUsername();
            if (username == null) {
                return Result.error("用户未登录");
            }

            User user = userService.getByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 验证旧密码
            if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                return Result.error("原密码错误");
            }

            // 更新密码
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            userService.save(user);

            return Result.success("密码修改成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            return Result.error("未登录");
        }

        return userService.getByUsername(username)
                .map(Result::success)
                .orElse(Result.error("用户不存在"));
    }

    @Operation(summary = "检查登录状态")
    @GetMapping("/status")
    public Result<String> getAuthStatus() {
        if (SecurityUtils.isAuthenticated()) {
            return Result.success("已登录", SecurityUtils.getCurrentUsername());
        } else {
            return Result.error("未登录");
        }
    }
}