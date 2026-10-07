package com.example.expense_management_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.expense_management_api.dto.BudgetResponse;
import com.example.expense_management_api.dto.CategoryExpenseResponse;
import com.example.expense_management_api.dto.FinancialSummaryResponse;
import com.example.expense_management_api.dto.MonthlyTrendResponse;
import com.example.expense_management_api.service.AnalyticsService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/analytics")
@Validated
@Tag(
    name = "Analytics",
    description = "Financial analytics and reporting"
)
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(
            AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<FinancialSummaryResponse>
            getMonthlySummary(
                @RequestParam Integer month,
                @RequestParam Integer year) {

        return ResponseEntity.ok(
                analyticsService.getMonthlySummary(
                    month,
                    year));
    }
    
    @GetMapping("/expenses-by-category")
    public ResponseEntity<List<CategoryExpenseResponse>>
            getExpensesByCategory(
                @RequestParam Integer month,
                @RequestParam Integer year) {

        return ResponseEntity.ok(
                analyticsService.getExpensesByCategory(
                        month,
                        year));
    }
    
    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendResponse>>
            getMonthlyTrend(
                @RequestParam Integer year) {

        return ResponseEntity.ok(
                analyticsService.getMonthlyTrend(year));
    }
    
    
    @GetMapping("/budget-performance")
    public ResponseEntity<List<BudgetResponse>>
            getBudgetPerformance(
                @RequestParam Integer month,
                @RequestParam Integer year) {

        return ResponseEntity.ok(
                analyticsService.getBudgetPerformance(
                        month,
                        year));
    }
}