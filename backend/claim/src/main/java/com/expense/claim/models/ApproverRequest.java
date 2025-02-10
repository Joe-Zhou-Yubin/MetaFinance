package com.expense.claim.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "approvers")
public class ApproverRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requestor_id", nullable = false)
    private User requestor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;  // Mandatory reference ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;  // Enum type for statuses

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;  // Timestamp for when notification was sent

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;  // Reason for rejection, if applicable

    public ApproverRequest() {
    }

    public ApproverRequest(User requestor, User approver, String type, Long referenceId) {
        this.requestor = requestor;
        this.approver = approver;
        this.type = type;
        this.referenceId = referenceId;
        this.createdAt = LocalDateTime.now();
        this.status = RequestStatus.PENDING;
    }
    
    


    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getRequestor() {
		return requestor;
	}

	public void setRequestor(User requestor) {
		this.requestor = requestor;
	}

	public User getApprover() {
		return approver;
	}

	public void setApprover(User approver) {
		this.approver = approver;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(Long referenceId) {
		this.referenceId = referenceId;
	}

	public RequestStatus getStatus() {
		return status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
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

	public LocalDateTime getNotifiedAt() {
		return notifiedAt;
	}

	public void setNotifiedAt(LocalDateTime notifiedAt) {
		this.notifiedAt = notifiedAt;
	}

	public String getRejectReason() {
		return rejectReason;
	}

	public void setRejectReason(String rejectReason) {
		this.rejectReason = rejectReason;
	}

	@Override
    public String toString() {
        return "ApproverRequest{" +
               "id=" + id +
               ", requestor=" + (requestor != null ? requestor.getId() : null) +
               ", approver=" + (approver != null ? approver.getId() : null) +
               ", type='" + type + '\'' +
               ", referenceId=" + referenceId +
               ", status=" + status +
               ", createdAt=" + createdAt +
               ", approvedAt=" + approvedAt +
               ", notifiedAt=" + notifiedAt +
               ", rejectReason='" + rejectReason + '\'' +
               '}';
    }
}
