package com.example.expense_management_api.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.expense_management_api.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleMalformedRequest(Exception exception) {
		return new ErrorResponse(400, "Malformed request body or parameter", null, LocalDateTime.now());
	}

	@ExceptionHandler(AuthenticationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleAuthenticationException(AuthenticationException exception) {
		return new ErrorResponse(401, "Authentication required", null, LocalDateTime.now());
	}
	
	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidationException(MethodArgumentNotValidException exception) {
		Map<String, String> errors = new HashMap<>();
		exception.getBindingResult()
				.getFieldErrors()
				.forEach(error -> errors.put(
						error.getField(), error.getDefaultMessage()));
		
		return new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Validation failed", 
				errors, 
				LocalDateTime.now());
		
	}
	
	@ExceptionHandler(ExpenseNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleExpenseNotFoundException(ExpenseNotFoundException exception) {
		return new ErrorResponse(HttpStatus.NOT_FOUND.value(),
				exception.getMessage(), 
				null, 
				LocalDateTime.now());
	}
	
	@ExceptionHandler(InvalidRequestException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleInvalidRequestException(InvalidRequestException exception) {
		return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), 
				exception.getMessage(), 
				null, 
				LocalDateTime.now());
	}
	
	@ExceptionHandler(DuplicateResourceException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponse handleDuplicateResourceException(DuplicateResourceException exception) {
		
		return new ErrorResponse(HttpStatus.CONFLICT.value(), exception.getMessage(), null, LocalDateTime.now());
	}
	
	@ExceptionHandler(CategoryNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponse handleCategoryNotFoundException(CategoryNotFoundException exception) {
		return new ErrorResponse(HttpStatus.NOT_FOUND.value(), exception.getMessage(), null, LocalDateTime.now()); 
	}
	
	@ExceptionHandler(InvalidCredentialsException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponse handleInvalidCredentialsException(InvalidCredentialsException exception) {
		
		return new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), exception.getMessage(), null, LocalDateTime.now());
	}

}
