package com.example.expense_management_api.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.expense_management_api.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

	Optional<Expense> findByIdAndUserId(Long id, Long userId);
	
	boolean existsByCategoryId(Long categoryId);
	
}
