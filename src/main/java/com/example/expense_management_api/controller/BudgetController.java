package com.example.expense_management_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.expense_management_api.dto.BudgetRequest;
import com.example.expense_management_api.dto.BudgetResponse;
import com.example.expense_management_api.service.BudgetService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/budgets")
@Tag(
    name = "Budgets",
    description = "Manage monthly category budgets"
)
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(
            BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody BudgetRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    budgetService.createBudget(request));
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>>
            getBudgets(
                @RequestParam Integer month,
                @RequestParam Integer year) {

        return ResponseEntity.ok(
                budgetService.getBudgets(month, year));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse>
            getBudgetById(@PathVariable Long id) {

        return ResponseEntity.ok(
                budgetService.getBudgetById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse>
            updateBudget(
                @PathVariable Long id,
                @Valid @RequestBody
                BudgetRequest request) {

        return ResponseEntity.ok(
                budgetService.updateBudget(
                    id,
                    request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
            deleteBudget(@PathVariable Long id) {

        budgetService.deleteBudget(id);

        return ResponseEntity.noContent().build();
    }
}