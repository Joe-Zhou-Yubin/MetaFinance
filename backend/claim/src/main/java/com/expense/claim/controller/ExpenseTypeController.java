package com.expense.claim.controller;

import com.expense.claim.models.ExpenseType;
import com.expense.claim.payload.response.MessageResponse;
import com.expense.claim.repository.ExpenseTypeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/expense-types")
public class ExpenseTypeController {

    @Autowired
    private ExpenseTypeRepository expenseTypeRepository;

    // **Create a new expense type**
    @PostMapping
    public ResponseEntity<?> createExpenseType(@Valid @RequestBody ExpenseType expenseTypeRequest) {
        expenseTypeRepository.save(expenseTypeRequest);
        return ResponseEntity.ok(new MessageResponse("Expense type created successfully!"));
    }

    // **Get all expense types**
    @GetMapping
    public ResponseEntity<List<ExpenseType>> getAllExpenseTypes() {
        return ResponseEntity.ok(expenseTypeRepository.findAll());
    }

    // **Get an expense type by ID**
    @GetMapping("/{id}")
    public ResponseEntity<?> getExpenseTypeById(@PathVariable Long id) {
        Optional<ExpenseType> expenseType = expenseTypeRepository.findById(id);
        if (expenseType.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense type not found!"));
        }
        return ResponseEntity.ok(expenseType.get());
    }

    // **Update an expense type by ID**
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpenseType(@PathVariable Long id, @Valid @RequestBody ExpenseType expenseTypeRequest) {
        Optional<ExpenseType> expenseTypeOptional = expenseTypeRepository.findById(id);
        if (expenseTypeOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense type not found!"));
        }

        ExpenseType expenseType = expenseTypeOptional.get();
        expenseType.setName(expenseTypeRequest.getName());
        expenseType.setDescription(expenseTypeRequest.getDescription());

        expenseTypeRepository.save(expenseType);
        return ResponseEntity.ok(new MessageResponse("Expense type updated successfully!"));
    }

    // **Delete an expense type by ID**
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpenseType(@PathVariable Long id) {
        if (!expenseTypeRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Expense type not found!"));
        }

        expenseTypeRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Expense type deleted successfully!"));
    }
}
