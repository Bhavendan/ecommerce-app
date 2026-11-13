
package org.example.ecommerceapp.serviceIMPL;

import org.example.ecommerceapp.dto.CartDto;
import org.example.ecommerceapp.dto.CartItemDto;
import org.example.ecommerceapp.entity.*;
import org.example.ecommerceapp.exception.BadRequestException;
import org.example.ecommerceapp.exception.ResourceNotFoundException;
import org.example.ecommerceapp.repository.*;
import org.example.ecommerceapp.service.CartService;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;

    public CartServiceImpl(
            CartRepository cartRepo,
            CartItemRepository cartItemRepo,
            UserRepository userRepo,
            ProductRepository productRepo, InventoryRepository inventoryRepo) {

        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.inventoryRepo = inventoryRepo;
    }

    // GET CART

    @Override
    @Transactional(readOnly = true)
    public CartDto getCart(String email) {

        Cart cart = cartRepo.findByUserEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found"));

        return mapToDto(cart);
    }

    // ADD TO CART

    @Override
    @Transactional
    public CartDto addToCart(
            String email,
            Long productId,
            int quantity) {

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be positive");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        // FIND OR CREATE CART
        Cart cart = cartRepo.findByUserEmailWithLock(email)
                .orElseGet(() -> {

                    Cart newCart = new Cart();

                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>());
                    newCart.setTotalAmount(0);

                    return cartRepo.save(newCart);
                });

        // CHECK WHETHER PRODUCT ALREADY EXISTS

        CartItem existingItem = cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        // CALCULATE UPDATED QUANTITY

        int updatedQuantity = quantity;

        if (existingItem != null) {
            updatedQuantity += existingItem.getQuantity();
        }

        // CHECK TOTAL REQUIRED STOCK

        Inventory inventory = inventoryRepo.findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventory not found"));

        if (inventory.getAvailableQuantity() < updatedQuantity) {
            throw new BadRequestException("Insufficient stock");
        }

        // UPDATE EXISTING ITEM OR CREATE NEW ITEM

        if (existingItem != null) {

            existingItem.setQuantity(updatedQuantity);

            existingItem.setSubtotal(
                    updatedQuantity * product.getPrice()
            );

        } else {

            CartItem item = new CartItem();

            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);

            item.setSubtotal(quantity * product.getPrice());

            cart.getItems().add(item);
        }

        // UPDATE CART TOTAL

        double total = cart.getItems()
                .stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();

        cart.setTotalAmount(total);

        Cart saved = cartRepo.save(cart);

        return mapToDto(saved);
    }


    // REMOVE CART ITEM

    @Transactional
    public void removeFromCart(Long cartItemId, String email) {

        // 1. Lock the user's cart first
        Cart cart = cartRepo.findByUserEmailWithLock(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found"));

        // 2. Find the item inside the locked cart
        CartItem item = cart.getItems().stream()
                .filter(cartItem ->
                        cartItem.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart item not found"));

        // 3. Remove the item
        cart.getItems().remove(item);

        // 4. Recalculate cart total
        cart.setTotalAmount(
                cart.getItems().stream()
                        .mapToDouble(CartItem::getSubtotal)
                        .sum()
        );

        // 5. Save cart
        cartRepo.save(cart);
    }
    // CLEAR CART

    @Override
    @Transactional
    public void clearCart(String email) {

        Cart cart = cartRepo.findByUserEmailWithLock(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found"));

        cart.getItems().clear();

        cart.setTotalAmount(0);

        cartRepo.save(cart);
    }

    // DTO MAPPING

    private CartDto mapToDto(Cart cart) {

        List<CartItemDto> items = cart.getItems()
                .stream()
                .map(item -> new CartItemDto(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getProduct().getPrice(),
                        item.getSubtotal()))
                .toList();

        return new CartDto(
                cart.getId(),
                cart.getUser().getId(),
                cart.getUser().getName(),
                items,
                cart.getTotalAmount());
    }
}
