package org.example.ecommerceapp.serviceIMPL;

import org.example.ecommerceapp.dto.CategoryDto;
import org.example.ecommerceapp.entity.Category;
import org.example.ecommerceapp.exception.BadRequestException;
import org.example.ecommerceapp.exception.ResourceNotFoundException;
import org.example.ecommerceapp.repository.CategoryRepository;
import org.example.ecommerceapp.repository.ProductRepository;
import org.example.ecommerceapp.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class    CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;
    private final ProductRepository productRepo;

    public CategoryServiceImpl(CategoryRepository repo, ProductRepository productRepo) {

        this.repo = repo;
        this.productRepo = productRepo;
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
    @Transactional
    public void deleteCategory(Long id) {

        Category category = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (productRepo.existsByCategoryId(id)) {
            throw new BadRequestException("Cannot delete category with associated products");
        }

        repo.delete(category);
    }

    private CategoryDto mapToDto(Category category) {

        return new CategoryDto(category.getId(), category.getName());
    }
}