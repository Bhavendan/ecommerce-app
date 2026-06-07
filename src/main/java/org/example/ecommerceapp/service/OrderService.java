package org.example.ecommerceapp.service;

import org.example.ecommerceapp.dto.OrderDto;

import java.util.List;

public interface OrderService {

    OrderDto placeOrder(
            String email);

    List<OrderDto> getUserOrders(String email);

    OrderDto getOrderById(
            Long orderId);

    void cancelOrder(
            Long orderId);
}