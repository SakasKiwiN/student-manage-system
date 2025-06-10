package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.Course;
import com.example.studentcoursesystem.entity.Courseware;
import com.example.studentcoursesystem.repository.CourseRepository;
import com.example.studentcoursesystem.repository.CoursewareRepository;
import com.example.studentcoursesystem.service.CoursewareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CoursewareServiceImpl implements CoursewareService {

    private final CoursewareRepository coursewareRepository;
    private final CourseRepository courseRepository;

    @Value("${file.upload.path:uploads/courseware}")
    private String uploadPath;

    @Override
    public Courseware uploadCourseware(@NonNull Long courseId, @NonNull MultipartFile file, @NonNull String title) {
        // 检查课程是否存在
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在: " + courseId));

        // 验证文件
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            try {
                Files.createDirectories(uploadDir);
            } catch (IOException e) {
                throw new RuntimeException("创建上传目录失败", e);
            }
        }

        // 保存文件
        Path filePath = uploadDir.resolve(fileName);
        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }

        // 创建课件记录
        Courseware courseware = new Courseware();
        courseware.setCourse(course);
        courseware.setTitle(title);
        courseware.setFilePath(filePath.toString());
        courseware.setFileSize(file.getSize());
        courseware.setUploadTime(LocalDateTime.now());

        Instant now = Instant.now();
        courseware.setGmtCreate(now);
        courseware.setGmtModified(now);

        Courseware saved = coursewareRepository.save(courseware);
        log.info("上传课件成功: 课程 {} - 文件 {}", course.getCourseCode(), title);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Courseware> findByCourseId(@NonNull Long courseId) {
        return coursewareRepository.findByCourseIdOrderByUploadTimeDesc(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Courseware> findById(@NonNull Long id) {
        return coursewareRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadCourseware(@NonNull Long id) {
        Courseware courseware = coursewareRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("课件不存在: " + id));

        try {
            Path filePath = Paths.get(courseware.getFilePath());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("文件不存在: " + courseware.getFilePath());
            }

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败", e);
        }
    }

    @Override
    public void deleteById(@NonNull Long id) {
        Courseware courseware = coursewareRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("课件不存在: " + id));

        // 删除文件
        try {
            Path filePath = Paths.get(courseware.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            log.error("删除文件失败: {}", courseware.getFilePath(), e);
        }

        // 删除记录
        coursewareRepository.deleteById(id);
        log.info("删除课件成功: {}", id);
    }

    @Override
    public Courseware update(@NonNull Courseware courseware) {
        if (!coursewareRepository.existsById(courseware.getId())) {
            throw new IllegalArgumentException("课件不存在: " + courseware.getId());
        }

        courseware.setGmtModified(Instant.now());
        Courseware updated = coursewareRepository.save(courseware);
        log.info("更新课件信息成功: {}", updated.getTitle());
        return updated;
    }
}