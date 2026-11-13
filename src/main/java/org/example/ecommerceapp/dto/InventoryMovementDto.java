
package org.example.ecommerceapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.ecommerceapp.entity.InventoryMovementType;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class InventoryMovementDto {

    private Long id;

    private Long productId;

    private InventoryMovementType movementType;

    private int quantity;

    private int previousQuantityOnHand;

    private int newQuantityOnHand;

    private int previousReservedQuantity;

    private int newReservedQuantity;

    private Long referenceId;

    private String changedBy;

    private LocalDateTime createdAt;
}
