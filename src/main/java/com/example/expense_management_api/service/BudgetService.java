package com.example.expense_management_api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expense_management_api.dto.BudgetRequest;
import com.example.expense_management_api.dto.BudgetResponse;
import com.example.expense_management_api.exception.BudgetNotFoundException;
import com.example.expense_management_api.exception.CategoryNotFoundException;
import com.example.expense_management_api.exception.DuplicateResourceException;
import com.example.expense_management_api.entity.Budget;
import com.example.expense_management_api.entity.Category;
import com.example.expense_management_api.entity.User;
import com.example.expense_management_api.repository.BudgetRepository;
import com.example.expense_management_api.repository.CategoryRepository;
import com.example.expense_management_api.repository.ExpenseRepository;
import com.example.expense_management_api.repository.UserRepository;
import com.example.expense_management_api.security.SecurityUtils;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            CategoryRepository categoryRepository,
            ExpenseRepository expenseRepository,
            UserRepository userRepository) {

        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BudgetResponse createBudget(
            BudgetRequest request) {

        User user = getCurrentUser();

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                    new CategoryNotFoundException(
                        request.getCategoryId()));

        boolean exists =
                budgetRepository
                    .existsByUserIdAndCategoryIdAndMonthAndYear(
                        user.getId(),
                        category.getId(),
                        request.getMonth(),
                        request.getYear());

        if (exists) {
            throw new DuplicateResourceException(
                "Budget already exists for this category and month");
        }

        Budget budget = Budget.builder()
                .amount(request.getAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .category(category)
                .user(user)
                .build();

        return mapToResponse(
                budgetRepository.save(budget));
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(
            Integer month,
            Integer year) {

        User user = getCurrentUser();

        return budgetRepository
                .findByUserIdAndMonthAndYear(
                        user.getId(),
                        month,
                        year)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long id) {

        User user = getCurrentUser();

        Budget budget = budgetRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new BudgetNotFoundException(id));

        return mapToResponse(budget);
    }

    @Transactional
    public BudgetResponse updateBudget(
            Long id,
            BudgetRequest request) {

        User user = getCurrentUser();

        Budget budget = budgetRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new BudgetNotFoundException(id));

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                    new CategoryNotFoundException(
                        request.getCategoryId()));

        budget.setAmount(request.getAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
        budget.setCategory(category);

        return mapToResponse(
                budgetRepository.save(budget));
    }

    @Transactional
    public void deleteBudget(Long id) {

        User user = getCurrentUser();

        Budget budget = budgetRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(() ->
                        new BudgetNotFoundException(id));

        budgetRepository.delete(budget);
    }

    private BudgetResponse mapToResponse(Budget budget) {

        YearMonth yearMonth =
                YearMonth.of(
                    budget.getYear(),
                    budget.getMonth());

        LocalDate startDate =
                yearMonth.atDay(1);

        LocalDate endDate =
                yearMonth.atEndOfMonth();

        BigDecimal spent =
                expenseRepository.calculateTotalSpent(
                    budget.getUser().getId(),
                    budget.getCategory().getId(),
                    startDate,
                    endDate);

        BigDecimal remaining =
                budget.getAmount().subtract(spent);

        BigDecimal percentage =
                spent
                    .multiply(BigDecimal.valueOf(100))
                    .divide(
                        budget.getAmount(),
                        2,
                        RoundingMode.HALF_UP);

        String status =
                spent.compareTo(budget.getAmount()) > 0
                    ? "EXCEEDED"
                    : "WITHIN_BUDGET";

        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getAmount(),
                spent,
                remaining,
                percentage,
                status,
                budget.getMonth(),
                budget.getYear()
        );
    }

    private User getCurrentUser() {

        String email =
                SecurityUtils.getCurrentUserEmail();

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Authenticated user not found"));
    }
}