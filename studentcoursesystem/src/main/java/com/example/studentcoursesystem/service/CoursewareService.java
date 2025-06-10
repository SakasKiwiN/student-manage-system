package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.Courseware;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface CoursewareService {
    /**
     * 上传课件
     */
    Courseware uploadCourseware(@NonNull Long courseId, @NonNull MultipartFile file, @NonNull String title);

    /**
     * 获取课程的所有课件
     */
    List<Courseware> findByCourseId(@NonNull Long courseId);

    /**
     * 获取课件详情
     */
    Optional<Courseware> findById(@NonNull Long id);

    /**
     * 下载课件
     */
    byte[] downloadCourseware(@NonNull Long id);

    /**
     * 删除课件
     */
    void deleteById(@NonNull Long id);

    /**
     * 更新课件信息
     */
    Courseware update(@NonNull Courseware courseware);
}