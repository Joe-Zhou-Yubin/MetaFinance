package com.expense.claim.controller;

import com.expense.claim.models.ApprovalMatrix;
import com.expense.claim.payload.response.MessageResponse;
import com.expense.claim.repository.ApprovalMatrixRepository;
import com.expense.claim.repository.DepartmentRepository;
import com.expense.claim.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/approval-matrix")
public class ApprovalMatrixController {

    @Autowired
    private ApprovalMatrixRepository approvalMatrixRepository;

    // **Get All Approval Matrix Entries**
    @GetMapping
    public ResponseEntity<?> getAllApprovalMatrices() {
        List<ApprovalMatrix> matrices = approvalMatrixRepository.findAll();
        return ResponseEntity.ok(matrices);
    }

 // **Get Approval Matrix by ID**
    @GetMapping("/{id}")
    public ResponseEntity<?> getApprovalMatrixById(@PathVariable Long id) {
        Optional<ApprovalMatrix> matrix = approvalMatrixRepository.findById(id);
        if (matrix.isPresent()) {
            return ResponseEntity.ok(matrix.get());
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Approval matrix not found."));
        }
    }


    // **Get Approval Matrix by Event Type and Level**
    @GetMapping("/event")
    public ResponseEntity<?> getApprovalMatrixByEventAndLevel(@RequestParam String approvalEventType, @RequestParam Integer level) {
        List<ApprovalMatrix> matrices = approvalMatrixRepository.findByApprovalEventTypeAndLevel(approvalEventType, level);
        if (matrices.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: No approval matrix found for the provided event type and level."));
        }
        return ResponseEntity.ok(matrices);
    }

    // **Create Approval Matrix Entry**
    @PostMapping
    public ResponseEntity<?> createApprovalMatrices(@Valid @RequestBody List<ApprovalMatrix> approvalMatrices) {
        if (approvalMatrices.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: No approval matrices provided."));
        }

        List<ApprovalMatrix> savedMatrices = approvalMatrixRepository.saveAll(approvalMatrices);

        return ResponseEntity.ok(savedMatrices);
    }


    // **Update Approval Matrix Entry**
    @PutMapping("/{id}")
    public ResponseEntity<?> updateApprovalMatrix(@PathVariable Long id, @RequestBody ApprovalMatrix approvalMatrix) {
        if (!approvalMatrixRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Approval matrix not found."));
        }
        approvalMatrix.setId(id);
        ApprovalMatrix updatedMatrix = approvalMatrixRepository.save(approvalMatrix);
        return ResponseEntity.ok(updatedMatrix);
    }

    // **Delete Approval Matrix Entry**
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApprovalMatrix(@PathVariable Long id) {
        if (!approvalMatrixRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Approval matrix not found."));
        }
        approvalMatrixRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Approval matrix deleted successfully."));
    }
    
    

}
