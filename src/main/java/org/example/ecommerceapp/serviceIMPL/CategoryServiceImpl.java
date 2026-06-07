package org.example.ecommerceapp.serviceIMPL;

import org.example.ecommerceapp.dto.CategoryDto;
import org.example.ecommerceapp.entity.Category;
import org.example.ecommerceapp.exception.ResourceNotFoundException;
import org.example.ecommerceapp.repository.CategoryRepository;
import org.example.ecommerceapp.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;

    public CategoryServiceImpl(CategoryRepository repo) {

        this.repo = repo;
    }

    @Override
    public CategoryDto createCategory(CategoryDto dto) {

        Category category = new Category();

        category.setName(dto.getName());

        Category saved = repo.save(category);

        return mapToDto(saved);
    }

    @Override
    public List<CategoryDto> getAllCategories() {

        return repo.findAll().stream().map(this::mapToDto).toList();
    }

    @Override
    public CategoryDto getCategoryById(Long id) {

        Category category = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        return mapToDto(category);
    }

    @Override
    public CategoryDto updateCategory(Long id, CategoryDto dto) {

        Category category = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(dto.getName());

        Category updated = repo.save(category) ;

        return mapToDto(updated);
    }

    @Override
    public void deleteCategory(Long id) {

        repo.deleteById(id);
    }

    private CategoryDto mapToDto(Category category) {

        return new CategoryDto(category.getId(), category.getName());
    }
}