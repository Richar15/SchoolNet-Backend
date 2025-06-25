package com.RichardDev.SchoolNet.persistence.repository;



import com.RichardDev.SchoolNet.persistence.entity.ProfessorAssignmentEntity;
import com.RichardDev.SchoolNet.constant.Subject;
import com.RichardDev.SchoolNet.constant.Grade;
import com.RichardDev.SchoolNet.persistence.entity.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessorAssignmentRepository extends JpaRepository<ProfessorAssignmentEntity, Long> {
    List<ProfessorAssignmentEntity> findByProfessor(TeacherEntity professor);
    List<ProfessorAssignmentEntity> findBySubjectAndGrade(Subject subject, Grade grade);
    Optional<ProfessorAssignmentEntity> findByProfessorAndSubjectAndGrade(TeacherEntity professor, Subject subject, Grade grade);
}

