package com.example.expense_management_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.expense_management_api.dto.CategoryRequest;
import com.example.expense_management_api.dto.CategoryResponse;
import com.example.expense_management_api.entity.Category;
import com.example.expense_management_api.exception.CategoryNotFoundException;
import com.example.expense_management_api.exception.DuplicateResourceException;
import com.example.expense_management_api.repository.CategoryRepository;
import com.example.expense_management_api.repository.ExpenseRepository;

@Service
public class CategoryService {
	
	private final CategoryRepository categoryRepository;
	private final ExpenseRepository expenseRepository;
	
	public CategoryService(CategoryRepository categoryRepository, ExpenseRepository expenseRepository) {
		this.categoryRepository = categoryRepository;
		this.expenseRepository = expenseRepository;
	}
	
	public CategoryResponse createCategory(CategoryRequest request) {
		String name = request.getName().trim();
		
		if(categoryRepository.existsByNameIgnoreCase(name)) {
			throw new DuplicateResourceException("Category already exists: "+name);
		}
	
	
	Category category = new Category();
	
	category.setName(name);
	
	Category savedCategory =
            categoryRepository.save(category);

    return mapToResponse(savedCategory);

}

	private CategoryResponse mapToResponse(Category category) {
		
		return new CategoryResponse(category.getId(), category.getName());
	}
	
	public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
	
	public CategoryResponse getCategoryById(Long id) {
		Category category = categoryRepository.findById(id).orElseThrow(()-> new CategoryNotFoundException(id));
		
		return mapToResponse(category);
	}
	
	public CategoryResponse updateCategory(Long id, CategoryRequest request) {
		
		Category category = categoryRepository.findById(id).orElseThrow(()-> new CategoryNotFoundException(id));
		
		String name = request.getName().trim();
		
		categoryRepository.findByNameIgnoreCase(name)
						  .filter(existingCategory -> !existingCategory.getId().equals(id))
						  .ifPresent(existingCategory -> {
							  throw new DuplicateResourceException(
							  "Category already exists: " + name);
						  });
		
		category.setName(name);
		
		Category updatedCategory = categoryRepository.save(category);
		
		return mapToResponse(updatedCategory);
	}
	
	public void deleteCategory(Long id) {
		
		Category category = categoryRepository.findById(id).orElseThrow(()-> new CategoryNotFoundException(id));
		
		if(expenseRepository.existsByCategoryId(id)) {
			throw new DuplicateResourceException("Cannot delete category because it is being used by expenses");
		}
		
		categoryRepository.delete(category);
	}
}
