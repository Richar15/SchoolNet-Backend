package com.RichardDev.SchoolNet.persistence.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "grades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @ManyToOne @JoinColumn(name = "assignment_id", nullable = false)
    private ProfessorAssignmentEntity assignment;

    @Column(nullable = false)
    private Double grade1;
    @Column(nullable = false)
    private Double grade2;
    @Column(nullable = false)
    private Double grade3;
    @Column(nullable = false)
    private Double grade4;
    @Column(nullable = false)
    private Double finalGrade;
}