package com.RichardDev.SchoolNet.persistence.repository;




import com.RichardDev.SchoolNet.persistence.entity.GradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GradeRepository extends JpaRepository<GradeEntity, Long> {

    List<GradeEntity> findByStudentId(Long studentId);

    @Query("SELECT g FROM GradeEntity g WHERE g.assignment.professor.id = :professorId")
    List<GradeEntity> findGradesByProfessorId(@Param("professorId") Long professorId);

}