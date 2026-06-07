package org.example.ecommerceapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDto {

    private Long productId;

    private String productName;

    private int quantity;

    private double price;

    private double subtotal;

    public CartItemDto() {
    }

    public CartItemDto(Long productId,
                       String productName,
                       int quantity,
                       double price,
                       double subtotal) {

        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    // getters setters
}