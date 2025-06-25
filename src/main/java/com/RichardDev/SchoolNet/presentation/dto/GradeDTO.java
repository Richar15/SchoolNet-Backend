package com.RichardDev.SchoolNet.presentation.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeDTO {
    private Long id;
    private Long studentId;
    private Long assignmentId;
    private Double grade1;
    private Double grade2;
    private Double grade3;
    private Double grade4;
    private Double finalGrade;
    private String subject;
}
