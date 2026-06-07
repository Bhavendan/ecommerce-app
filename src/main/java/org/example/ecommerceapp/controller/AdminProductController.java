package org.example.ecommerceapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.ecommerceapp.dto.ProductDto;
import org.example.ecommerceapp.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@Tag(
        name = "Admin Product Management",
        description = "APIs for administrators to manage products"
)
public class AdminProductController {

    private final ProductService service;

    public AdminProductController(
            ProductService service) {

        this.service = service;
    }

    // CREATE PRODUCT

    @PostMapping
    @Operation(
            summary = "Create a product",
            description = "Creates a new product in the catalog (ADMIN only)"
    )
    public ResponseEntity<ProductDto> createProduct(
            @Valid
            @RequestBody ProductDto dto) {

        ProductDto product =
                service.createProduct(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(product);
    }

    // UPDATE PRODUCT

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a product",
            description = "Updates an existing product (ADMIN only)"
    )
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto dto) {

        ProductDto product =
                service.updateProduct(id, dto);

        return ResponseEntity.ok(product);
    }

    // DELETE PRODUCT

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a product",
            description = "Deletes a product from the catalog (ADMIN only)"
    )
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id) {

        service.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }
}