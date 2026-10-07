package com.example.expense_management_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ExpenseRequest {
	
	@NotBlank(message = "Title is required")
	private String title;
	
	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.01", message = "Amount must be greater than 0")
	private BigDecimal amount;
	
	private String description;
	
	@NotNull(message = "Category ID is required")
	private Long categoryId;
	
	@NotNull(message = "Expense Date is required")
	private LocalDate expenseDate;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public LocalDate getExpenseDate() {
		return expenseDate;
	}

	public void setExpenseDate(LocalDate expenseDate) {
		this.expenseDate = expenseDate;
	}
	
	

}
