package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.Student;
import com.example.studentcoursesystem.repository.StudentRepository;
import com.example.studentcoursesystem.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> findById(@NonNull Long id) {
        return studentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> findByStudentNo(@NonNull String studentNo) {
        return studentRepository.findByStudentNo(studentNo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> findByUserId(@NonNull Long userId) {
        return studentRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findByCollegeId(@NonNull Long collegeId) {
        return studentRepository.findByCollegeId(collegeId);
    }

    @Override
    public Student create(@NonNull Student student) {
        if (studentRepository.existsByStudentNo(student.getStudentNo())) {
            throw new IllegalArgumentException("学号已存在: " + student.getStudentNo());
        }

        Instant now = Instant.now();
        student.setGmtCreate(now);
        student.setGmtModified(now);

        Student saved = studentRepository.save(student);
        log.info("创建学生成功: {} - {}", saved.getStudentNo(), saved.getName());
        return saved;
    }

    @Override
    public Student update(@NonNull Student student) {
        if (!studentRepository.existsById(student.getId())) {
            throw new IllegalArgumentException("学生不存在: " + student.getId());
        }

        student.setGmtModified(Instant.now());
        Student updated = studentRepository.save(student);
        log.info("更新学生信息成功: {} - {}", updated.getStudentNo(), updated.getName());
        return updated;
    }

    @Override
    public void deleteById(@NonNull Long id) {
        if (!studentRepository.existsById(id)) {
            throw new IllegalArgumentException("学生不存在: " + id);
        }

        studentRepository.deleteById(id);
        log.info("删除学生成功: {}", id);
    }

    @Override
    public List<Student> batchCreate(@NonNull List<Student> students) {
        List<Student> savedStudents = new ArrayList<>();
        Instant now = Instant.now();

        for (Student student : students) {
            if (studentRepository.existsByStudentNo(student.getStudentNo())) {
                log.warn("批量导入跳过已存在学号: {}", student.getStudentNo());
                continue;
            }

            student.setGmtCreate(now);
            student.setGmtModified(now);
            savedStudents.add(student);
        }

        List<Student> saved = studentRepository.saveAll(savedStudents);
        log.info("批量导入学生成功: {} 条", saved.size());
        return saved;
    }
}