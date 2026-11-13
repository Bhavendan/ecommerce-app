package org.example.ecommerceapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ecommerceapp.dto.OrderDto;
import org.example.ecommerceapp.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(
        name = "Admin Order Management",
        description = "APIs for managing order status"
)
public class AdminOrderController {

    private final OrderService service;

    public AdminOrderController(OrderService service) {
        this.service = service;
    }

    @PutMapping("/{orderId}/status/{status}")
    @Operation(
            summary = "Update order status",
            description = "Allows admin to update order status"
    )
    public ResponseEntity<OrderDto> updateOrderStatus(

            @PathVariable Long orderId,

            @PathVariable String status) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(
                service.updateOrderStatus(orderId, status, email)
        );
    }
    @GetMapping
    @Operation(summary = "Get all orders")
    public ResponseEntity<List<OrderDto>> getAllOrders() {

        return ResponseEntity.ok(service.getAllOrders());
    }
    @GetMapping("/{orderId}")
    @Operation(summary = "Get any order by ID")
    public ResponseEntity<OrderDto> getAdminOrderById(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                service.getAdminOrderById(orderId)
        );
    }
}