package com.example.expense_management_api.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

	private Long id;
	private String name;
	private String email;
	private LocalDateTime createdAt;
	
}
