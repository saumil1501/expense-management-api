package com.example.expense_management_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.expense_management_api.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	Optional<User> findByEmailIgnoreCase(String email);
	
	boolean existsByEmailIgnoreCase(String email);

}
