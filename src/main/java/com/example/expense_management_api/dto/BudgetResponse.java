package com.example.expense_management_api.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BudgetResponse {

    private Long id;

    private Long categoryId;
    private String categoryName;

    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;

    private BigDecimal usagePercentage;

    private String status;

    private Integer month;
    private Integer year;
}