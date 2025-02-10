package com.expense.claim.controller;

import com.expense.claim.models.Budget;
import com.expense.claim.models.Department;
import com.expense.claim.payload.response.MessageResponse;
import com.expense.claim.payload.response.UserInfoResponse;
import com.expense.claim.repository.BudgetRepository;
import com.expense.claim.repository.DepartmentRepository;
import com.expense.claim.payload.response.BudgetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    private UserController userController;

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

        // Retrieve the admin user info
        ResponseEntity<?> adminUserResponse = userController.getAdminUser();
        if (adminUserResponse.getStatusCode().is4xxClientError()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Admin user not found!"));
        }

        UserInfoResponse adminUser = (UserInfoResponse) adminUserResponse.getBody();
        if (adminUser == null) {
            return ResponseEntity.status(500).body(new MessageResponse("Error: Unable to retrieve admin user information."));
        }

        // Dynamically trigger an approver request after budget creation
        approverRequestController.createApproverRequest(
                currentUser.getId(),               // Requestor ID from current user info
                adminUser.getId(),                 // Approver ID from the admin user info
                "Budget Approval",
                budget.getId()
        );

        return ResponseEntity.ok(budget);
    }

    // Delete a budget
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable Long id) {
        if (!budgetRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Budget not found!"));
        }

        budgetRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Budget deleted successfully!"));
    }
}
