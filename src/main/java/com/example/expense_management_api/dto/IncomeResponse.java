package com.example.expense_management_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IncomeResponse {

    private Long id;
    private String source;
    private BigDecimal amount;
    private String description;
    private LocalDate incomeDate;
    private LocalDateTime createdAt;
}