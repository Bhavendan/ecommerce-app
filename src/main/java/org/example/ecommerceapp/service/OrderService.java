
package org.example.ecommerceapp.service;

import org.example.ecommerceapp.dto.OrderDto;
import org.example.ecommerceapp.dto.ShippingAddressDto;

import java.util.List;

public interface OrderService {

    OrderDto placeOrder(String email, ShippingAddressDto address);

    List<OrderDto> getUserOrders(String email);

    // IDOR FIX: Verify order ownership
    OrderDto getOrderById(Long orderId, String email);

    // IDOR FIX: Verify order ownership before cancellation
    void cancelOrder(Long orderId, String email);

    OrderDto updateOrderStatus(Long orderId, String status, String changedBy);
    List<OrderDto> getAllOrders();

    OrderDto getAdminOrderById(Long orderId);
}
