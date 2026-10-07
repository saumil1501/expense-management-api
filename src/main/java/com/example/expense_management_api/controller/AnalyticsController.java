package com.example.expense_management_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.expense_management_api.dto.FinancialSummaryResponse;
import com.example.expense_management_api.service.AnalyticsService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/analytics")
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
}