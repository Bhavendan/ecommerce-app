
package org.example.ecommerceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class InventoryDto {

    private Long id;

    private Long productId;

    private String productName;

    private int quantityOnHand;

    private int reservedQuantity;

    private int availableQuantity;

    private LocalDateTime updatedAt;
}
