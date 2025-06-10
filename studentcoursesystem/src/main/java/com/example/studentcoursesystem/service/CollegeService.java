package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.College;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface CollegeService {
    /**
     * 获取所有学院
     */
    List<College> findAll();

    /**
     * 根据ID获取学院
     */
    Optional<College> findById(@NonNull Long id);

    /**
     * 根据编码获取学院
     */
    Optional<College> findByCode(@NonNull String code);

    /**
     * 创建学院
     */
    College create(@NonNull College college);

    /**
     * 更新学院
     */
    College update(@NonNull College college);

    /**
     * 删除学院
     */
    void deleteById(@NonNull Long id);
}