package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.Teacher;
import com.example.studentcoursesystem.repository.TeacherRepository;
import com.example.studentcoursesystem.service.TeacherService;
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
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Teacher> findAll() {
        return teacherRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Teacher> findById(@NonNull Long id) {
        return teacherRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Teacher> findByTeacherNo(@NonNull String teacherNo) {
        return teacherRepository.findByTeacherNo(teacherNo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Teacher> findByUserId(@NonNull Long userId) {
        return teacherRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Teacher> findByCollegeId(@NonNull Long collegeId) {
        return teacherRepository.findByCollegeId(collegeId);
    }

    @Override
    public Teacher create(@NonNull Teacher teacher) {
        if (teacherRepository.existsByTeacherNo(teacher.getTeacherNo())) {
            throw new IllegalArgumentException("教师编号已存在: " + teacher.getTeacherNo());
        }

        Instant now = Instant.now();
        teacher.setGmtCreate(now);
        teacher.setGmtModified(now);

        Teacher saved = teacherRepository.save(teacher);
        log.info("创建教师成功: {} - {}", saved.getTeacherNo(), saved.getName());
        return saved;
    }

    @Override
    public Teacher update(@NonNull Teacher teacher) {
        if (!teacherRepository.existsById(teacher.getId())) {
            throw new IllegalArgumentException("教师不存在: " + teacher.getId());
        }

        teacher.setGmtModified(Instant.now());
        Teacher updated = teacherRepository.save(teacher);
        log.info("更新教师信息成功: {} - {}", updated.getTeacherNo(), updated.getName());
        return updated;
    }

    @Override
    public void deleteById(@NonNull Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new IllegalArgumentException("教师不存在: " + id);
        }

        teacherRepository.deleteById(id);
        log.info("删除教师成功: {}", id);
    }
}