
package org.example.ecommerceapp.serviceIMPL;

import org.example.ecommerceapp.entity.Inventory;
import org.example.ecommerceapp.entity.InventoryMovement;
import org.example.ecommerceapp.entity.InventoryMovementType;
import org.example.ecommerceapp.repository.InventoryMovementRepository;
import org.example.ecommerceapp.service.InventoryMovementService;
import org.springframework.stereotype.Service;

@Service
public class InventoryMovementServiceImpl implements InventoryMovementService {

    private final InventoryMovementRepository movementRepo;

    public InventoryMovementServiceImpl(
            InventoryMovementRepository movementRepo
    ) {
        this.movementRepo = movementRepo;
    }

    @Override
    public void recordMovement(
            Inventory inventory,
            InventoryMovementType movementType,
            int quantity,
            int previousQuantityOnHand,
            int previousReservedQuantity,
            Long referenceId,
            String changedBy
    ) {
        InventoryMovement movement = new InventoryMovement();

        movement.setInventory(inventory);
        movement.setMovementType(movementType);
        movement.setQuantity(quantity);

        movement.setPreviousQuantityOnHand(previousQuantityOnHand);
        movement.setNewQuantityOnHand(inventory.getQuantityOnHand());

        movement.setPreviousReservedQuantity(previousReservedQuantity);
        movement.setNewReservedQuantity(inventory.getReservedQuantity());

        movement.setReferenceId(referenceId);
        movement.setChangedBy(changedBy);

        movementRepo.save(movement);
    }
}
