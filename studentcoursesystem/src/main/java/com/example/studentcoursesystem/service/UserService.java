package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.User;
import com.example.studentcoursesystem.vo.UserVO;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserVO> findAll();

    /**
     * 通过用户名获取账户
     */
    Optional<User> getByUsername(@NonNull String username);

    /**
     * 创建用户账户
     */
    boolean create(@NonNull UserVO userVO);

    /**
     * 根据ID获取用户
     */
    User getById(@NonNull Long id);

    /**
     * 根据ID查找用户
     */
    Optional<User> findById(@NonNull Long id);

    /**
     * 保存用户
     */
    User save(@NonNull User user);

    /**
     * 删除用户
     */
    void deleteById(@NonNull Long id);
}