package com.example.expense_management_api.exception;

public class IncomeNotFoundException extends RuntimeException {

    public IncomeNotFoundException(Long id) {
        super("Income not found with id: " + id);
    }
}