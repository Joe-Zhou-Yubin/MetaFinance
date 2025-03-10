package com.expense.claim.controller;

import com.expense.claim.models.ApproverRequest;
import com.expense.claim.models.Budget;
import com.expense.claim.models.Commitment;
import com.expense.claim.models.ExpenseHeader;
import com.expense.claim.models.RequestStatus;
import com.expense.claim.models.User;
import com.expense.claim.payload.response.MessageResponse;
import com.expense.claim.repository.ApproverRequestRepository;
import com.expense.claim.repository.BudgetRepository;
import com.expense.claim.repository.CommitmentRepository;
import com.expense.claim.repository.ExpenseHeaderRepository;
import com.expense.claim.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.expense.claim.security.services.UserDetailsImpl;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/approver-requests")
public class ApproverRequestController {

    @Autowired
    private ApproverRequestRepository approverRequestRepository;
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private CommitmentRepository commitmentRepository;
    
    @Autowired
    private ExpenseHeaderRepository expenseHeaderRepository;
    
    @Autowired
    private UserRepository userRepository;

    // Get all approver requests
    @GetMapping
    public ResponseEntity<List<ApproverRequest>> getAllApproverRequests() {
        return ResponseEntity.ok(approverRequestRepository.findAll());
    }

    // Get request by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getApproverRequestById(@PathVariable Long id) {
        Optional<ApproverRequest> request = approverRequestRepository.findById(id);
        if (request.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Approver request not found!"));
        }
        return ResponseEntity.ok(request.get());
    }

//    // Create a new approver request
//    @PostMapping
//    public ResponseEntity<?> createApproverRequest(@RequestBody ApproverRequest request) {
//        request.setCreatedAt(LocalDateTime.now());
//        request.setStatus(RequestStatus.PENDING);
//        approverRequestRepository.save(request);
//        return ResponseEntity.ok(new MessageResponse("Approver request created successfully!"));
//    }

    // Update an approver request's status (e.g., approve or reject)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateRequestStatus(
            @PathVariable Long id,
            @RequestParam RequestStatus status,
            @RequestParam(required = false) String rejectReason) {
        
        Optional<ApproverRequest> requestOptional = approverRequestRepository.findById(id);
        if (requestOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Approver request not found!"));
        }

        ApproverRequest request = requestOptional.get();
        request.setStatus(status);

        if (status == RequestStatus.REJECTED) {
            request.setRejectReason(rejectReason);
        }
        request.setApprovedAt(LocalDateTime.now());
        approverRequestRepository.save(request);

        // ✅ Dynamically update or delete the corresponding entity based on request type
        if ("Budget Approval".equalsIgnoreCase(request.getType())) {
            Optional<Budget> budgetOptional = budgetRepository.findById(request.getReferenceId());
            if (budgetOptional.isPresent()) {
                Budget budget = budgetOptional.get();

                if (status == RequestStatus.APPROVED) {
                    budget.setApproved(true);
                    budget.setUpdatedAt(LocalDateTime.now());
                    budgetRepository.save(budget);
                } else {
                    // ❌ DELETE the rejected budget
                    budgetRepository.deleteById(request.getReferenceId());
                    return ResponseEntity.ok(new MessageResponse("Budget request rejected and deleted."));
                }
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Budget not found for approval request!"));
            }
        } else if ("Commitment Approval".equalsIgnoreCase(request.getType())) {
            Optional<Commitment> commitmentOptional = commitmentRepository.findById(request.getReferenceId());
            if (commitmentOptional.isPresent()) {
                Commitment commitment = commitmentOptional.get();

                if (status == RequestStatus.APPROVED) {
                    commitment.setApproved(true);
                    commitment.setApprovedAt(LocalDateTime.now());
                    commitmentRepository.save(commitment);
                } else {
                    // ❌ DELETE the rejected commitment
                    commitmentRepository.deleteById(request.getReferenceId());
                    return ResponseEntity.ok(new MessageResponse("Commitment request rejected and deleted."));
                }
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Commitment not found for approval request!"));
            }
        } else if ("Expense Approval".equalsIgnoreCase(request.getType())) {
            Optional<ExpenseHeader> expenseOptional = expenseHeaderRepository.findById(request.getReferenceId());
            if (expenseOptional.isPresent()) {
                ExpenseHeader expenseHeader = expenseOptional.get();

                if (status == RequestStatus.APPROVED) {
                    expenseHeader.setApproved(true);
                } else {
                    // ❌ UNSET submitted flag if expense is rejected
                    expenseHeader.setSubmitted(false);
                }

                expenseHeader.setApprovedAt(LocalDateTime.now());
                expenseHeaderRepository.save(expenseHeader);
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense not found for approval request!"));
            }
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Unsupported request type!"));
        }

        return ResponseEntity.ok(new MessageResponse("Request status updated successfully!"));
    }

        

    // Notify approver (simulates notification by updating the notifiedAt field) -- not done yet
    @PutMapping("/{id}/notify")
    public ResponseEntity<?> notifyApprover(@PathVariable Long id) {
        Optional<ApproverRequest> requestOptional = approverRequestRepository.findById(id);
        if (requestOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Approver request not found!"));
        }

        ApproverRequest request = requestOptional.get();
        request.setNotifiedAt(LocalDateTime.now());
        approverRequestRepository.save(request);

        return ResponseEntity.ok(new MessageResponse("Approver notified successfully!"));
    }

    // Delete an approver request
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApproverRequest(@PathVariable Long id) {
        if (!approverRequestRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Approver request not found!"));
        }

        approverRequestRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Approver request deleted successfully!"));
    }

    // Get all pending requests for a specific approver
    @GetMapping("/approver/{approverId}/pending")
    public ResponseEntity<List<ApproverRequest>> getPendingRequestsForApprover(@PathVariable Long approverId) {
        List<ApproverRequest> pendingRequests = approverRequestRepository.findByApproverIdAndStatus(approverId, RequestStatus.PENDING);
        return ResponseEntity.ok(pendingRequests);
    }
    
    @GetMapping("/department/{departmentId}/pending")
    public ResponseEntity<List<ApproverRequest>> getPendingRequestsForDepartment(@PathVariable Long departmentId) {
        // Fetch pending requests where the reference points to budgets related to the department
        List<ApproverRequest> pendingRequests = approverRequestRepository.findPendingByDepartmentId(departmentId, RequestStatus.PENDING);
        return ResponseEntity.ok(pendingRequests);
    }

    
 // Dynamically create and save an approver request
    public ApproverRequest createApproverRequest(Long requestorId, Long approverId, String type, Long referenceId) {
        // Retrieve requestor and approver users
        Optional<User> requestor = userRepository.findById(requestorId);
        Optional<User> approver = userRepository.findById(approverId);

        if (requestor.isEmpty() || approver.isEmpty()) {
            throw new RuntimeException("Error: User(s) not found!");
        }

        // Create the approver request
        ApproverRequest request = new ApproverRequest();
        request.setRequestor(requestor.get());
        request.setApprover(approver.get());
        request.setType(type);
        request.setReferenceId(referenceId);
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        return approverRequestRepository.save(request);
    }
    
 // ✅ New Method: Get Own Pending Requests
    @GetMapping("/pending")
    public ResponseEntity<List<ApproverRequest>> getOwnPendingRequests() {
        // Get the currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

        // Extract user ID from JWT token
        Long approverId = currentUser.getId();

        // Fetch pending approval requests for the logged-in user
        List<ApproverRequest> pendingRequests = approverRequestRepository.findByApproverIdAndStatus(approverId, RequestStatus.PENDING);

        return ResponseEntity.ok(pendingRequests);
    }
    
    
}
