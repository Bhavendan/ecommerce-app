
package org.example.ecommerceapp.service;

import org.example.ecommerceapp.entity.Inventory;
import org.example.ecommerceapp.entity.InventoryMovementType;

public interface InventoryMovementService {

    void recordMovement(
            Inventory inventory,
            InventoryMovementType movementType,
            int quantity,
            int previousQuantityOnHand,
            int previousReservedQuantity,
            Long referenceId,
            String changedBy
    );
}
