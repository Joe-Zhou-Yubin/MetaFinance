package com.expense.claim.controller;

import com.expense.claim.models.UserDepartment;
import com.expense.claim.repository.UserDepartmentRepository;
import com.expense.claim.payload.response.MessageResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user-departments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserDepartmentController {

    @Autowired
    private UserDepartmentRepository userDepartmentRepository;

    // Get all user-department mappings
    @GetMapping
    public ResponseEntity<List<UserDepartment>> getAllUserDepartments() {
        return ResponseEntity.ok(userDepartmentRepository.findAll());
    }

    // Get a mapping by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserDepartmentById(@PathVariable Long id) {
        Optional<UserDepartment> userDepartment = userDepartmentRepository.findById(id);
        if (userDepartment.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User-Department mapping not found!"));
        }
        return ResponseEntity.ok(userDepartment.get());
    }

    // Create a new user-department mapping
    @PostMapping
    public ResponseEntity<?> createUserDepartment(@RequestBody UserDepartment userDepartment) {
        userDepartmentRepository.save(userDepartment);
        return ResponseEntity.ok(new MessageResponse("User-Department mapping created successfully!"));
    }

    // Update user-department mapping
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserDepartment(@PathVariable Long id, @RequestBody UserDepartment updatedUserDepartment) {
        Optional<UserDepartment> userDepartment = userDepartmentRepository.findById(id);
        if (userDepartment.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User-Department mapping not found!"));
        }

        UserDepartment existingUserDepartment = userDepartment.get();
        existingUserDepartment.setUser(updatedUserDepartment.getUser());
        existingUserDepartment.setDepartment(updatedUserDepartment.getDepartment());
        userDepartmentRepository.save(existingUserDepartment);

        return ResponseEntity.ok(new MessageResponse("User-Department mapping updated successfully!"));
    }

    // Delete a user-department mapping
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserDepartment(@PathVariable Long id) {
        if (!userDepartmentRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: User-Department mapping not found!"));
        }

        userDepartmentRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("User-Department mapping deleted successfully!"));
    }
}
