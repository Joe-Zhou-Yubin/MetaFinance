package com.expense.claim.repository;

import com.expense.claim.models.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByDepartmentId(Long departmentId);
    List<Budget> findByApprovedTrue();
    Optional<Budget> findTopByDepartmentIdAndApprovedOrderByUpdatedAtDesc(Long departmentId, boolean approved);
    @Query("SELECT b FROM Budget b WHERE b.approved = true AND b.updatedAt = " +
            "(SELECT MAX(b2.updatedAt) FROM Budget b2 WHERE b2.department.id = b.department.id AND b2.approved = true)")
     List<Budget> findLatestApprovedBudgetsForAllDepartments();
}
