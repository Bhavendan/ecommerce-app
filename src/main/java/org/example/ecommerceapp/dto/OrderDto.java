package org.example.ecommerceapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class OrderDto {

    private Long id;

    private Long userId;

    private String userName;

    private List<OrderItemDto> items;

    private double totalAmount;

    private String status;

    private LocalDateTime orderDate;

    public OrderDto() {
    }

    public OrderDto(Long id,
                    Long userId,
                    String userName,
                    List<OrderItemDto> items,
                    double totalAmount,
                    String status,
                    LocalDateTime orderDate) {

        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
    }

    // getters setters
}