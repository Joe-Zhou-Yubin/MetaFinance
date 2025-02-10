package com.expense.claim.controller;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.expense.claim.models.ERole;
import com.expense.claim.models.Role;
import com.expense.claim.models.User;
import com.expense.claim.payload.request.LoginRequest;
import com.expense.claim.payload.request.SignupRequest;
import com.expense.claim.payload.response.JwtResponse;
import com.expense.claim.payload.response.MessageResponse;
import com.expense.claim.repository.RoleRepository;
import com.expense.claim.repository.UserRepository;
import com.expense.claim.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import com.expense.claim.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;
  
//Handle user login
  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
	  
      // Authenticate the user using Spring Security
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
    
    // Set the authenticated user's context
    SecurityContextHolder.getContext().setAuthentication(authentication);
    
    // Generate a JWT token for the authenticated user
    String jwt = jwtUtils.generateJwtToken(authentication);
    
    // Extract user details from the authenticated user
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();    
    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());
    
    // Return the JWT token and user information in the response
    return ResponseEntity.ok(new JwtResponse(jwt, 
                         userDetails.getId(), 
                         userDetails.getUsername(), 
                         userDetails.getEmail(), 
                         roles));
  }
  
  // Handle user registration
  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
//      System.out.println("Received signup request for username: " + signUpRequest.getUsername());
      
      // Check if the username is already taken
      if (userRepository.existsByUsername(signUpRequest.getUsername())) {
//          System.out.println("Username is already taken.");
          return ResponseEntity
              .badRequest()
              .body(new MessageResponse("Error: Username is already taken!"));
      }
      
      // Check if the email is already in use
      if (userRepository.existsByEmail(signUpRequest.getEmail())) {
//          System.out.println("Email is already in use.");
          return ResponseEntity
              .badRequest()
              .body(new MessageResponse("Error: Email is already in use!"));
      }

      // Create new user's account
      User user = new User(signUpRequest.getUsername(), 
                 signUpRequest.getEmail(),
                 encoder.encode(signUpRequest.getPassword()));

      Set<String> strRoles = signUpRequest.getRole();
      Set<Role> roles = new HashSet<>();

      if (strRoles == null) {
//          System.out.println("No roles specified, assigning default role.");
          
          // Assign a default role if no roles are specified
          Role userRole = roleRepository.findByName(ERole.ROLE_MEMBER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(userRole);
      } else {
//          System.out.println("Assigning roles: " + strRoles);
          // Assign roles based on the provided role names
          strRoles.forEach(role -> {
              switch (role) {
                  case "admin":
                      Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                      roles.add(adminRole);

                      break;
                  default:
                      Role userRole = roleRepository.findByName(ERole.ROLE_MEMBER)
                          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                      roles.add(userRole);
              }
          });
      }

      user.setRoles(roles);
      userRepository.save(user);

//      System.out.println("User registered successfully!");
      return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  // Get a list of all users
  @GetMapping("/users")
  public ResponseEntity<List<User>> getAllUsers() {
    List<User> users = userRepository.findAll();
    return ResponseEntity.ok(users);
  }
  
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<?> deleteUserById(@PathVariable Long id) {
      // Check if the user exists
      if (!userRepository.existsById(id)) {
          return ResponseEntity
                  .badRequest()
                  .body(new MessageResponse("Error: User not found with id " + id));
      }

      // Delete the user
      userRepository.deleteById(id);

      return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
  }
  
}