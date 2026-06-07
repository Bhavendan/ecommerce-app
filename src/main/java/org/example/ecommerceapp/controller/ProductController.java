package org.example.ecommerceapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ecommerceapp.dto.ProductDto;
import org.example.ecommerceapp.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(
        name = "Product Management",
        description = "APIs for viewing and searching products"
)
public class ProductController {

    private final ProductService service;

    public ProductController(
            ProductService service) {

        this.service = service;
    }

    // GET PRODUCT BY ID

    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID",
            description = "Returns product details for the specified product ID"
    )
    public ResponseEntity<ProductDto> getProductById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getProductById(id)
        );
    }

    // GET ALL PRODUCTS

    @GetMapping
    @Operation(
            summary = "Get all products",
            description = "Returns paginated products with sorting support"
    )
    public ResponseEntity<Page<ProductDto>> getAllProducts(

            @RequestParam(
                    defaultValue = "0")
            int page,

            @RequestParam(
                    defaultValue = "5")
            int size,

            @RequestParam(
                    defaultValue = "id")
            String sortBy,

            @RequestParam(
                    defaultValue = "asc")
            String direction) {

        return ResponseEntity.ok(
                service.getAllProducts(
                        page,
                        size,
                        sortBy,
                        direction
                )
        );
    }

    // SEARCH PRODUCTS

    @GetMapping("/search")
    @Operation(
            summary = "Search products",
            description = "Searches products by keyword with pagination support"
    )
    public ResponseEntity<Page<ProductDto>> searchProducts(

            @RequestParam String keyword,

            @RequestParam(
                    defaultValue = "0")
            int page,

            @RequestParam(
                    defaultValue = "5")
            int size) {

        return ResponseEntity.ok(
                service.searchProducts(
                        keyword,
                        page,
                        size
                )
        );
    }
}