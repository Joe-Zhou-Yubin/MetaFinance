package com.expense.claim.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ExpenseHeaderResponse {
    private Long id;
    private String description;
    private String departmentName;
    private BigDecimal totalAmount;
    private Boolean submitted;
    private LocalDateTime createdAt;
    private List<ExpenseLineItemResponse> lineItems;
    private Boolean approved;

    // Constructors, Getters, and Setters
    public ExpenseHeaderResponse() {}

    public ExpenseHeaderResponse(Long id, String description, String departmentName, BigDecimal totalAmount, Boolean submitted, LocalDateTime createdAt, Boolean approved, List<ExpenseLineItemResponse> lineItems) {
        this.id = id;
        this.description = description;
        this.departmentName = departmentName;
        this.totalAmount = totalAmount;
        this.submitted = submitted;
        this.createdAt = createdAt;
        this.approved = approved;
        this.lineItems = lineItems;
    }

    public Boolean getApproved() {
		return approved;
	}

	public void setApproved(Boolean approved) {
		this.approved = approved;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ExpenseLineItemResponse> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<ExpenseLineItemResponse> lineItems) {
        this.lineItems = lineItems;
    }
}
