package com.expense.claim.repository;

import com.expense.claim.models.Commitment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommitmentRepository extends JpaRepository<Commitment, Long> {
    List<Commitment> findByDepartmentId(Long departmentId);
    List<Commitment> findByApproved(boolean approved);
    List<Commitment> findByPaid(boolean paid);
    List<Commitment> findByDepartmentIdAndApprovedAndPaid(Long departmentId, boolean approved, boolean paid);
	List<Commitment> findByApprovedAndPaid(boolean b, boolean c);
	// Fetch all approved commitments
    List<Commitment> findByApprovedTrue();

    // Fetch approved commitments for a specific department
    List<Commitment> findByDepartmentIdAndApprovedTrue(Long departmentId);
}
