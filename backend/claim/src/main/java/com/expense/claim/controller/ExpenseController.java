package com.expense.claim.controller;

import com.expense.claim.models.ExpenseHeader;
import com.expense.claim.models.ExpenseLineItem;
import com.expense.claim.models.RequestStatus;
import com.expense.claim.models.User;
import com.expense.claim.models.UserDepartment;
import com.expense.claim.payload.response.ExpenseHeaderResponse;
import com.expense.claim.payload.response.ExpenseLineItemResponse;
import com.expense.claim.payload.response.MessageResponse;
import com.expense.claim.repository.ApproverRequestRepository;
import com.expense.claim.repository.ExpenseHeaderRepository;
import com.expense.claim.repository.ExpenseLineItemRepository;
import com.expense.claim.repository.ExpenseTypeRepository;
import com.expense.claim.repository.UserDepartmentRepository;
import com.expense.claim.repository.UserRepository;
import com.expense.claim.security.services.UserDetailsImpl;
import com.expense.claim.service.ApproverDerivationService;
import com.expense.claim.models.ApproverRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// add submit for the expense header
// add approver method

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseHeaderRepository expenseHeaderRepository;

    @Autowired
    private ExpenseLineItemRepository expenseLineItemRepository;

    @Autowired
    private UserDepartmentRepository userDepartmentRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseTypeRepository expenseTypeRepository;
    
    @Autowired
    private ApproverDerivationService approverDerivationService;
    
    @Autowired
    private ApproverRequestRepository approverRequestRepository;

    // **Create Expense Header**
    @PostMapping("/header")
    public ResponseEntity<?> createExpenseHeader(@Valid @RequestBody ExpenseHeader expenseHeaderRequest) {
        // Retrieve the current user info (requestor)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

        // Fetch the department for the current user
        Optional<UserDepartment> userDepartmentOptional = userDepartmentRepository.findByUserId(currentUser.getId());
        if (userDepartmentOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: No department mapping found for the user."));
        }

        UserDepartment userDepartment = userDepartmentOptional.get();

        // Set requestor and header details
        expenseHeaderRequest.setRequestorId(currentUser.getId());
        expenseHeaderRequest.setDepartment(userDepartment.getDepartment());
        expenseHeaderRequest.setCreatedAt(LocalDateTime.now());
        expenseHeaderRequest.setTotalAmount(BigDecimal.ZERO);
        expenseHeaderRequest.setSubmitted(false);  // Default submitted value is 0

        // Save the expense header
        ExpenseHeader savedHeader = expenseHeaderRepository.save(expenseHeaderRequest);
        ExpenseHeaderResponse response = mapToExpenseHeaderResponse(savedHeader);
        return ResponseEntity.ok(response);
    }
    
 // Helper method to map ExpenseHeader to ExpenseHeaderResponseDTO
    private ExpenseHeaderResponse mapToExpenseHeaderResponse(ExpenseHeader header) {
        List<ExpenseLineItemResponse> lineItems = Optional.ofNullable(header.getLineItems())
        		.orElse(List.of())
        		.stream()
                .map(this::mapToExpenseLineItemResponse)
                .collect(Collectors.toList());

        return new ExpenseHeaderResponse(
                header.getId(),
                header.getDescription(),
                header.getDepartment().getName(),
                header.getTotalAmount(),
                header.isSubmitted(),
                header.getCreatedAt(),
                header.isApproved(),
                lineItems
        );
    }
    
 // Helper method to map ExpenseLineItem to ExpenseLineItemResponseDTO
    private ExpenseLineItemResponse mapToExpenseLineItemResponse(ExpenseLineItem lineItem) {
        return new ExpenseLineItemResponse(
                lineItem.getId(),
                lineItem.getExpenseType().getName(),
                lineItem.getDescription(),
                lineItem.getAmount(),
                lineItem.getCreatedAt()
        );
    }
    
    @PostMapping("/line/{id}")
    public ResponseEntity<?> createExpenseLineItems(@PathVariable Long id, @RequestBody List<ExpenseLineItem> lineItemRequests) {
        System.out.println("Received request to create line items for header ID: " + id);

        // Retrieve the expense header by ID
        Optional<ExpenseHeader> headerOptional = expenseHeaderRepository.findById(id);
        if (headerOptional.isEmpty()) {
            System.out.println("Error: Expense header not found for ID: " + id);
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense header not found."));
        }

        ExpenseHeader expenseHeader = headerOptional.get();
        System.out.println("Expense header found with ID: " + expenseHeader.getId());

        // Validate and process each line item
        BigDecimal updatedTotalAmount = expenseHeader.getTotalAmount();
        for (ExpenseLineItem lineItem : lineItemRequests) {
            System.out.println("Processing line item with description: " + lineItem.getDescription());

            // Validate the expense type
            if (lineItem.getExpenseType() == null || !expenseTypeRepository.existsById(lineItem.getExpenseType().getId())) {
                System.out.println("Error: Invalid expense type for line item with description: " + lineItem.getDescription());
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid expense type for one of the line items."));
            }
            System.out.println("Expense type validated for line item with description: " + lineItem.getDescription());

            // Set the necessary details for the line item
            lineItem.setExpenseHeader(expenseHeader);
            lineItem.setCreatedAt(LocalDateTime.now());
            System.out.println("Line item details set: header ID = " + expenseHeader.getId() + ", createdAt = " + lineItem.getCreatedAt());

            // Save the line item
            expenseLineItemRepository.save(lineItem);
            System.out.println("Line item saved with ID: " + lineItem.getId());

            // Update the total amount
            updatedTotalAmount = updatedTotalAmount.add(lineItem.getAmount());
            System.out.println("Updated total amount: " + updatedTotalAmount);
        }

        // Update and save the expense header with the new total amount
        expenseHeader.setTotalAmount(updatedTotalAmount);
        expenseHeaderRepository.save(expenseHeader);
        System.out.println("Expense header updated with new total amount: " + updatedTotalAmount);

        return ResponseEntity.ok(new MessageResponse("Line items created successfully."));
    }


 // **Get Expense Header and Line Items**
    @GetMapping("/header/{id}")
    public ResponseEntity<?> getExpenseHeaderById(@PathVariable Long id) {
        // Retrieve the expense header by ID
        Optional<ExpenseHeader> headerOptional = expenseHeaderRepository.findById(id);

        if (headerOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense header not found."));
        }

        ExpenseHeader expenseHeader = headerOptional.get();

        // Map to the response DTO
        ExpenseHeaderResponse response = mapToExpenseHeaderResponse(expenseHeader);

        return ResponseEntity.ok(response);
    }

    
 // **Get Expense Headers and Line Items by User ID**
    @GetMapping("/header/user/{userId}")
    public ResponseEntity<?> getExpenseHeadersByUserId(@PathVariable Long userId) {
        // Retrieve expense headers for the given user ID
        List<ExpenseHeader> headers = expenseHeaderRepository.findByRequestorId(userId);

        if (headers.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: No expense headers found for the user."));
        }

        // Map headers to response DTOs
        List<ExpenseHeaderResponse> responses = headers.stream()
                .map(this::mapToExpenseHeaderResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/header/submit/{id}")
    public ResponseEntity<?> submitExpenseHeader(@PathVariable Long id) {
        System.out.println("Received request to submit expense header with ID: " + id);

        // Retrieve the current user info (requestor)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        System.out.println("Current User ID: " + currentUser.getId());

        // Retrieve the expense header by ID
        Optional<ExpenseHeader> headerOptional = expenseHeaderRepository.findById(id);
        if (headerOptional.isEmpty()) {
            System.out.println("Error: Expense header not found for ID: " + id);
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense header not found."));
        }

        ExpenseHeader expenseHeader = headerOptional.get();
        if (expenseHeader.isSubmitted()) {
            System.out.println("Error: Expense header is already submitted.");
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense header is already submitted."));
        }

        // Update the submitted flag
        expenseHeader.setSubmitted(true);
        expenseHeaderRepository.save(expenseHeader);
        System.out.println("Expense header submitted successfully.");

        // ✅ Use ApprovalMatrix to derive the approver dynamically
        Optional<Long> approverIdOptional = approverDerivationService.deriveApprover(currentUser.getId(), "Expense");
        if (approverIdOptional.isEmpty()) {
            System.out.println("Error: No approver found for Expense approval.");
            return ResponseEntity.badRequest().body(new MessageResponse("Error: No approver found for Expense approval."));
        }

        Long approverId = approverIdOptional.get();
        Optional<User> approverOptional = userRepository.findById(approverId);

        if (approverOptional.isEmpty()) {
            System.out.println("Error: Approver user not found with ID: " + approverId);
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Approver user not found."));
        }

        User approver = approverOptional.get();
        System.out.println("Approver ID: " + approver.getId() + " - " + approver.getUsername());

        // ✅ Retrieve the requestor using requestorId
        Optional<User> requestorOptional = userRepository.findById(expenseHeader.getRequestorId());
        if (requestorOptional.isEmpty()) {
            System.out.println("Error: Requestor user not found with ID: " + expenseHeader.getRequestorId());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Requestor user not found."));
        }

        User requestor = requestorOptional.get();

        // ✅ Create and save an approver request
        ApproverRequest approverRequest = new ApproverRequest();
        approverRequest.setRequestor(requestor);
        approverRequest.setApprover(approver);
        approverRequest.setType("Expense Approval");
        approverRequest.setReferenceId(expenseHeader.getId());
        approverRequest.setStatus(RequestStatus.PENDING);
        approverRequest.setCreatedAt(LocalDateTime.now());

        approverRequestRepository.save(approverRequest);
        System.out.println("Approver request created successfully for Expense approval.");

        return ResponseEntity.ok(new MessageResponse("Expense header submitted and approval request triggered successfully."));
    }



}
