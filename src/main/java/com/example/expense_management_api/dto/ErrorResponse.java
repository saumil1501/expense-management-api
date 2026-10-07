package com.example.expense_management_api.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
	
	private int status;
	private String message;
	private Map<String, String> errors;
	private LocalDateTime timestamp;

}
