package com.expense.claim.controller;

import com.expense.claim.models.Department;
import com.expense.claim.models.ERole;
import com.expense.claim.models.Role;
import com.expense.claim.models.User;
import com.expense.claim.payload.response.UserInfoResponse;
import com.expense.claim.repository.DepartmentRepository;
import com.expense.claim.repository.RoleRepository;
import com.expense.claim.repository.UserRepository;
import com.expense.claim.security.jwt.JwtUtils;
import com.expense.claim.security.services.UserDetailsImpl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usercontroller")
public class UserController {
	
	@Autowired
    private DepartmentRepository departmentRepository;
	
	@Autowired
    private RoleRepository roleRepository;
	
	@Autowired
    private UserRepository userRepository;
	
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUserInfo() {
        // Retrieve the current authentication from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(null);  // Return 401 Unauthorized if no user is authenticated
        }

        // Get the authenticated user details
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Create and return the response with user information
        UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/department-head/{departmentId}")
    public ResponseEntity<?> getDepartmentHead(@PathVariable Long departmentId) {
        // Find the department by ID
        Optional<Department> department = departmentRepository.findById(departmentId);
        if (department.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Department not found!");
        }

        // Get the head of the department
        User head = department.get().getHead();
        if (head == null) {
            return ResponseEntity.badRequest().body("Error: Department head not assigned!");
        }

        // Create and return the response with the department head's user information
        UserInfoResponse response = new UserInfoResponse(head.getId(), head.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin")
    public ResponseEntity<?> getAdminUser() {
        // Find the admin role in the database
        Optional<Role> adminRole = roleRepository.findByName(ERole.ROLE_ADMIN);
        if (adminRole.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Admin role not found!");
        }

        // Find the user with the admin role
        Optional<User> adminUser = userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(adminRole.get()))
                .findFirst();

        if (adminUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Admin user not found!");
        }

        // Return the admin user information
        User user = adminUser.get();
        UserInfoResponse response = new UserInfoResponse(user.getId(), user.getUsername());
        return ResponseEntity.ok(response);
    }
}
