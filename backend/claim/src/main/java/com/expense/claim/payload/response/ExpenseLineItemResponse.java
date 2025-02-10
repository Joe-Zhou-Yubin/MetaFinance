package com.expense.claim.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ExpenseLineItemResponse {
    private Long id;
    private String expenseTypeName;
    private String description;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    // Constructors, Getters, and Setters
    public ExpenseLineItemResponse() {}

    public ExpenseLineItemResponse(Long id, String expenseTypeName, String description, BigDecimal amount, LocalDateTime createdAt) {
        this.id = id;
        this.expenseTypeName = expenseTypeName;
        this.description = description;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExpenseTypeName() {
        return expenseTypeName;
    }

    public void setExpenseTypeName(String expenseTypeName) {
        this.expenseTypeName = expenseTypeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
