package com.expense.claim.repository;

import com.expense.claim.models.ExpenseHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseHeaderRepository extends JpaRepository<ExpenseHeader, Long> {

    // Custom method to find all headers by department
    List<ExpenseHeader> findByDepartmentId(Long departmentId);

    // Custom method to find all headers by requestor
    List<ExpenseHeader> findByRequestorId(Long requestorId);
}
