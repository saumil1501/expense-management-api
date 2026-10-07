package com.example.expense_management_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.expense_management_api.dto.AuthResponse;
import com.example.expense_management_api.dto.LoginRequest;
import com.example.expense_management_api.dto.RegisterRequest;
import com.example.expense_management_api.dto.UserResponse;
import com.example.expense_management_api.service.UserService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(
	    name = "Authentication",
	    description = "User registration and authentication"
	)
	@RestController
	@RequestMapping("/api/auth")
public class AuthController {
	
	private final UserService userService;
	
	public AuthController(UserService userService) {
		this.userService = userService;
	}
	
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse register(@Valid @RequestBody RegisterRequest request) {
		return userService.registerUser(request);
	}
	
	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		
		return userService.loginUser(request);
		
	}

}
