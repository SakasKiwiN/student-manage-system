package com.example.studentcoursesystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "teacher")
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "teacher_no", nullable = false, unique = true, length = 20)
    private String teacherNo;

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(name = "gmt_create")
    private Instant gmtCreate;

    @Column(name = "gmt_modified")
    private Instant gmtModified;
}