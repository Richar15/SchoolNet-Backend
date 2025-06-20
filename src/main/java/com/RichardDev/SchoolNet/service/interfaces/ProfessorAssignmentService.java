package com.RichardDev.SchoolNet.service.interfaces;

import com.RichardDev.SchoolNet.persistence.entity.ProfessorAssignmentEntity;
import com.RichardDev.SchoolNet.persistence.entity.TeacherEntity;

import java.util.List;

public interface ProfessorAssignmentService {

    List<ProfessorAssignmentEntity> getAssignmentsForProfessor(Long professorId);
    void distributeAssignments();
    void assignOnNewProfessor(TeacherEntity newProfessor);


}
