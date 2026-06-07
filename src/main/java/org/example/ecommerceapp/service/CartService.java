package org.example.ecommerceapp.service;

import org.example.ecommerceapp.dto.CartDto;

public interface CartService {

    CartDto getCart(String email);

    CartDto addToCart(
            Long userId,
            Long productId,
            int quantity);

    void removeFromCart(
            Long cartItemId);

    void clearCart(Long userId);
}