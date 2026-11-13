
package org.example.ecommerceapp.service;

import org.example.ecommerceapp.dto.CartDto;

public interface CartService {

    CartDto getCart(String email);

    CartDto addToCart(
            String email,
            Long productId,
            int quantity);

    void removeFromCart(
            Long cartItemId,
            String email);

    void clearCart(String email);
}
