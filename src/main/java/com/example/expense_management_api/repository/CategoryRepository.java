package com.example.expense_management_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.expense_management_api.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>{
	
	Optional<Category> findByNameIgnoreCase(String name);
	
	boolean existsByNameIgnoreCase(String name);

}
