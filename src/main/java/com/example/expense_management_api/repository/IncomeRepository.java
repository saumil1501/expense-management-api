package com.example.expense_management_api.repository;

import java.util.List;
import java.util.Optional;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.expense_management_api.entity.Income;

public interface IncomeRepository
        extends JpaRepository<Income, Long> {

    List<Income> findByUserIdOrderByIncomeDateDesc(Long userId);

    Optional<Income> findByIdAndUserId(
            Long id,
            Long userId
    );
    
    @Query("""
    	    SELECT COALESCE(SUM(i.amount), 0)
    	    FROM Income i
    	    WHERE i.user.id = :userId
    	      AND i.incomeDate BETWEEN :startDate AND :endDate
    	""")
    	BigDecimal calculateTotalIncome(
    	        @Param("userId") Long userId,
    	        @Param("startDate") LocalDate startDate,
    	        @Param("endDate") LocalDate endDate);

    	long countByUserIdAndIncomeDateBetween(
    	        Long userId,
    	        LocalDate startDate,
    	        LocalDate endDate);
}