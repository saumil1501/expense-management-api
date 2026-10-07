package com.example.expense_management_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.expense_management_api.entity.Income;

public interface IncomeRepository
        extends JpaRepository<Income, Long> {

    List<Income> findByUserIdOrderByIncomeDateDesc(Long userId);

    Optional<Income> findByIdAndUserId(
            Long id,
            Long userId
    );
}