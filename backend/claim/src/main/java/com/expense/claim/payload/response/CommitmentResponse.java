package com.expense.claim.payload.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CommitmentResponse {
	private Long id;
    private String requestorName;
    private String departmentName;
    private String description;
    private BigDecimal amount;
    private boolean approved;
    private boolean paid;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;

    public CommitmentResponse(Long id, String requestorName, String departmentName, String description,
                              BigDecimal amount, boolean approved, boolean paid, LocalDateTime createdAt,
                              LocalDateTime approvedAt) {
        this.id = id;
        this.requestorName = requestorName;
        this.departmentName = departmentName;
        this.description = description;
        this.amount = amount;
        this.approved = approved;
        this.paid = paid;
        this.createdAt = createdAt;
        this.approvedAt = approvedAt;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRequestorName() {
		return requestorName;
	}

	public void setRequestorName(String requestorName) {
		this.requestorName = requestorName;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
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

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public boolean isPaid() {
		return paid;
	}

	public void setPaid(boolean paid) {
		this.paid = paid;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getApprovedAt() {
		return approvedAt;
	}

	public void setApprovedAt(LocalDateTime approvedAt) {
		this.approvedAt = approvedAt;
	}
    
}
