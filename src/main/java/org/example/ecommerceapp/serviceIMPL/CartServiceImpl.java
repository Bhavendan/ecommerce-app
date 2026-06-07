package org.example.ecommerceapp.serviceIMPL;

import org.example.ecommerceapp.dto.CartDto;
import org.example.ecommerceapp.dto.CartItemDto;
import org.example.ecommerceapp.entity.*;
import org.example.ecommerceapp.exception.BadRequestException;
import org.example.ecommerceapp.exception.ResourceNotFoundException;

import org.example.ecommerceapp.repository.*;

import org.example.ecommerceapp.service.CartService;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepo;

    private final CartItemRepository cartItemRepo;

    private final UserRepository userRepo;

    private final ProductRepository productRepo;

    public CartServiceImpl(

            CartRepository cartRepo,

            CartItemRepository cartItemRepo,

            UserRepository userRepo,

            ProductRepository productRepo) {

        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
    }

    // =====================================
    // GET CART
    // =====================================

    @Override
    public CartDto getCart(String email) {

        Cart cart = cartRepo.findByUserEmail(email).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return mapToDto(cart);
    }

    // =====================================
    // ADD TO CART
    // =====================================

    @Override
    public CartDto addToCart(

            Long userId,

            Long productId,

            int quantity) {

        if (quantity <= 0) {

            throw new BadRequestException("Quantity must be positive");
        }

        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Product product = productRepo.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStock() < quantity) {

            throw new BadRequestException("Insufficient stock");
        }

        // FIND OR CREATE CART

        Cart cart = cartRepo.findByUserId(userId).orElseGet(() -> {

            Cart newCart = new Cart();

            newCart.setUser(user);

            newCart.setItems(new ArrayList<>());

            newCart.setTotalAmount(0);

            return cartRepo.save(newCart);
        });

        // CREATE CART ITEM

        CartItem item = new CartItem();

        item.setCart(cart);

        item.setProduct(product);

        item.setQuantity(quantity);

        double subtotal = quantity * product.getPrice();

        item.setSubtotal(subtotal);

        cart.getItems().add(item);

        // UPDATE TOTAL

        double total = cart.getItems().stream().mapToDouble(CartItem::getSubtotal).sum();

        cart.setTotalAmount(total);

        Cart saved = cartRepo.save(cart);

        return mapToDto(saved);
    }

    // =====================================
    // REMOVE CART ITEM
    // =====================================

    @Override
    public void removeFromCart(Long cartItemId) {

        CartItem item = cartItemRepo.findById(cartItemId).orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        Cart cart = item.getCart();

        cart.getItems().remove(item);

        cartItemRepo.delete(item);

        double total = cart.getItems().stream().mapToDouble(CartItem::getSubtotal).sum();

        cart.setTotalAmount(total);

        cartRepo.save(cart);
    }

    // =====================================
    // CLEAR CART
    // =====================================

    @Override
    public void clearCart(Long userId) {

        Cart cart = cartRepo.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.getItems().clear();

        cart.setTotalAmount(0);

        cartRepo.save(cart);
    }

    // =====================================
    // DTO MAPPING
    // =====================================

    private CartDto mapToDto(Cart cart) {

        List<CartItemDto> items = cart.getItems().stream().map(item -> new CartItemDto(

                item.getProduct().getId(),

                item.getProduct().getName(),

                item.getQuantity(),

                item.getProduct().getPrice(),

                item.getSubtotal())).toList();

        return new CartDto(

                cart.getId(),

                cart.getUser().getId(),

                cart.getUser().getName(),

                items,

                cart.getTotalAmount());
    }
}