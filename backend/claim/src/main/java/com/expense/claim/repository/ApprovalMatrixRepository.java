package com.expense.claim.repository;

import com.expense.claim.models.ApprovalMatrix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalMatrixRepository extends JpaRepository<ApprovalMatrix, Long> {
    List<ApprovalMatrix> findByApprovalEventTypeAndLevel(String approvalEventType, Integer level);

	List<ApprovalMatrix> findByApprovalEventType(String approvalEventType);
}
