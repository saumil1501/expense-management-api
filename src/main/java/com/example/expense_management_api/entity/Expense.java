package com.example.expense_management_api.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "expenses")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String title;
	
	private BigDecimal amount;
	
	private String description;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "category_id",
			nullable = false)
	private Category category;
	
	private LocalDate expenseDate;
	
	private LocalDateTime createdAt;
	
	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

}

