package org.example.ecommerceapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CategoryDto {

    private Long id;

    private String name;

    public CategoryDto() {
    }

    public CategoryDto(Long id,
                       String name) {

        this.id = id;
        this.name = name;
    }

    // getters setters
}