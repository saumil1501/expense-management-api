package com.example.expense_management_api.specification;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.example.expense_management_api.entity.Expense;

import jakarta.persistence.criteria.CriteriaBuilder;

public class ExpenseSpecification {

	public static Specification<Expense> ownedByUser(Long userId) {
		return (root, query, builder) -> builder.equal(root.get("user").get("id"), userId);
	}

	public static Specification<Expense> dateOnOrAfter(LocalDate date) {
		return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("expenseDate"), date);
	}

	public static Specification<Expense> dateOnOrBefore(LocalDate date) {
		return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("expenseDate"), date);
	}
	
	public static Specification<Expense> hasCategory(String category) {
		return (root, query, criteriaBuilder) -> 
				criteriaBuilder.equal(
						criteriaBuilder.lower(root.get("category").get("name")), 
						category.toLowerCase());
	}
	
	public static Specification<Expense> dateBetween(
	        LocalDate startDate,
	        LocalDate endDate) {

	    return (root, query, criteriaBuilder) ->
	            criteriaBuilder.between(
	                    root.get("expenseDate"),
	                    startDate,
	                    endDate
	            );
	}
	
	public static Specification<Expense> amountGreaterThanOrEqualTo(
	        BigDecimal minAmount) {

	    return (root, query, criteriaBuilder) ->
	            criteriaBuilder.greaterThanOrEqualTo(
	                    root.get("amount"),
	                    minAmount
	            );
	}
	
	public static Specification<Expense> amountLessThanOrEqualTo(
	        BigDecimal maxAmount) {

	    return (root, query, criteriaBuilder) ->
	            criteriaBuilder.lessThanOrEqualTo(
	                    root.get("amount"),
	                    maxAmount
	            );
	}
	
	public static Specification<Expense> containsText (String search) {
		return (root, query, criteriaBuilder) -> {
			
			String pattern = "%" + search.toLowerCase() + "%";
			
			return criteriaBuilder.or(
					criteriaBuilder.like(
							criteriaBuilder.lower(
									root.get("title")),
									pattern),
							criteriaBuilder.like(
									criteriaBuilder.lower(root.get("description")), pattern));
					
		};
	}

}
