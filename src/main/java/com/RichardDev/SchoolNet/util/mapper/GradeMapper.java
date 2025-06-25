package com.RichardDev.SchoolNet.util.mapper;



import com.RichardDev.SchoolNet.persistence.entity.GradeEntity;
import com.RichardDev.SchoolNet.presentation.dto.GradeDTO;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {
    public GradeDTO toDTO(GradeEntity entity) {
        return GradeDTO.builder()
                .id(entity.getId())
                .studentId(entity.getStudent().getId())
                .assignmentId(entity.getAssignment().getId())
                .grade1(entity.getGrade1())
                .grade2(entity.getGrade2())
                .grade3(entity.getGrade3())
                .grade4(entity.getGrade4())
                .finalGrade(entity.getFinalGrade())
                .subject(entity.getAssignment().getSubject().name())

                .build();
    }
}