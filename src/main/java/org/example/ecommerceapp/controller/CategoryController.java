package org.example.ecommerceapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ecommerceapp.dto.CategoryDto;
import org.example.ecommerceapp.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(
        name = "Category Management",
        description = "APIs for viewing product categories"
)
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    // GET ALL CATEGORIES

    @GetMapping
    @Operation(
            summary = "Get all categories",
            description = "Returns a list of all available product categories"
    )
    public ResponseEntity<List<CategoryDto>> getAllCategories() {

        return ResponseEntity.ok(
                service.getAllCategories()
        );
    }

    // GET CATEGORY BY ID

    @GetMapping("/{id}")
    @Operation(
            summary = "Get category by ID",
            description = "Returns category details for the specified category ID"
    )
    public ResponseEntity<CategoryDto> getCategoryById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getCategoryById(id)
        );
    }
}