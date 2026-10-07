package com.example.expense_management_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
	
	private String token;
	private UserResponse user;

}
