package org.example.ecommerceapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ecommerceapp.dto.CartDto;
import org.example.ecommerceapp.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(
        name = "Cart Management",
        description = "APIs for managing the authenticated user's shopping cart"
)
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    // GET USER CART

    @GetMapping
    @Operation(
            summary = "Get current user's cart",
            description = "Returns all cart items and total amount for the authenticated user"
    )
    public ResponseEntity<CartDto> getCart() {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = auth.getName();

        return ResponseEntity.ok(
                service.getCart(email)
        );
    }

    // ADD TO CART

    @PostMapping("/add")
    @Operation(
            summary = "Add product to cart",
            description = "Adds a product to the user's cart with the specified quantity"
    )
    public ResponseEntity<CartDto> addToCart(

            @RequestParam Long userId,

            @RequestParam Long productId,

            @RequestParam int quantity) {

        return ResponseEntity.ok(
                service.addToCart(
                        userId,
                        productId,
                        quantity
                )
        );
    }

    // REMOVE ITEM

    @DeleteMapping("/remove/{cartItemId}")
    @Operation(
            summary = "Remove item from cart",
            description = "Removes a specific cart item from the user's cart"
    )
    public ResponseEntity<Void> removeFromCart(
            @PathVariable Long cartItemId) {

        service.removeFromCart(cartItemId);

        return ResponseEntity
                .noContent()
                .build();
    }

    // CLEAR CART

    @DeleteMapping("/clear/{userId}")
    @Operation(
            summary = "Clear cart",
            description = "Removes all items from the specified user's cart"
    )
    public ResponseEntity<Void> clearCart(
            @PathVariable Long userId) {

        service.clearCart(userId);

        return ResponseEntity
                .noContent()
                .build();
    }
}