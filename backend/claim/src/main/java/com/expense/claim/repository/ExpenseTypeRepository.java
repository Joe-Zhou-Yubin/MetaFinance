package com.expense.claim.repository;

import com.expense.claim.models.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseTypeRepository extends JpaRepository<ExpenseType, Long> {
}
