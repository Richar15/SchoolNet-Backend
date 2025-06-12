package com.RichardDev.SchoolNet.persistence.repository;

import com.RichardDev.SchoolNet.persistence.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, Long> {
    boolean existsByUsername(String username);
}
