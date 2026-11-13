
package org.example.ecommerceapp.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.ecommerceapp.entity.OrderStatus;

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

    private OrderStatus status;

    private LocalDateTime orderDate;

    private String shippingRecipient;

    private String shippingPhone;

    private String shippingAddressLine1;

    private String shippingAddressLine2;

    private String shippingCity;

    private String shippingState;

    private String shippingPostalCode;

    private String shippingCountry;
    private List<OrderStatusHistoryDto> statusHistory;

    public OrderDto() {
    }

    public OrderDto(
            Long id,
            Long userId,
            String userName,
            List<OrderItemDto> items,
            double totalAmount,
            OrderStatus status,
            LocalDateTime orderDate,
            String shippingRecipient,
            String shippingPhone,
            String shippingAddressLine1,
            String shippingAddressLine2,
            String shippingCity,
            String shippingState,
            String shippingPostalCode,
            String shippingCountry,
            List<OrderStatusHistoryDto> statusHistory
    ) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = orderDate;
        this.shippingRecipient = shippingRecipient;
        this.shippingPhone = shippingPhone;
        this.shippingAddressLine1 = shippingAddressLine1;
        this.shippingAddressLine2 = shippingAddressLine2;
        this.shippingCity = shippingCity;
        this.shippingState = shippingState;
        this.shippingPostalCode = shippingPostalCode;
        this.shippingCountry = shippingCountry;
        this.statusHistory=statusHistory;
    }
}
