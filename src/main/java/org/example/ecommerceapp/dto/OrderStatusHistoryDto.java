package org.example.ecommerceapp.dto;

import org.example.ecommerceapp.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderStatusHistoryDto(
        OrderStatus previousStatus,
        OrderStatus newStatus,
        String changedBy,
        LocalDateTime changedAt
) {
}