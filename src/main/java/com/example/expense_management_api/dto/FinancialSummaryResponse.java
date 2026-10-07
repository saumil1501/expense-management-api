package com.example.expense_management_api.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FinancialSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private BigDecimal savingsRate;

    private long transactionCount;

    private Integer month;
    private Integer year;
}