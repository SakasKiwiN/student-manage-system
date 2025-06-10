package com.example.studentcoursesystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_code", nullable = false, unique = true, length = 20)
    private String courseCode;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @Column(nullable = false)
    private Integer credits;

    @Column(name = "max_students", nullable = false)
    private Integer maxStudents;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer status = 1; // 0-停用，1-启用

    @ManyToMany
    @JoinTable(
            name = "course_prerequisite",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_id")
    )
    private List<Course> prerequisites;

    @Column(name = "gmt_create")
    private Instant gmtCreate;

    @Column(name = "gmt_modified")
    private Instant gmtModified;
}