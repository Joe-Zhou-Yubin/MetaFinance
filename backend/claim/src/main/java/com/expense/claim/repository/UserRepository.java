package com.expense.claim.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.expense.claim.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);
	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);
	
	@Query("SELECT d.head.id FROM UserDepartment ud " +
		       "JOIN ud.department d " +
		       "WHERE ud.user.id = :userId")
	Optional<Long> findDepartmentHeadByUserId(@Param("userId") Long userId);

	
	

}