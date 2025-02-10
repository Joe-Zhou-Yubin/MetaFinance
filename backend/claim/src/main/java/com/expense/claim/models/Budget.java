package com.expense.claim.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean approved;

    public Budget() {
    }

    public Budget(Department department, BigDecimal amount, LocalDateTime updatedAt, boolean approved) {
        this.department = department;
        this.amount = amount;
        this.updatedAt = updatedAt;
        this.approved = approved;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @PrePersist
    @PreUpdate
    public void setUpdatedAtAutomatically() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Budget{" +
               "id=" + id +
               ", department=" + (department != null ? department.getId() : null) +
               ", amount=" + amount +
               ", updatedAt=" + updatedAt +
               ", approved=" + approved +
               '}';
    }
}
