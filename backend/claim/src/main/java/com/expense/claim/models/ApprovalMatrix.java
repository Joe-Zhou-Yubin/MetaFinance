package com.expense.claim.models;

import jakarta.persistence.*;

@Entity
@Table(name = "approval_matrix")
public class ApprovalMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "approval_event_type", nullable = false)
    private String approvalEventType; // Commitment, Budget, Expense, etc.

    @Column(name = "level", nullable = false)
    private Integer level; // Integer to control levels of approvers

    @Column(name = "derivation_type", nullable = false)
    private String derivationType; // Function or User

    @Column(name = "approver_id")
    private String approverId; // Could be tied to userId or determined in controller

    public ApprovalMatrix() {
    }

    public ApprovalMatrix(String approvalEventType, Integer level, String derivationType, String approverId) {
        this.approvalEventType = approvalEventType;
        this.level = level;
        this.derivationType = derivationType;
        this.approverId = approverId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApprovalEventType() {
        return approvalEventType;
    }

    public void setApprovalEventType(String approvalEventType) {
        this.approvalEventType = approvalEventType;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getDerivationType() {
        return derivationType;
    }

    public void setDerivationType(String derivationType) {
        this.derivationType = derivationType;
    }

	public String getApproverId() {
		return approverId;
	}

	public void setApproverId(String approverId) {
		this.approverId = approverId;
	}

    
}
