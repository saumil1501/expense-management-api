package com.example.expense_management_api.controller;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.expense_management_api.dto.ExpenseRequest;
import com.example.expense_management_api.dto.ExpenseResponse;
import com.example.expense_management_api.dto.PagedResponse;
import com.example.expense_management_api.entity.Expense;
import com.example.expense_management_api.service.ExpenseService;

import jakarta.validation.Valid;

@Tag(
	    name = "Expenses",
	    description = "Create, retrieve, update, delete and search expenses"
	)
	@RestController
	@RequestMapping("/api/expenses")
public class ExpenseController {
	
	private final ExpenseService expenseService;
	
	public ExpenseController(ExpenseService expenseService) {
		this.expenseService = expenseService;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ExpenseResponse createExpense(@Valid @RequestBody ExpenseRequest request) {
		return expenseService.createExpense(request);
	}
	
	@GetMapping
	public PagedResponse<ExpenseResponse> getAllExpenses(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "expenseDate") String sortBy,
			@RequestParam(defaultValue = "desc") String direction,
			@RequestParam(required = false) String category,
			@RequestParam(required = false)
	        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	        LocalDate startDate,
	        @RequestParam(required = false)
	        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	        LocalDate endDate,
	        @RequestParam(required = false) BigDecimal minAmount,
	        @RequestParam(required = false) BigDecimal maxAmount,
	        @RequestParam(required = false) String search) {
		return expenseService.getAllExpenses(
				page,
				size,
				sortBy,
				direction,
				category,
				startDate,
	            endDate,
	            minAmount,
	            maxAmount,
	            search);
	}
	
	@GetMapping("/{id}")
	public ExpenseResponse getExpenseById(@PathVariable Long id) {
		return expenseService.getExpenseById(id);
	}
	
	@PutMapping("/{id}")
	public ExpenseResponse updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
		return expenseService.updateExpense(id, request);
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteExpense(@PathVariable Long id) {
		expenseService.deleteExpense(id);
	}

}
