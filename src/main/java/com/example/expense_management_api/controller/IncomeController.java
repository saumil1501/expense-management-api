package com.example.expense_management_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.expense_management_api.dto.IncomeRequest;
import com.example.expense_management_api.dto.IncomeResponse;
import com.example.expense_management_api.service.IncomeService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/incomes")
@Tag(
    name = "Income",
    description = "Manage user income records"
)
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(
            IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @PostMapping
    public ResponseEntity<IncomeResponse> createIncome(
            @Valid @RequestBody IncomeRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(incomeService.createIncome(request));
    }

    @GetMapping
    public ResponseEntity<List<IncomeResponse>>
            getAllIncomes() {

        return ResponseEntity.ok(
                incomeService.getAllIncomes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse>
            getIncomeById(@PathVariable Long id) {

        return ResponseEntity.ok(
                incomeService.getIncomeById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse>
            updateIncome(
                    @PathVariable Long id,
                    @Valid @RequestBody
                    IncomeRequest request) {

        return ResponseEntity.ok(
                incomeService.updateIncome(
                        id,
                        request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
            deleteIncome(@PathVariable Long id) {

        incomeService.deleteIncome(id);

        return ResponseEntity.noContent().build();
    }
}