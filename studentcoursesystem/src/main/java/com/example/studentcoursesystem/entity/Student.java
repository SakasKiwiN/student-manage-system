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
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "student_no", nullable = false, unique = true, length = 20)
    private String studentNo;

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(nullable = false, length = 10)
    private String grade;

    @Column(name = "class", nullable = false, length = 20)
    private String className;

    @Column(name = "gmt_create")
    private Instant gmtCreate;

    @Column(name = "gmt_modified")
    private Instant gmtModified;
}