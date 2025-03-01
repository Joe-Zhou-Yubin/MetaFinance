package com.expense.claim.controller;

import com.expense.claim.models.ExpenseHeader;
import com.expense.claim.models.ExpenseLineItem;
import com.expense.claim.models.RequestStatus;
import com.expense.claim.models.User;
import com.expense.claim.models.UserDepartment;
import com.expense.claim.payload.response.BudgetResponse;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
@CrossOrigin(origins = "*", maxAge = 3600)
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
    private BudgetController budgetController;
    
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


// // **Get Expense Header and Line Items**
//    @GetMapping("/header/{id}")
//    public ResponseEntity<?> getExpenseHeaderById(@PathVariable Long id) {
//        // Retrieve the expense header by ID
//        Optional<ExpenseHeader> headerOptional = expenseHeaderRepository.findById(id);
//
//        if (headerOptional.isEmpty()) {
//            return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense header not found."));
//        }
//
//        ExpenseHeader expenseHeader = headerOptional.get();
//
//        // Map to the response DTO
//        ExpenseHeaderResponse response = mapToExpenseHeaderResponse(expenseHeader);
//
//        return ResponseEntity.ok(response);
//    }
    
    @GetMapping("/header/{id}")
    public ResponseEntity<?> getExpenseHeaderById(@PathVariable Long id) {
        // Retrieve the expense header by ID
        Optional<ExpenseHeader> headerOptional = expenseHeaderRepository.findById(id);

        if (headerOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense header not found."));
        }

        ExpenseHeader expenseHeader = headerOptional.get();

        // Extract the authenticated user's details from JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(new MessageResponse("Error: Unauthorized access."));
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername(); // Extract logged-in user's username

        // Check if user is either the owner, a director, or an admin
        boolean isOwner = expenseHeader.getRequestorId().equals(username);
        boolean isDirectorOrAdmin = userDetails.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_DIRECTOR") || role.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isDirectorOrAdmin) {
            throw new AccessDeniedException("Error: You are not authorized to access this expense.");
        }

        // Map to response DTO
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

        // ✅ Step 1: Get Department ID
        Long departmentId = expenseHeader.getDepartment().getId();

        // ✅ Step 2: Fetch Remaining Committed Budget
        ResponseEntity<BudgetResponse> budgetResponse = budgetController.getCommittedBudgetForDepartment(departmentId);
        if (!budgetResponse.getStatusCode().is2xxSuccessful() || budgetResponse.getBody() == null) {
            System.out.println("Error: Failed to retrieve committed budget for department ID: " + departmentId);
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Unable to retrieve committed budget."));
        }

        BudgetResponse budgetData = budgetResponse.getBody();
        BigDecimal remainingBudget = budgetData.getAmount();
        BigDecimal expenseTotal = expenseHeader.getTotalAmount();

        // ✅ Step 3: Check Budget Constraint
        if (expenseTotal.compareTo(remainingBudget) > 0) {
            System.out.println("Error: Expense total exceeds committed budget.");
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense total exceeds committed budget. Please check with your Director."));
        }

        // ✅ Step 4: Update the submitted flag
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

    
 // **Get Expense Headers and Line Items for the Authenticated User**
    @GetMapping("/header/own")
    public ResponseEntity<?> getExpenseHeadersByUser(@AuthenticationPrincipal UserDetails userDetails) {
        // Extract user ID from the authenticated user
        Optional<User> userOptional = userRepository.findByUsername(userDetails.getUsername());

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found."));
        }

        Long userId = userOptional.get().getId();

        // Retrieve expense headers for the authenticated user
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
    
 // **Delete an expense header and its associated line items & approval requests**
    @DeleteMapping("/header/{id}")
    public ResponseEntity<?> deleteExpenseHeader(@PathVariable Long id) {
        if (!expenseHeaderRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense header not found!"));
        }

        // Step 1: Delete associated approval requests
        List<ApproverRequest> approvalRequests = approverRequestRepository.findByTypeAndReferenceId("Expense Approval", id);
        if (!approvalRequests.isEmpty()) {
            approverRequestRepository.deleteAll(approvalRequests);
        }

        // Step 2: Delete all expense line items linked to this expense header
        List<ExpenseLineItem> lineItems = expenseLineItemRepository.findByExpenseHeaderId(id);
        if (!lineItems.isEmpty()) {
            expenseLineItemRepository.deleteAll(lineItems);
        }

        // Step 3: Now delete the expense header itself
        expenseHeaderRepository.deleteById(id);

        return ResponseEntity.ok(new MessageResponse("Expense header, associated line items, and approval requests deleted successfully!"));
    }


 // **Delete an expense line item by header ID and line item ID**
    @DeleteMapping("/line/{headerId}/{lineItemId}")
    public ResponseEntity<?> deleteExpenseLineItem(@PathVariable Long headerId, @PathVariable Long lineItemId) {
        if (headerId == null || lineItemId == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid header ID or line item ID."));
        }

        Optional<ExpenseHeader> expenseHeaderOptional = expenseHeaderRepository.findById(headerId);
        if (expenseHeaderOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense header not found!"));
        }

        ExpenseHeader expenseHeader = expenseHeaderOptional.get();

        Optional<ExpenseLineItem> lineItemOptional = expenseLineItemRepository.findById(lineItemId);
        if (lineItemOptional.isEmpty() || !lineItemOptional.get().getExpenseHeader().getId().equals(headerId)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Line item not found or does not belong to the specified expense header!"));
        }

        ExpenseLineItem lineItem = lineItemOptional.get();
        expenseLineItemRepository.deleteById(lineItemId);

        BigDecimal newTotalAmount = expenseHeader.getLineItems().stream()
            .filter(item -> !item.getId().equals(lineItemId))
            .map(ExpenseLineItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        expenseHeader.setTotalAmount(newTotalAmount);
        expenseHeaderRepository.save(expenseHeader);

        return ResponseEntity.ok(new MessageResponse("Expense line item deleted successfully and total amount updated!"));
    }




 // **Edit an expense line item by header ID and line item ID**
    @PutMapping("/line/{headerId}/{lineItemId}")
    public ResponseEntity<?> editExpenseLineItem(
        @PathVariable Long headerId, 
        @PathVariable Long lineItemId, 
        @RequestBody ExpenseLineItem updatedLineItem
    ) {
        // Check if the expense header exists
        Optional<ExpenseHeader> expenseHeaderOptional = expenseHeaderRepository.findById(headerId);
        if (expenseHeaderOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense header not found!"));
        }

        ExpenseHeader expenseHeader = expenseHeaderOptional.get();

        // Prevent updates if the expense has already been submitted
        if (expenseHeader.isSubmitted()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Cannot edit line item because the expense has already been submitted!"));
        }

        // Check if the line item exists and belongs to the given expense header
        Optional<ExpenseLineItem> lineItemOptional = expenseLineItemRepository.findById(lineItemId);
        if (lineItemOptional.isEmpty() || !lineItemOptional.get().getExpenseHeader().getId().equals(headerId)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Line item not found or does not belong to the specified expense header!"));
        }

        // Get the existing line item
        ExpenseLineItem lineItem = lineItemOptional.get();

        // Update only the editable fields (excluding expenseType)
        lineItem.setDescription(updatedLineItem.getDescription());
        lineItem.setAmount(updatedLineItem.getAmount());
        lineItem.setCreatedAt(LocalDateTime.now()); // Optionally update the timestamp

        // Save the updated line item
        expenseLineItemRepository.save(lineItem);

        // Recalculate the total amount after updating this line item
        BigDecimal newTotalAmount = expenseHeader.getLineItems().stream()
            .map(ExpenseLineItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Update the total amount in the expense header
        expenseHeader.setTotalAmount(newTotalAmount);
        expenseHeaderRepository.save(expenseHeader);

        return ResponseEntity.ok(new MessageResponse("Expense line item updated successfully!"));
    }




}
