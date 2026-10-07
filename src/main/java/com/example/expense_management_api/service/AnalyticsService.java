package com.example.expense_management_api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expense_management_api.dto.BudgetResponse;
import com.example.expense_management_api.dto.CategoryExpenseResponse;
import com.example.expense_management_api.dto.FinancialSummaryResponse;
import com.example.expense_management_api.dto.MonthlyTrendResponse;
import com.example.expense_management_api.entity.User;
import com.example.expense_management_api.repository.ExpenseRepository;
import com.example.expense_management_api.repository.IncomeRepository;
import com.example.expense_management_api.repository.UserRepository;
import com.example.expense_management_api.security.SecurityUtils;

@Service
public class AnalyticsService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;
    private final BudgetService budgetService;

    public AnalyticsService(
            ExpenseRepository expenseRepository,
            IncomeRepository incomeRepository,
            UserRepository userRepository, BudgetService budgetService) {

        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
		this.budgetService = budgetService;
    }

    @Transactional(readOnly = true)
    public FinancialSummaryResponse getMonthlySummary(
            Integer month,
            Integer year) {

        User user = getCurrentUser();

        YearMonth yearMonth =
                YearMonth.of(year, month);

        LocalDate startDate =
                yearMonth.atDay(1);

        LocalDate endDate =
                yearMonth.atEndOfMonth();

        BigDecimal totalIncome =
                incomeRepository.calculateTotalIncome(
                        user.getId(),
                        startDate,
                        endDate);

        BigDecimal totalExpenses =
                expenseRepository.calculateTotalExpenses(
                        user.getId(),
                        startDate,
                        endDate);

        BigDecimal netBalance =
                totalIncome.subtract(totalExpenses);

        BigDecimal savingsRate = BigDecimal.ZERO;

        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {

            savingsRate = netBalance
                    .multiply(BigDecimal.valueOf(100))
                    .divide(
                        totalIncome,
                        2,
                        RoundingMode.HALF_UP
                    );
        }

        long expenseCount =
                expenseRepository
                    .countByUserIdAndExpenseDateBetween(
                        user.getId(),
                        startDate,
                        endDate);

        long incomeCount =
                incomeRepository
                    .countByUserIdAndIncomeDateBetween(
                        user.getId(),
                        startDate,
                        endDate);

        return new FinancialSummaryResponse(
                totalIncome,
                totalExpenses,
                netBalance,
                savingsRate,
                expenseCount + incomeCount,
                month,
                year
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
    
    
    @Transactional(readOnly = true)
    public List<CategoryExpenseResponse> getExpensesByCategory(
            Integer month,
            Integer year) {

        User user = getCurrentUser();

        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        BigDecimal total =
                expenseRepository.calculateTotalExpenses(
                        user.getId(),
                        startDate,
                        endDate);

        return expenseRepository
                .findExpensesByCategory(
                        user.getId(),
                        startDate,
                        endDate)
                .stream()
                .map(row -> {

                    Long categoryId = (Long) row[0];
                    String categoryName = (String) row[1];
                    BigDecimal amount = (BigDecimal) row[2];

                    BigDecimal percentage = BigDecimal.ZERO;

                    if (total.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = amount
                                .multiply(BigDecimal.valueOf(100))
                                .divide(
                                    total,
                                    2,
                                    RoundingMode.HALF_UP);
                    }

                    return new CategoryExpenseResponse(
                            categoryId,
                            categoryName,
                            amount,
                            percentage);
                })
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<MonthlyTrendResponse> getMonthlyTrend(
            Integer year) {

        User user = getCurrentUser();

        List<MonthlyTrendResponse> result =
                new ArrayList<>();

        for (int month = 1; month <= 12; month++) {

            YearMonth yearMonth =
                    YearMonth.of(year, month);

            LocalDate startDate =
                    yearMonth.atDay(1);

            LocalDate endDate =
                    yearMonth.atEndOfMonth();

            BigDecimal income =
                    incomeRepository.calculateTotalIncome(
                            user.getId(),
                            startDate,
                            endDate);

            BigDecimal expenses =
                    expenseRepository.calculateTotalExpenses(
                            user.getId(),
                            startDate,
                            endDate);

            result.add(
                new MonthlyTrendResponse(
                    month,
                    yearMonth.getMonth().toString(),
                    income,
                    expenses,
                    income.subtract(expenses)
                )
            );
        }

        return result;
    }
    
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetPerformance(
            Integer month,
            Integer year) {

        return budgetService.getBudgets(month, year);
    }
}