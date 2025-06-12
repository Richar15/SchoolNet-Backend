package com.RichardDev.SchoolNet.persistence.repository;

import com.RichardDev.SchoolNet.constant.Grade;
import com.RichardDev.SchoolNet.persistence.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {

    List<ScheduleEntity> findByGrade(Grade grade);

    Optional<ScheduleEntity> findTopByGradeOrderByIdDesc(Grade grade);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.grade = :grade ORDER BY s.id DESC")
    List<ScheduleEntity> findByGradeOrderByIdDesc(@Param("grade") Grade grade);

    @Query("SELECT COUNT(s) FROM ScheduleEntity s WHERE s.grade = :grade")
    long countByGrade(@Param("grade") Grade grade);

    @Query("SELECT DISTINCT s.grade FROM ScheduleEntity s ORDER BY s.grade")
    List<Grade> findDistinctGrades();

    void deleteByGrade(Grade grade);

    boolean existsByGrade(Grade grade);
}