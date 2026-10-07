package com.example.expense_management_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.expense_management_api.entity.Budget;

public interface BudgetRepository
        extends JpaRepository<Budget, Long> {

    Optional<Budget> findByIdAndUserId(
            Long id,
            Long userId);

    List<Budget> findByUserIdAndMonthAndYear(
            Long userId,
            Integer month,
            Integer year);

    boolean existsByUserIdAndCategoryIdAndMonthAndYear(
            Long userId,
            Long categoryId,
            Integer month,
            Integer year);
}