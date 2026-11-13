package org.example.ecommerceapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDto {

    private Long id;

    @NotBlank(message = "Product name required")
    private String name;

    private String description;

    @Positive(message = "Price must be positive")
    private double price;

    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stock;

    // category info only
    private Long categoryId;

    private String categoryName;

    public ProductDto() {
    }

    public ProductDto(Long id,
                      String name,
                      String description,
                      double price,
                      int stock,
                      Long categoryId,
                      String categoryName) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    // getters setters
}