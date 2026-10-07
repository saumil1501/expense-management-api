package com.example.expense_management_api.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlyTrendResponse {

    private Integer month;
    private String monthName;
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal netBalance;
}