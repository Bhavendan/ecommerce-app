package org.example.ecommerceapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.ecommerceapp.dto.CategoryDto;
import org.example.ecommerceapp.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@Tag(
        name = "Admin Category Management",
        description = "APIs for administrators to manage product categories"
)
public class AdminCategoryController {

    private final CategoryService service;

    public AdminCategoryController(CategoryService service) {
        this.service = service;
    }

    // CREATE CATEGORY

    @PostMapping
    @Operation(
            summary = "Create category",
            description = "Creates a new product category (ADMIN only)"
    )
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CategoryDto dto) {

        CategoryDto category =
                service.createCategory(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(category);
    }

    // UPDATE CATEGORY

    @PutMapping("/{id}")
    @Operation(
            summary = "Update category",
            description = "Updates an existing category (ADMIN only)"
    )
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDto dto) {

        CategoryDto category =
                service.updateCategory(id, dto);

        return ResponseEntity.ok(category);
    }

    // DELETE CATEGORY

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete category",
            description = "Deletes a category by ID (ADMIN only)"
    )
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id) {

        service.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }
}