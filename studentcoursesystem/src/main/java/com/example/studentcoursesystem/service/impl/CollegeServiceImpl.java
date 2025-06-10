package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.College;
import com.example.studentcoursesystem.repository.CollegeRepository;
import com.example.studentcoursesystem.service.CollegeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CollegeServiceImpl implements CollegeService {

    private final CollegeRepository collegeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<College> findAll() {
        return collegeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<College> findById(@NonNull Long id) {
        return collegeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<College> findByCode(@NonNull String code) {
        return collegeRepository.findByCode(code);
    }

    @Override
    public College create(@NonNull College college) {
        if (collegeRepository.existsByCode(college.getCode())) {
            throw new IllegalArgumentException("学院编码已存在: " + college.getCode());
        }

        Instant now = Instant.now();
        college.setGmtCreate(now);
        college.setGmtModified(now);

        College saved = collegeRepository.save(college);
        log.info("创建学院成功: {}", saved.getName());
        return saved;
    }

    @Override
    public College update(@NonNull College college) {
        if (!collegeRepository.existsById(college.getId())) {
            throw new IllegalArgumentException("学院不存在: " + college.getId());
        }

        college.setGmtModified(Instant.now());
        College updated = collegeRepository.save(college);
        log.info("更新学院成功: {}", updated.getName());
        return updated;
    }

    @Override
    public void deleteById(@NonNull Long id) {
        if (!collegeRepository.existsById(id)) {
            throw new IllegalArgumentException("学院不存在: " + id);
        }

        collegeRepository.deleteById(id);
        log.info("删除学院成功: {}", id);
    }
}