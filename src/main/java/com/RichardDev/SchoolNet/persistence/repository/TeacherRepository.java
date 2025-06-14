package com.RichardDev.SchoolNet.persistence.repository;

import com.RichardDev.SchoolNet.constant.Subject;
import com.RichardDev.SchoolNet.persistence.entity.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<TeacherEntity, Long> {
    @Query("SELECT s FROM TeacherEntity s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<TeacherEntity> searchByKeyword(String keyword);

    List<TeacherEntity> findByAreaOfExpertise(Subject areaOfExpertise);

    Optional<TeacherEntity> findByUsername(String username);



}
