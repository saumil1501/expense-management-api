package com.example.expense_management_api.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.expense_management_api.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

	Optional<Expense> findByIdAndUserId(Long id, Long userId);
	
	boolean existsByCategoryId(Long categoryId);
	
	@Query("""
		    SELECT COALESCE(SUM(e.amount), 0)
		    FROM Expense e
		    WHERE e.user.id = :userId
		      AND e.category.id = :categoryId
		      AND e.expenseDate BETWEEN :startDate AND :endDate
		""")
		BigDecimal calculateTotalSpent(
		        @Param("userId") Long userId,
		        @Param("categoryId") Long categoryId,
		        @Param("startDate") LocalDate startDate,
		        @Param("endDate") LocalDate endDate);
	
	@Query("""
		    SELECT COALESCE(SUM(e.amount), 0)
		    FROM Expense e
		    WHERE e.user.id = :userId
		      AND e.expenseDate BETWEEN :startDate AND :endDate
		""")
		BigDecimal calculateTotalExpenses(
		        @Param("userId") Long userId,
		        @Param("startDate") LocalDate startDate,
		        @Param("endDate") LocalDate endDate);
	
	long countByUserIdAndExpenseDateBetween(
	        Long userId,
	        LocalDate startDate,
	        LocalDate endDate);
	
}
