package com.example.studentcoursesystem.service;

import com.example.studentcoursesystem.entity.Message;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface MessageService {
    /**
     * 发送消息
     */
    Message sendMessage(@NonNull Long senderId, @NonNull Long receiverId, @NonNull String content, @Nullable Long courseId);

    /**
     * 获取发送的消息
     */
    List<Message> findSentMessages(@NonNull Long userId);

    /**
     * 获取接收的消息
     */
    List<Message> findReceivedMessages(@NonNull Long userId);

    /**
     * 获取未读消息
     */
    List<Message> findUnreadMessages(@NonNull Long userId);

    /**
     * 标记消息为已读
     */
    void markAsRead(@NonNull Long messageId);

    /**
     * 批量标记为已读
     */
    void markAllAsRead(@NonNull Long userId);

    /**
     * 获取与课程相关的消息
     */
    List<Message> findByCourseId(@NonNull Long courseId);

    /**
     * 删除消息
     */
    void deleteById(@NonNull Long id);

    /**
     * 获取未读消息数量
     */
    Long countUnreadMessages(@NonNull Long userId);
}