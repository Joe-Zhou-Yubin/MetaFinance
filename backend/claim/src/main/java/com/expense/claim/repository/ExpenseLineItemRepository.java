package com.expense.claim.repository;

import com.expense.claim.models.ExpenseLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseLineItemRepository extends JpaRepository<ExpenseLineItem, Long> {

    // Custom method to find line items by expense header ID
    List<ExpenseLineItem> findByExpenseHeaderId(Long expenseHeaderId);
    
    
}
