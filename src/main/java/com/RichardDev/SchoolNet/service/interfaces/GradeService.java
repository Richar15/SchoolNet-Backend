package com.RichardDev.SchoolNet.service.interfaces;

import com.RichardDev.SchoolNet.presentation.dto.GradeDTO;

import java.util.List;

public interface GradeService {
    GradeDTO assignGrade(Long professorId, GradeDTO gradeDTO);
    List<GradeDTO> getGradesByStudentId(Long studentId);
}
