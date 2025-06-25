package com.RichardDev.SchoolNet.persistence.repository;

import com.RichardDev.SchoolNet.constant.Grade;
import com.RichardDev.SchoolNet.persistence.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    Optional<StudentEntity> findByUsername(String username);

    @Query("SELECT s FROM StudentEntity s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<StudentEntity> searchByKeyword(String keyword);

    List<StudentEntity> findByGrade(Grade grade);

}
