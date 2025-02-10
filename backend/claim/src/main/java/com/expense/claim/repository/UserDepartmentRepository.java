package com.expense.claim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.expense.claim.models.UserDepartment;

import java.util.Optional;

@Repository
public interface UserDepartmentRepository extends JpaRepository<UserDepartment, Long> {
    
    // Find UserDepartment by user ID
    Optional<UserDepartment> findByUserId(Long userId);

    // Find UserDepartment by department ID
    Optional<UserDepartment> findByDepartmentId(Long departmentId);
}
