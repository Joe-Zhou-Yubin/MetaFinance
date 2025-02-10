package com.expense.claim.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BudgetResponse {
    private Long id;
    private Long departmentId;
    private String departmentName;
    private BigDecimal amount;
    private boolean approved;
    private LocalDateTime updatedAt;

    public BudgetResponse(Long id, Long departmentId, String departmentName, BigDecimal amount, boolean approved, LocalDateTime updatedAt) {
        this.id = id;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.amount = amount;
        this.approved = approved;
        this.updatedAt = updatedAt;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

    // Getters and setters
}
