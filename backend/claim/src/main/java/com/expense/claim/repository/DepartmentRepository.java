package com.expense.claim.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.expense.claim.models.Department;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    // Find a department by name
    Optional<Department> findByName(String name);
    
    // Find a department by its head's user ID
    Optional<Department> findByHeadId(Long headId);
}
