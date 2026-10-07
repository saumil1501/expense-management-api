package com.example.expense_management_api.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import com.example.expense_management_api.dto.ExpenseRequest;
import com.example.expense_management_api.dto.ExpenseResponse;
import com.example.expense_management_api.dto.PagedResponse;
import com.example.expense_management_api.entity.Category;
import com.example.expense_management_api.entity.Expense;
import com.example.expense_management_api.entity.User;
import com.example.expense_management_api.exception.CategoryNotFoundException;
import com.example.expense_management_api.exception.ExpenseNotFoundException;
import com.example.expense_management_api.exception.InvalidRequestException;
import com.example.expense_management_api.repository.CategoryRepository;
import com.example.expense_management_api.repository.ExpenseRepository;
import com.example.expense_management_api.repository.UserRepository;
import com.example.expense_management_api.security.SecurityUtils;

import org.springframework.data.jpa.domain.Specification;

import static com.example.expense_management_api.specification.ExpenseSpecification.*;

@Service
@Transactional(readOnly = true)
public class ExpenseService {
	
	private static final List<String> ALLOWED_SORT_FIELDS = 
			List.of("id",
					"title",
					"amount",
					"expenseDate",
					"createdAt");
	
	private final ExpenseRepository expenseRepository;
	private final UserRepository userRepository;
	
	private final CategoryRepository categoryRepository;
	
	public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
		this.expenseRepository = expenseRepository;
		this.userRepository = userRepository;
		this.categoryRepository = categoryRepository;
	}
	
	private User getCurrentUser() {

	    String email =
	            SecurityUtils.getCurrentUserEmail();

	    return userRepository
	            .findByEmailIgnoreCase(email)
	            .orElseThrow(() ->
	                    new InsufficientAuthenticationException(
	                            "Authenticated user not found"
	                    )
	            );
	}
	
	@Transactional
	public ExpenseResponse createExpense(ExpenseRequest request) {
		Expense expense = new Expense();
		User currentUser = getCurrentUser();
		
		Category category = categoryRepository
		        .findById(request.getCategoryId())
		        .orElseThrow(() ->
		                new CategoryNotFoundException(
		                        request.getCategoryId()
		                )
		        );
		
		expense.setTitle(request.getTitle());
		expense.setAmount(request.getAmount());
		expense.setDescription(request.getDescription());
		expense.setCategory(category);
		expense.setExpenseDate(request.getExpenseDate());
		expense.setUser(currentUser);
		
		Expense savedExpense = expenseRepository.save(expense);
		
		return mapToResponse(savedExpense);
	}
	
	private ExpenseResponse mapToResponse(Expense expense) {
		
		ExpenseResponse response = new ExpenseResponse();
		
		
		
		response.setId(expense.getId());
        response.setTitle(expense.getTitle());
        response.setAmount(expense.getAmount());
        response.setDescription(expense.getDescription());
        response.setCategoryId(
                expense.getCategory().getId()
        );

        response.setCategoryName(
                expense.getCategory().getName()
        );
        response.setExpenseDate(expense.getExpenseDate());
        response.setCreatedAt(expense.getCreatedAt());
		
		return response;
	}

	public PagedResponse<ExpenseResponse> getAllExpenses(int page, int size, String sortBy, String direction, String category,  LocalDate startDate,
	        LocalDate endDate, BigDecimal minAmount, BigDecimal maxAmount, String search) {
		
		if(page<0) {
			throw new InvalidRequestException("Page no. cannot be negative");
		}
		
		if(size < 1 || size > 100) {
			throw new InvalidRequestException("Page size must be between 1 and 100");
		}
		
		if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
		    throw new InvalidRequestException(
		            "Invalid sort field: " + sortBy
		    );
		}
		
		if (!direction.equalsIgnoreCase("asc")
		        && !direction.equalsIgnoreCase("desc")) {

		    throw new InvalidRequestException(
		            "Sort direction must be 'asc' or 'desc'"
		    );
		}
		
		if (startDate != null
		        && endDate != null
		        && startDate.isAfter(endDate)) {

		    throw new InvalidRequestException(
		            "Start date cannot be after end date"
		    );
		}
		
		if(minAmount != null && minAmount.compareTo(BigDecimal.ZERO) < 0) {
			throw new InvalidRequestException("Minimum amount cannot be negative");
		}
		if(maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) < 0) {
			throw new InvalidRequestException("Maximum amount cannot be negative");
		}
		if (minAmount != null
		        && maxAmount != null
		        && minAmount.compareTo(maxAmount) > 0) {

		    throw new InvalidRequestException(
		            "Minimum amount cannot be greater than maximum amount"
		    );
		}
		
		Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
		
		Pageable pageable = PageRequest.of(page, size, sort);
		
		Specification<Expense> specification = ownedByUser(getCurrentUser().getId());
		
		if(category != null && !category.isBlank()) {
			specification = specification.and(hasCategory(category));
		}
		
		if(startDate != null) {
			specification = specification.and(dateOnOrAfter(startDate));
		}
		if(endDate != null) {
			specification = specification.and(dateOnOrBefore(endDate));
		}
		
		if(minAmount != null) {
			specification = specification.and(amountGreaterThanOrEqualTo(minAmount));
		}
		
		if(maxAmount != null) {
			specification = specification.and(amountLessThanOrEqualTo(maxAmount));
		}
		
		if (search != null && !search.isBlank()) {
		    specification =
		            specification.and(
		                    containsText(search)
		            );
		}
		
		Page<Expense> expensePage = expenseRepository.findAll(specification, pageable);


		
		List<ExpenseResponse> content = expensePage.getContent().stream().map(this::mapToResponse).toList();
		
		return new PagedResponse<>(
	            content,
	            expensePage.getNumber(),
	            expensePage.getSize(),
	            expensePage.getTotalElements(),
	            expensePage.getTotalPages(),
	            expensePage.isLast()
	            );
		
	}
	
	private Expense getOwnedExpense(Long id) {
		return expenseRepository.findByIdAndUserId(id, getCurrentUser().getId())
				.orElseThrow(() -> new ExpenseNotFoundException(id));
	}

	public ExpenseResponse getExpenseById(Long id) {
		Expense expense = getOwnedExpense(id);
		
		return mapToResponse(expense);
	}
	
	@Transactional
	public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
		Expense expense = getOwnedExpense(id);
		
		Category category = categoryRepository
		        .findById(request.getCategoryId())
		        .orElseThrow(() ->
		                new CategoryNotFoundException(
		                        request.getCategoryId()
		                )
		        );
		
		 	expense.setTitle(request.getTitle());
		    expense.setAmount(request.getAmount());
		    expense.setDescription(request.getDescription());
		    expense.setCategory(category);
		    expense.setExpenseDate(request.getExpenseDate());
		    
		    Expense updatedExpense = expenseRepository.save(expense);
		    
		    return mapToResponse(updatedExpense);
	}
	
	@Transactional
	public void deleteExpense(Long id) {
		Expense expense = getOwnedExpense(id);
		
		expenseRepository.delete(expense);
	}

}
