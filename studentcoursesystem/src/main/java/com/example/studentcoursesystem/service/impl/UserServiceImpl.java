package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.repository.UserRepository;
import com.example.studentcoursesystem.entity.User;
import com.example.studentcoursesystem.service.UserService;
import com.example.studentcoursesystem.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<UserVO> findAll() {
        return userRepository.findAll().stream().map(user -> {
            UserVO userVO = new UserVO();
            userVO.setId(user.getId());
            userVO.setUsername(user.getUsername());
            return userVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<User> getByUsername(@NonNull String username) {
        List<User> result = userRepository.getUserByUsername(username);
        return Optional.ofNullable(result.isEmpty() ? null : result.get(0));
    }

    @Override
    public boolean create(UserVO userVO) {
        String username = userVO.getUsername();
        String password = userVO.getPassword();
        /*由于使用了BCryptPasswordEncoder实现（在SecurityConfig中已经给出相关的bean定义），无需用盐*/
        // String salt = null;
        String encryptedPwd = passwordEncoder.encode(password);
        Instant now = Instant.now();
        User user = User.builder()
                .username(username)
                .password(encryptedPwd)
                // .salt(salt)
                .gmtCreate(now)
                .gmtModified(now)
                .build();
        /*记录操作时间*/
        userVO.setTimestamp(now.toEpochMilli());
        try {
            user = userRepository.save(user);
            userVO.setId(user.getId());
            return true;
        } catch (Exception e) {
            System.err.println("注册用户出错！");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User getById(Long id) {
        return userRepository.getOne(id);
    }
}