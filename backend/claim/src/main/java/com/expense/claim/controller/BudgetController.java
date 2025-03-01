package com.expense.claim.controller;

import com.expense.claim.models.ApproverRequest;
import com.expense.claim.models.Budget;
import com.expense.claim.models.Commitment;
import com.expense.claim.models.Department;
import com.expense.claim.payload.response.MessageResponse;
import com.expense.claim.payload.response.UserInfoResponse;
import com.expense.claim.repository.ApproverRequestRepository;
import com.expense.claim.repository.BudgetRepository;
import com.expense.claim.repository.CommitmentRepository;
import com.expense.claim.repository.DepartmentRepository;
import com.expense.claim.service.ApproverDerivationService;
import com.expense.claim.payload.response.BudgetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// need update for calculating remaining budget

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BudgetController {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private ApproverRequestController approverRequestController;
    
    @Autowired
    private ApproverRequestRepository approverRequestRepository;
    
    @Autowired
    private UserController userController;
    
    @Autowired
    private ApproverDerivationService approverDerivationService;
    
    @Autowired
    private CommitmentRepository commitmentRepository;

 // Get all budgets - admin method
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAllBudgets() {
        List<BudgetResponse> budgetResponses = budgetRepository.findAll().stream()
            .map(budget -> new BudgetResponse(
                budget.getId(),
                budget.getDepartment().getId(),
                budget.getDepartment().getName(),
                budget.getAmount(),
                budget.isApproved(),
                budget.getUpdatedAt()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(budgetResponses);
    }


 // Get the most recent approved budget by department ID
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<BudgetResponse> getLatestApprovedBudgetByDepartmentId(@PathVariable Long departmentId) {
        Optional<Budget> latestApprovedBudget = budgetRepository.findTopByDepartmentIdAndApprovedOrderByUpdatedAtDesc(departmentId, true);

        if (latestApprovedBudget.isEmpty()) {
            return ResponseEntity.badRequest().body(null); // Return 400 if no approved budget found
        }

        Budget budget = latestApprovedBudget.get();
        BudgetResponse response = new BudgetResponse(
            budget.getId(),
            budget.getDepartment().getId(),
            budget.getDepartment().getName(),
            budget.getAmount(),
            budget.isApproved(),
            budget.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }
    
 // Get the most recent approved budget for all departments
    @GetMapping("/department")
    public ResponseEntity<List<BudgetResponse>> getLatestApprovedBudgetForAllDepartments() {
        // Fetch all departments with their most recent approved budgets
        List<Budget> latestApprovedBudgets = budgetRepository.findLatestApprovedBudgetsForAllDepartments();

        // Convert each budget to a BudgetResponse DTO
        List<BudgetResponse> response = latestApprovedBudgets.stream().map(budget -> 
            new BudgetResponse(
                budget.getId(),
                budget.getDepartment().getId(),
                budget.getDepartment().getName(),
                budget.getAmount(),
                budget.isApproved(),
                budget.getUpdatedAt()
            )
        ).toList();

        return ResponseEntity.ok(response);
    }


    
    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudgetById(@PathVariable Long id) {
        Optional<Budget> budgetOptional = budgetRepository.findById(id);
        if (budgetOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        Budget budget = budgetOptional.get();
        BudgetResponse response = new BudgetResponse(
            budget.getId(),
            budget.getDepartment().getId(),
            budget.getDepartment().getName(),
            budget.getAmount(),
            budget.isApproved(),
            budget.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }
    
 // Get all approved budgets
    @GetMapping("/approved")
    public ResponseEntity<List<BudgetResponse>> getApprovedBudgets() {
        List<BudgetResponse> responses = budgetRepository.findByApprovedTrue().stream()
            .map(budget -> new BudgetResponse(
                budget.getId(),
                budget.getDepartment().getId(),
                budget.getDepartment().getName(),
                budget.getAmount(),
                budget.isApproved(),
                budget.getUpdatedAt()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }


    
    

//    @PostMapping
//    public ResponseEntity<?> createBudget(@Valid @RequestBody Budget budgetRequest) {
//        // Retrieve the current user info (requestor)
//        ResponseEntity<UserInfoResponse> currentUserResponse = userController.getCurrentUserInfo();
//        if (currentUserResponse.getStatusCode().is4xxClientError()) {
//            return ResponseEntity.status(401).body(new MessageResponse("Error: Unauthorized!"));
//        }
//
//        UserInfoResponse currentUser = currentUserResponse.getBody();
//        if (currentUser == null) {
//            return ResponseEntity.status(500).body(new MessageResponse("Error: Unable to retrieve user information."));
//        }
//
//        // Check if the department exists
//        Optional<Department> department = departmentRepository.findById(budgetRequest.getDepartment().getId());
//        if (department.isEmpty()) {
//            return ResponseEntity.badRequest().body(new MessageResponse("Error: Department not found!"));
//        }
//
//        // Set department and updated date
//        budgetRequest.setDepartment(department.get());
//        budgetRequest.setUpdatedAt(LocalDateTime.now());
//
//        // Save the budget
//        Budget budget = budgetRepository.save(budgetRequest);
//
//        // Retrieve the admin user info
//        ResponseEntity<?> adminUserResponse = userController.getAdminUser();
//        if (adminUserResponse.getStatusCode().is4xxClientError()) {
//            return ResponseEntity.badRequest().body(new MessageResponse("Error: Admin user not found!"));
//        }
//
//        UserInfoResponse adminUser = (UserInfoResponse) adminUserResponse.getBody();
//        if (adminUser == null) {
//            return ResponseEntity.status(500).body(new MessageResponse("Error: Unable to retrieve admin user information."));
//        }
//
//        // Dynamically trigger an approver request after budget creation
//        approverRequestController.createApproverRequest(
//                currentUser.getId(),               // Requestor ID from current user info
//                adminUser.getId(),                 // Approver ID from the admin user info
//                "Budget Approval",
//                budget.getId()
//        );
//
//        return ResponseEntity.ok(budget);
//    }
    
    @PostMapping
    public ResponseEntity<?> createBudget(@Valid @RequestBody Budget budgetRequest) {
        // Retrieve the current user info (requestor)
        ResponseEntity<UserInfoResponse> currentUserResponse = userController.getCurrentUserInfo();
        if (currentUserResponse.getStatusCode().is4xxClientError()) {
            return ResponseEntity.status(401).body(new MessageResponse("Error: Unauthorized!"));
        }

        UserInfoResponse currentUser = currentUserResponse.getBody();
        if (currentUser == null) {
            return ResponseEntity.status(500).body(new MessageResponse("Error: Unable to retrieve user information."));
        }

        // Check if the department exists
        Optional<Department> department = departmentRepository.findById(budgetRequest.getDepartment().getId());
        if (department.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Department not found!"));
        }

        // Set department and updated date
        budgetRequest.setDepartment(department.get());
        budgetRequest.setUpdatedAt(LocalDateTime.now());

        // Save the budget
        Budget budget = budgetRepository.save(budgetRequest);

        // Use ApprovalMatrix to derive approver
        Optional<Long> approverId = approverDerivationService.deriveApprover(currentUser.getId(), "Budget");
        if (approverId.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: No approver found for the budget approval process."));
        }

        // Dynamically trigger an approver request
        approverRequestController.createApproverRequest(
                currentUser.getId(),
                approverId.get(),
                "Budget Approval",
                budget.getId()
        );

        return ResponseEntity.ok(budget);
    }


 // Delete a budget and its corresponding approval request
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable Long id) {
        if (!budgetRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Budget not found!"));
        }

        // Find and delete all approval requests associated with this budget
        List<ApproverRequest> approvalRequests = approverRequestRepository.findByTypeAndReferenceId("Budget Approval", id);
        
        if (!approvalRequests.isEmpty()) {
            approverRequestRepository.deleteAll(approvalRequests);
        }

        // Now delete the budget
        budgetRepository.deleteById(id);
        
        return ResponseEntity.ok(new MessageResponse("Budget and associated approval request(s) deleted successfully!"));
    }


    
 // **Get Committed Budget for All Departments**
    @GetMapping("/committed")
    public ResponseEntity<List<BudgetResponse>> getCommittedBudgetForAllDepartments() {
        // Fetch all approved budgets for all departments
        List<Budget> latestApprovedBudgets = budgetRepository.findLatestApprovedBudgetsForAllDepartments();

        // Fetch all approved commitments
        List<Commitment> approvedCommitments = commitmentRepository.findByApprovedTrue();

        // Group commitments by department ID and sum the amounts
        Map<Long, BigDecimal> commitmentTotals = approvedCommitments.stream()
            .collect(Collectors.groupingBy(
                commitment -> commitment.getDepartment().getId(),  // Get department ID
                Collectors.reducing(BigDecimal.ZERO, Commitment::getAmount, BigDecimal::add)
            ));

        // Process each budget and subtract commitments
        List<BudgetResponse> response = latestApprovedBudgets.stream()
            .map(budget -> {
                BigDecimal totalCommitments = commitmentTotals.getOrDefault(budget.getDepartment().getId(), BigDecimal.ZERO);
                BigDecimal committedBudget = budget.getAmount().subtract(totalCommitments);

                return new BudgetResponse(
                    budget.getId(),
                    budget.getDepartment().getId(),
                    budget.getDepartment().getName(),
                    committedBudget,  // Remaining budget after commitments
                    budget.isApproved(),
                    budget.getUpdatedAt()
                );
            }).toList();

        return ResponseEntity.ok(response);
    }
    
 // **Get Committed Budget for Specific Department**
    @GetMapping("/committed/department/{departmentId}")
    public ResponseEntity<BudgetResponse> getCommittedBudgetForDepartment(@PathVariable Long departmentId) {
        // Fetch latest approved budget for the department
        Optional<Budget> latestApprovedBudget = budgetRepository.findTopByDepartmentIdAndApprovedOrderByUpdatedAtDesc(departmentId, true);

        if (latestApprovedBudget.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        // Fetch commitments for the department
        List<Commitment> departmentCommitments = commitmentRepository.findByDepartmentIdAndApprovedTrue(departmentId);
        BigDecimal totalCommitments = departmentCommitments.stream()
            .map(Commitment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate committed budget
        Budget budget = latestApprovedBudget.get();
        BudgetResponse response = new BudgetResponse(
            budget.getId(),
            budget.getDepartment().getId(),
            budget.getDepartment().getName(),
            budget.getAmount().subtract(totalCommitments), // Deduct committed amount
            budget.isApproved(),
            budget.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }

}
