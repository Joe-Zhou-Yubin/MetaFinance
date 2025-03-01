package com.expense.claim.repository;

import com.expense.claim.models.ApproverRequest;
import com.expense.claim.models.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApproverRequestRepository extends JpaRepository<ApproverRequest, Long> {

    // Find requests by approver ID and status
    List<ApproverRequest> findByApproverIdAndStatus(Long approverId, RequestStatus status);

    // Find requests by requestor ID
    List<ApproverRequest> findByRequestorId(Long requestorId);

    // Find all requests by type and reference ID
    List<ApproverRequest> findByTypeAndReferenceId(String type, Long referenceId);

    // Delete all approval requests related to a commitment
    void deleteByTypeAndReferenceId(String type, Long referenceId);

    // Count pending requests for an approver
    long countByApproverIdAndStatus(Long approverId, RequestStatus status);

    @Query("SELECT ar FROM ApproverRequest ar WHERE ar.status = :status AND ar.referenceId IN " +
            "(SELECT b.id FROM Budget b WHERE b.department.id = :departmentId)")
    List<ApproverRequest> findPendingByDepartmentId(@Param("departmentId") Long departmentId, 
                                                    @Param("status") RequestStatus status);
}
