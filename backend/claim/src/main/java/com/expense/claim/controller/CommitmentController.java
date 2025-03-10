package com.expense.claim.controller;

import com.expense.claim.models.Commitment;
import com.expense.claim.models.Department;
import com.expense.claim.models.RequestStatus;
import com.expense.claim.models.User;
import com.expense.claim.models.UserDepartment;
import com.expense.claim.models.ApproverRequest;
import com.expense.claim.payload.response.MessageResponse;
import com.expense.claim.payload.response.UserInfoResponse;
import com.expense.claim.repository.ApproverRequestRepository;
import com.expense.claim.repository.CommitmentRepository;
import com.expense.claim.repository.DepartmentRepository;
import com.expense.claim.repository.UserDepartmentRepository;
import com.expense.claim.repository.UserRepository;
import com.expense.claim.service.ApproverDerivationService;
import com.expense.claim.payload.response.CommitmentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/commitments")
public class CommitmentController {

    @Autowired
    private CommitmentRepository commitmentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private UserController userController;
    
    @Autowired
    private UserDepartmentRepository userDepartmentRepository;

    @Autowired
    private ApproverRequestRepository approverRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ApproverDerivationService approverDerivationService;
    
 // **Create a new commitment**
    @PostMapping
    public ResponseEntity<?> createCommitment(@Valid @RequestBody Commitment commitmentRequest) {
        ResponseEntity<UserInfoResponse> currentUserResponse = userController.getCurrentUserInfo();

        if (currentUserResponse.getStatusCode().is4xxClientError()) {
            System.out.println("Error: Unauthorized access - Could not retrieve current user.");
            return ResponseEntity.status(401).body(new MessageResponse("Error: Unauthorized!"));
        }

        UserInfoResponse currentUser = currentUserResponse.getBody();
        if (currentUser == null) {
            System.out.println("Error: Unable to retrieve user information.");
            return ResponseEntity.status(500).body(new MessageResponse("Error: Unable to retrieve user information."));
        }

        System.out.println("Current User ID: " + currentUser.getId());

        Optional<UserDepartment> userDepartmentOptional = userDepartmentRepository.findByUserId(currentUser.getId());

        if (userDepartmentOptional.isEmpty()) {
            System.out.println("Error: No department mapping found for User ID: " + currentUser.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: No department mapping found for the user."));
        }

        UserDepartment userDepartment = userDepartmentOptional.get();

        System.out.println("Department found: " + userDepartment.getDepartment().getName() +
                           " (Department ID: " + userDepartment.getDepartment().getId() + ")");

        // Set commitment details
        commitmentRequest.setRequestor(userDepartment.getUser());
        commitmentRequest.setDepartment(userDepartment.getDepartment());
        commitmentRequest.setCreatedAt(LocalDateTime.now());
        commitmentRequest.setApproved(false);
        commitmentRequest.setPaid(false);

        Commitment commitment = commitmentRepository.save(commitmentRequest);

        // Use ApprovalMatrix to derive the approver dynamically
        Optional<Long> approverIdOptional = approverDerivationService.deriveApprover(currentUser.getId(), "Commitment");
        if (approverIdOptional.isEmpty()) {
            System.out.println("Error: No approver found for Commitment approval.");
            return ResponseEntity.badRequest().body(new MessageResponse("Error: No approver found for Commitment approval."));
        }

        Long approverId = approverIdOptional.get();
        Optional<User> approverOptional = userRepository.findById(approverId);

        if (approverOptional.isEmpty()) {
            System.out.println("Error: Approver user not found with ID: " + approverId);
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Approver user not found."));
        }

        User approver = approverOptional.get();
        System.out.println("Approver ID: " + approver.getId() + " - " + approver.getUsername());

        // ✅ Create and save an approver request
        ApproverRequest approverRequest = new ApproverRequest();
        approverRequest.setRequestor(userDepartment.getUser());
        approverRequest.setApprover(approver);
        approverRequest.setType("Commitment Approval");
        approverRequest.setReferenceId(commitment.getId());
        approverRequest.setStatus(RequestStatus.PENDING);
        approverRequest.setCreatedAt(LocalDateTime.now());

        approverRequestRepository.save(approverRequest);

        // ✅ Return DTO response
        CommitmentResponse response = new CommitmentResponse(
            commitment.getId(),
            commitment.getRequestor().getUsername(),
            commitment.getDepartment().getName(),
            commitment.getDescription(),
            commitment.getAmount(),
            commitment.isApproved(),
            commitment.isPaid(),
            commitment.getCreatedAt(),
            commitment.getApprovedAt()
        );

        return ResponseEntity.ok(response);
    }




    // **Update a commitment**
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCommitment(@PathVariable Long id, @Valid @RequestBody Commitment commitmentRequest) {
        Optional<Commitment> existingCommitment = commitmentRepository.findById(id);
        if (existingCommitment.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Commitment not found!"));
        }

        Commitment commitment = existingCommitment.get();
        commitment.setDescription(commitmentRequest.getDescription());
        commitment.setAmount(commitmentRequest.getAmount());
        commitment.setApproved(commitmentRequest.isApproved());
        commitment.setPaid(commitmentRequest.isPaid());
        commitment.setApprovedAt(commitmentRequest.isApproved() ? LocalDateTime.now() : null);

        commitmentRepository.save(commitment);
        return ResponseEntity.ok(new MessageResponse("Commitment updated successfully!"));
    }

 // **Delete a commitment**
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCommitment(@PathVariable Long id) {
        if (!commitmentRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Commitment not found!"));
        }

        // Find and delete all approval requests associated with this commitment
        List<ApproverRequest> approvalRequests = approverRequestRepository.findByTypeAndReferenceId("Commitment Approval", id);
        
        if (!approvalRequests.isEmpty()) {
            approverRequestRepository.deleteAll(approvalRequests);
        }

        // Now delete the commitment
        commitmentRepository.deleteById(id);

        return ResponseEntity.ok(new MessageResponse("Commitment and associated approval request(s) deleted successfully!"));
    }



 // **Get all commitments**
    @GetMapping
    public ResponseEntity<List<CommitmentResponse>> getAllCommitments() {
        List<CommitmentResponse> responses = commitmentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // **Get a commitment by ID**
    @GetMapping("/{id}")
    public ResponseEntity<?> getCommitmentById(@PathVariable Long id) {
        Optional<Commitment> commitment = commitmentRepository.findById(id);
        if (commitment.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Commitment not found!"));
        }

        return ResponseEntity.ok(mapToDto(commitment.get()));
    }

    // **Get all commitments by department**
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<CommitmentResponse>> getCommitmentsByDepartmentId(@PathVariable Long departmentId) {
        List<CommitmentResponse> responses = commitmentRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // **Get all approved commitments**
    @GetMapping("/approved")
    public ResponseEntity<List<CommitmentResponse>> getApprovedCommitments() {
        List<CommitmentResponse> responses = commitmentRepository.findByApproved(true).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // **Get all unpaid commitments**
    @GetMapping("/unpaid")
    public ResponseEntity<List<CommitmentResponse>> getUnpaidCommitments() {
        List<CommitmentResponse> responses = commitmentRepository.findByPaid(false).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // **Helper method to map Commitment to CommitmentResponse**
    private CommitmentResponse mapToDto(Commitment commitment) {
        return new CommitmentResponse(
                commitment.getId(),
                commitment.getRequestor().getUsername(),
                commitment.getDepartment().getName(),
                commitment.getDescription(),
                commitment.getAmount(),
                commitment.isApproved(),
                commitment.isPaid(),
                commitment.getCreatedAt(),
                commitment.getApprovedAt()
        );
    }
    
    
 // **Get total commitments by department**
    @GetMapping("/department/total-unpaid-approved/{departmentId}")
    public ResponseEntity<?> getTotalUnpaidApprovedCommitmentsByDepartment(@PathVariable Long departmentId) {
        // Fetch commitments filtered by department, approved status, and unpaid status
        List<Commitment> commitments = commitmentRepository.findByDepartmentIdAndApprovedAndPaid(departmentId, true, false);

        // Calculate the total amount
        BigDecimal totalAmount = commitments.stream()
                .map(Commitment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Return the response
        return ResponseEntity.ok(totalAmount);
    }
    @GetMapping("/departments/total-unpaid-approved")
    public ResponseEntity<Map<String, String>> getTotalUnpaidApprovedCommitmentsByDepartments() {
        // Fetch all commitments filtered by approved and unpaid status
        List<Commitment> commitments = commitmentRepository.findByApprovedAndPaid(true, false);

        // Group commitments by department and calculate total amount per department
        Map<String, BigDecimal> totalsByDepartment = commitments.stream()
                .collect(Collectors.groupingBy(
                        commitment -> commitment.getDepartment().getName(),  // Group by department name
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Commitment::getAmount,
                                BigDecimal::add
                        )
                ));

        // Convert the result to string format for response
        Map<String, String> response = totalsByDepartment.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,                   // Department name
                        entry -> entry.getValue().toPlainString()  // Total amount as string
                ));

        return ResponseEntity.ok(response);
    }

}
