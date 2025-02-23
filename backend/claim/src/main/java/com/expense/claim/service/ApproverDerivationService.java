package com.expense.claim.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.expense.claim.models.ApprovalMatrix;
import com.expense.claim.repository.ApprovalMatrixRepository;
import com.expense.claim.repository.DepartmentRepository;
import com.expense.claim.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ApproverDerivationService {

	@Autowired
    private ApprovalMatrixRepository approvalMatrixRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    // Derive approver based on requestor ID and approval event type
    public Optional<Long> deriveApprover(Long requestorId, String approvalEventType) {
        List<ApprovalMatrix> matrices = approvalMatrixRepository.findByApprovalEventType(approvalEventType);

        for (ApprovalMatrix matrix : matrices) {
            if ("User".equalsIgnoreCase(matrix.getDerivationType())) {
                // Return the user ID directly
                return Optional.of(Long.parseLong(matrix.getApproverId()));
            } else if ("Function".equalsIgnoreCase(matrix.getDerivationType())) {
                // Derive department head or other functions
                if ("DEPARTMENT_HEAD".equalsIgnoreCase(matrix.getApproverId())) {
                    return findDepartmentHead(requestorId);
                }
            }
        }
        return Optional.empty();
    }

    // Find the department head based on requestor's department
    private Optional<Long> findDepartmentHead(Long requestorId) {
        return userRepository.findDepartmentHeadByUserId(requestorId);
    }
}