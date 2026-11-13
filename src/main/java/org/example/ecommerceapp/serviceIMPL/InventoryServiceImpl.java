
package org.example.ecommerceapp.serviceIMPL;

import org.example.ecommerceapp.dto.InventoryDto;
import org.example.ecommerceapp.dto.InventoryAdjustmentDto;
import org.example.ecommerceapp.dto.InventoryMovementDto;
import org.example.ecommerceapp.entity.Inventory;
import org.example.ecommerceapp.entity.InventoryMovement;
import org.example.ecommerceapp.entity.InventoryMovementType;
import org.example.ecommerceapp.exception.ResourceNotFoundException;
import org.example.ecommerceapp.repository.InventoryMovementRepository;
import org.example.ecommerceapp.repository.InventoryRepository;
import org.example.ecommerceapp.service.InventoryMovementService;
import org.example.ecommerceapp.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepo;
    private final InventoryMovementService movementService;
    private InventoryMovementRepository movementRepo;

    public InventoryServiceImpl(InventoryRepository inventoryRepo, InventoryMovementService movementService, InventoryMovementRepository movementRepo) {
        this.inventoryRepo = inventoryRepo;
        this.movementService = movementService;
        this.movementRepo = movementRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto getInventoryByProductId(Long productId) {

        Inventory inventory = inventoryRepo.findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventory not found"));

        return mapToDto(inventory);
    }

    @Override
    @Transactional
    public InventoryDto adjustAvailableQuantity(
            Long productId,
            InventoryAdjustmentDto dto
    ) {
        Inventory inventory = inventoryRepo.findByProductIdWithLock(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventory not found"));

        // 1. Capture previous inventory state
        int previousOnHand = inventory.getQuantityOnHand();
        int previousReserved = inventory.getReservedQuantity();
        int previousAvailable = inventory.getAvailableQuantity();

        // 2. Adjust inventory
        inventory.adjustAvailableQuantity(dto.getAvailableQuantity());

        // 3. Calculate the quantity changed
        int quantityChanged = Math.abs(
                dto.getAvailableQuantity() - previousAvailable
        );

        // 4. Record movement only if stock actually changed
        if (quantityChanged > 0) {

            String changedBy = SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();

            movementService.recordMovement(
                    inventory,
                    InventoryMovementType.STOCK_ADJUSTMENT,
                    quantityChanged,
                    previousOnHand,
                    previousReserved,
                    null,
                    changedBy
            );
        }

        return mapToDto(inventory);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<InventoryMovementDto> getMovementHistory(
            Long productId,
            Pageable pageable
    ) {
        // Verify that inventory exists for this product
        inventoryRepo.findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventory not found"));

        Page<InventoryMovement> movements =
                movementRepo.findByInventoryProductIdOrderByCreatedAtDesc(
                        productId,
                        pageable
                );

        return movements.map(movement -> new InventoryMovementDto(
                movement.getId(),
                movement.getInventory().getProduct().getId(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getPreviousQuantityOnHand(),
                movement.getNewQuantityOnHand(),
                movement.getPreviousReservedQuantity(),
                movement.getNewReservedQuantity(),
                movement.getReferenceId(),
                movement.getChangedBy(),
                movement.getCreatedAt()
        ));
    }

    private InventoryDto mapToDto(Inventory inventory) {

        return new InventoryDto(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getProduct().getName(),
                inventory.getQuantityOnHand(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity(),
                inventory.getUpdatedAt()
        );
    }
}
