package com.example.expense_management_api.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.annotation.PutExchange;

import com.example.expense_management_api.dto.CategoryRequest;
import com.example.expense_management_api.dto.CategoryResponse;
import com.example.expense_management_api.service.CategoryService;

import jakarta.validation.Valid;

@Tag(
	    name = "Categories",
	    description = "Manage expense categories"
	)
	@RestController
	@RequestMapping("/api/categories")
public class CategoryController {
	
	private final CategoryService categoryService;
	
	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	@GetMapping
    public List<CategoryResponse> getAllCategories() {

        return categoryService.getAllCategories();
    }
	
	@PostMapping
	public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
		return categoryService.createCategory(request);
	}
	
	@GetMapping("/{id}")
	public CategoryResponse getCategoryById(@PathVariable Long id) {
		return categoryService.getCategoryById(id);
	}
	
	@PutMapping("/{id}")
	public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
		return categoryService.updateCategory(id, request);
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCategory(@PathVariable Long id) {
		categoryService.deleteCategory(id);
	}
}
