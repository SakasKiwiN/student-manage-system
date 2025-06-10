package com.example.studentcoursesystem.service.impl;

import com.example.studentcoursesystem.entity.Course;
import com.example.studentcoursesystem.entity.Message;
import com.example.studentcoursesystem.entity.User;
import com.example.studentcoursesystem.repository.CourseRepository;
import com.example.studentcoursesystem.repository.MessageRepository;
import com.example.studentcoursesystem.repository.UserRepository;
import com.example.studentcoursesystem.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Override
    public Message sendMessage(@NonNull Long senderId, @NonNull Long receiverId,
                               @NonNull String content, @Nullable Long courseId) {
        // 验证发送者和接收者
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("发送者不存在: " + senderId));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("接收者不存在: " + receiverId));

        // 如果有课程ID，验证课程是否存在
        Course course = null;
        if (courseId != null) {
            course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("课程不存在: " + courseId));
        }

        // 创建消息
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setCourse(course);
        message.setStatus(1); // 已发送

        Instant now = Instant.now();
        message.setGmtCreate(now);
        message.setGmtModified(now);

        Message saved = messageRepository.save(message);
        log.info("发送消息成功: {} -> {}", sender.getUsername(), receiver.getUsername());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findSentMessages(@NonNull Long userId) {
        return messageRepository.findBySenderIdOrderByGmtCreateDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findReceivedMessages(@NonNull Long userId) {
        return messageRepository.findByReceiverIdOrderByGmtCreateDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findUnreadMessages(@NonNull Long userId) {
        return messageRepository.findByReceiverIdAndStatus(userId, 1);
    }

    @Override
    public void markAsRead(@NonNull Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在: " + messageId));

        if (message.getStatus() != 2) {
            message.setStatus(2); // 已读
            message.setGmtModified(Instant.now());
            messageRepository.save(message);
            log.info("标记消息为已读: {}", messageId);
        }
    }

    @Override
    public void markAllAsRead(@NonNull Long userId) {
        List<Message> unreadMessages = messageRepository.findByReceiverIdAndStatus(userId, 1);
        Instant now = Instant.now();

        for (Message message : unreadMessages) {
            message.setStatus(2); // 已读
            message.setGmtModified(now);
        }

        messageRepository.saveAll(unreadMessages);
        log.info("标记用户 {} 的所有消息为已读: {} 条", userId, unreadMessages.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findByCourseId(@NonNull Long courseId) {
        return messageRepository.findByUserIdAndCourseId(null, courseId);
    }

    @Override
    public void deleteById(@NonNull Long id) {
        if (!messageRepository.existsById(id)) {
            throw new IllegalArgumentException("消息不存在: " + id);
        }

        messageRepository.deleteById(id);
        log.info("删除消息成功: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadMessages(@NonNull Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }
}