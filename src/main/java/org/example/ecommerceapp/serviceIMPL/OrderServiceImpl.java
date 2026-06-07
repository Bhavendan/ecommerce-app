package org.example.ecommerceapp.serviceIMPL;

import jakarta.transaction.Transactional;
import org.example.ecommerceapp.dto.OrderDto;
import org.example.ecommerceapp.dto.OrderItemDto;

import org.example.ecommerceapp.entity.*;

import org.example.ecommerceapp.exception.BadRequestException;
import org.example.ecommerceapp.exception.ResourceNotFoundException;

import org.example.ecommerceapp.repository.*;

import org.example.ecommerceapp.service.OrderService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;

    private final CartRepository cartRepo;

    private final ProductRepository productRepo;

    public OrderServiceImpl(

            OrderRepository orderRepo,

            CartRepository cartRepo,

            ProductRepository productRepo) {

        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    // =====================================
    // PLACE ORDER
    // =====================================

    @Override
    @Transactional
    public OrderDto placeOrder(String email) {

        Cart cart = cartRepo.findByUserEmail(email).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {

            throw new BadRequestException("Cart is empty");
        }

        Order order = new Order();

        order.setUser(cart.getUser());

        order.setOrderDate(LocalDateTime.now());

        order.setStatus("PLACED");

        order.setItems(new ArrayList<>());

        double total = 0;

        // CONVERT CART ITEMS -> ORDER ITEMS

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            // CHECK STOCK

            if (product.getStock() < cartItem.getQuantity()) {

                throw new BadRequestException("Insufficient stock for " + product.getName());
            }

            // REDUCE STOCK

            product.setStock(product.getStock() - cartItem.getQuantity());

            productRepo.save(product);

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);

            orderItem.setProduct(product);

            orderItem.setQuantity(cartItem.getQuantity());

            orderItem.setPrice(product.getPrice());

            orderItem.setSubtotal(cartItem.getSubtotal());

            order.getItems().add(orderItem);

            total += cartItem.getSubtotal();
        }

        order.setTotalAmount(total);

        Order saved = orderRepo.save(order);

        // CLEAR CART

        cart.getItems().clear();

        cart.setTotalAmount(0);

        cartRepo.save(cart);

        return mapToDto(saved);
    }

    // =====================================
    // GET USER ORDERS
    // =====================================

    @Override
    public List<OrderDto> getUserOrders(String email) {

        return orderRepo.findByUserEmail(email).stream().map(this::mapToDto).toList();
    }

    // =====================================
    // GET ORDER BY ID
    // =====================================

    @Override
    public OrderDto getOrderById(Long orderId) {

        Order order = orderRepo.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return mapToDto(order);
    }

    // =====================================
    // CANCEL ORDER
    // =====================================

    @Override
    public void cancelOrder(Long orderId) {

        Order order = orderRepo.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus("CANCELLED");

        orderRepo.save(order);
    }

    // =====================================
    // DTO MAPPING
    // =====================================

    private OrderDto mapToDto(Order order) {

        List<OrderItemDto> items = order.getItems().stream().map(item -> new OrderItemDto(

                item.getProduct().getId(),

                item.getProduct().getName(),

                item.getQuantity(),

                item.getPrice(),

                item.getSubtotal())).toList();

        return new OrderDto(

                order.getId(),

                order.getUser().getId(),

                order.getUser().getName(),

                items,

                order.getTotalAmount(),

                order.getStatus(),

                order.getOrderDate());
    }
}