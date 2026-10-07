package com.example.expense_management_api.exception;

public class BudgetNotFoundException
        extends RuntimeException {

    public BudgetNotFoundException(Long id) {
        super("Budget not found with id: " + id);
    }
}