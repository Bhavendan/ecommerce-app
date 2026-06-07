package org.example.ecommerceapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class CartDto {

    private Long id;

    private Long userId;

    private String userName;

    private List<CartItemDto> items;

    private double totalAmount;

    public CartDto() {
    }

    public CartDto(Long id,
                   Long userId,
                   String userName,
                   List<CartItemDto> items,
                   double totalAmount) {

        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    // getters setters
}