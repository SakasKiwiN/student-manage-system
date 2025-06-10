package com.example.studentcoursesystem.repository;

import com.example.studentcoursesystem.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderIdOrderByGmtCreateDesc(Long senderId);
    List<Message> findByReceiverIdOrderByGmtCreateDesc(Long receiverId);
    List<Message> findByReceiverIdAndStatus(Long receiverId, Integer status);

    @Query("SELECT m FROM Message m WHERE (m.sender.id = :userId OR m.receiver.id = :userId) " +
            "AND (:courseId IS NULL OR m.course.id = :courseId) ORDER BY m.gmtCreate DESC")
    List<Message> findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :receiverId AND m.status = 1")
    Long countUnreadMessages(@Param("receiverId") Long receiverId);
}