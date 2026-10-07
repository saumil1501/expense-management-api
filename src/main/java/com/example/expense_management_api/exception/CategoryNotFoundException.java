package com.example.expense_management_api.exception;

public class CategoryNotFoundException extends RuntimeException{ 
	
	public CategoryNotFoundException(Long id) {
		super("Category not found with id: "+id);
	}

}
