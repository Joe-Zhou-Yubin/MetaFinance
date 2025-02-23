package com.expense.claim.controller;

import com.expense.claim.dto.DepartmentDTO;
import com.expense.claim.dto.UserDTO;
import com.expense.claim.models.Department;
import com.expense.claim.models.User;
import com.expense.claim.repository.DepartmentRepository;
import com.expense.claim.repository.UserRepository;
import com.expense.claim.payload.response.MessageResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DepartmentController {

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private UserRepository userRepository;

    // Get all departments
    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentRepository.findAll().stream().map(department -> 
            new DepartmentDTO(
                department.getId(),
                department.getName(),
                new UserDTO(department.getHead().getId(), department.getHead().getUsername())
            )
        ).collect(Collectors.toList());
        return ResponseEntity.ok(departments);
    }

    // Get department by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartmentById(@PathVariable Long id) {
        Optional<Department> department = departmentRepository.findById(id);
        if (department.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Department not found!"));
        }

        Department dept = department.get();
        DepartmentDTO departmentDTO = new DepartmentDTO(
            dept.getId(),
            dept.getName(),
            new UserDTO(dept.getHead().getId(), dept.getHead().getUsername())
        );

        return ResponseEntity.ok(departmentDTO);
    }


    // Create a new department
    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody Department department) {
        if (departmentRepository.findByName(department.getName()).isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Department already exists!"));
        }

        // NEW: Load the user from the database before assigning as head
        if (department.getHead() != null && department.getHead().getId() != null) {
            Optional<User> userOptional = userRepository.findById(department.getHead().getId());
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: User not found!"));
            }
            department.setHead(userOptional.get());
        }

        departmentRepository.save(department);
        return ResponseEntity.ok(new MessageResponse("Department created successfully!"));
    }


    // Update department details
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @RequestBody Department updatedDepartment) {
        Optional<Department> department = departmentRepository.findById(id);
        if (department.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Department not found!"));
        }

        Department existingDepartment = department.get();
        existingDepartment.setName(updatedDepartment.getName());
        existingDepartment.setHead(updatedDepartment.getHead());
        departmentRepository.save(existingDepartment);

        return ResponseEntity.ok(new MessageResponse("Department updated successfully!"));
    }

    // Delete a department
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        if (!departmentRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Department not found!"));
        }

        departmentRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("Department deleted successfully!"));
    }
}
