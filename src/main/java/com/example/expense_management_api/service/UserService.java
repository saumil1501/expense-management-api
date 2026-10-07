package com.example.expense_management_api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.expense_management_api.dto.AuthResponse;
import com.example.expense_management_api.dto.LoginRequest;
import com.example.expense_management_api.dto.RegisterRequest;
import com.example.expense_management_api.dto.UserResponse;
import com.example.expense_management_api.entity.User;
import com.example.expense_management_api.exception.DuplicateResourceException;
import com.example.expense_management_api.exception.InvalidCredentialsException;
import com.example.expense_management_api.repository.UserRepository;
import com.example.expense_management_api.security.JwtService;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	
	public UserService(UserRepository userRepository,
						PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}
	
	public UserResponse registerUser(RegisterRequest request) {
		String email = request.getEmail().trim().toLowerCase();
		
		if(userRepository.existsByEmailIgnoreCase(email)) {
			throw new DuplicateResourceException("User already exists with email: " + email);
		}
		
		User user = new User();
		
		user.setName(request.getName().trim());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		
		User savedUser = userRepository.save(user);
		
		return mapToResponse(savedUser);
	}

	private UserResponse mapToResponse(User user) {
		return new UserResponse(user.getId(), 
								user.getName(), 
								user.getEmail(), 
								user.getCreatedAt());
	}
	
	public AuthResponse loginUser(LoginRequest request) {
		
		String email = request.getEmail().trim().toLowerCase();
		
		User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(InvalidCredentialsException::new);
		
		if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			
			throw new InvalidCredentialsException();
		}
		
		String token = jwtService.generateToken(user);
		
		UserResponse userResponse = mapToResponse(user);
		
		return new AuthResponse(token, userResponse);
		
	}

}
