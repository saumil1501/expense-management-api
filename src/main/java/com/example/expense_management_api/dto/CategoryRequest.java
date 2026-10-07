package com.example.expense_management_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequest {
	
	@NotBlank(message = "category name is required")
	@Size(
			max = 50,
			message = "category name cannot exceed 50 characters"
			)
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}
