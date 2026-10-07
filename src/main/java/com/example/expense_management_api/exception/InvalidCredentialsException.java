package com.example.expense_management_api.exception;

public class InvalidCredentialsException extends RuntimeException{
	
	public InvalidCredentialsException() {
		super("Invalid email or password");
	}

}
