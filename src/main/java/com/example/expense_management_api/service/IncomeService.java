package com.example.expense_management_api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expense_management_api.dto.IncomeRequest;
import com.example.expense_management_api.dto.IncomeResponse;
import com.example.expense_management_api.entity.Income;
import com.example.expense_management_api.entity.User;
import com.example.expense_management_api.exception.IncomeNotFoundException;
import com.example.expense_management_api.repository.IncomeRepository;
import com.example.expense_management_api.repository.UserRepository;
import com.example.expense_management_api.security.SecurityUtils;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    public IncomeService(
            IncomeRepository incomeRepository,
            UserRepository userRepository) {

        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public IncomeResponse createIncome(
            IncomeRequest request) {

        User currentUser = getCurrentUser();

        Income income = Income.builder()
                .source(request.getSource().trim())
                .amount(request.getAmount())
                .description(request.getDescription())
                .incomeDate(request.getIncomeDate())
                .user(currentUser)
                .build();

        Income savedIncome =
                incomeRepository.save(income);

        return mapToResponse(savedIncome);
    }

    @Transactional(readOnly = true)
    public List<IncomeResponse> getAllIncomes() {

        User currentUser = getCurrentUser();

        return incomeRepository
                .findByUserIdOrderByIncomeDateDesc(
                        currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IncomeResponse getIncomeById(Long id) {

        User currentUser = getCurrentUser();

        Income income = incomeRepository
                .findByIdAndUserId(
                        id,
                        currentUser.getId())
                .orElseThrow(() ->
                new IncomeNotFoundException(id));

        return mapToResponse(income);
    }

    @Transactional
    public IncomeResponse updateIncome(
            Long id,
            IncomeRequest request) {

        User currentUser = getCurrentUser();

        Income income = incomeRepository
                .findByIdAndUserId(
                        id,
                        currentUser.getId())
                .orElseThrow(() ->
                new IncomeNotFoundException(id));

        income.setSource(request.getSource().trim());
        income.setAmount(request.getAmount());
        income.setDescription(request.getDescription());
        income.setIncomeDate(request.getIncomeDate());

        return mapToResponse(
                incomeRepository.save(income));
    }

    @Transactional
    public void deleteIncome(Long id) {

        User currentUser = getCurrentUser();

        Income income = incomeRepository
                .findByIdAndUserId(
                        id,
                        currentUser.getId())
                .orElseThrow(() ->
                new IncomeNotFoundException(id));
        
        incomeRepository.delete(income);
    }

    private User getCurrentUser() {

        String email =
                SecurityUtils.getCurrentUserEmail();

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                            "Authenticated user not found"));
    }

    private IncomeResponse mapToResponse(
            Income income) {

        return new IncomeResponse(
                income.getId(),
                income.getSource(),
                income.getAmount(),
                income.getDescription(),
                income.getIncomeDate(),
                income.getCreatedAt()
        );
    }
}