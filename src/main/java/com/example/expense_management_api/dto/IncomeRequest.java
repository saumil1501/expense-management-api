package com.example.expense_management_api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncomeRequest {

    @NotBlank(message = "Income source is required")
    @Size(max = 100, message = "Income source cannot exceed 100 characters")
    private String source;

    @NotNull(message = "Amount is required")
    @DecimalMin(
        value = "0.01",
        message = "Amount must be greater than 0"
    )
    private BigDecimal amount;

    @Size(
        max = 500,
        message = "Description cannot exceed 500 characters"
    )
    private String description;

    @NotNull(message = "Income date is required")
    private LocalDate incomeDate;
}