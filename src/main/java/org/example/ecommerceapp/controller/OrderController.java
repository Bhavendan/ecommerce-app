package org.example.ecommerceapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.ecommerceapp.dto.OrderDto;
import org.example.ecommerceapp.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(
        name = "Order Management",
        description = "APIs for placing and managing customer orders"
)
public class OrderController {

    private final OrderService service;

    public OrderController(
            OrderService service) {

        this.service = service;
    }

    // PLACE ORDER

    @PostMapping("/place")
    @Operation(
            summary = "Place an order",
            description = "Creates a new order for the authenticated user"
    )
    public ResponseEntity<OrderDto> placeOrder() {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        OrderDto order =
                service.placeOrder(email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(order);
    }

    // GET USER ORDERS

    @GetMapping
    @Operation(
            summary = "Get user orders",
            description = "Returns all orders belonging to the authenticated user"
    )
    public ResponseEntity<List<OrderDto>> getUserOrders() {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        return ResponseEntity.ok(
                service.getUserOrders(email)
        );
    }

    // GET ORDER BY ID

    @GetMapping("/{orderId}")
    @Operation(
            summary = "Get order by ID",
            description = "Returns details of a specific order"
    )
    public ResponseEntity<OrderDto> getOrderById(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                service.getOrderById(orderId)
        );
    }

    // CANCEL ORDER

    @DeleteMapping("/{orderId}")
    @Operation(
            summary = "Cancel order",
            description = "Cancels an existing order"
    )
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long orderId) {

        service.cancelOrder(orderId);

        return ResponseEntity
                .noContent()
                .build();
    }
}