
package org.example.ecommerceapp.service;

import org.example.ecommerceapp.dto.InventoryDto;
import org.example.ecommerceapp.dto.InventoryAdjustmentDto;
import org.example.ecommerceapp.dto.InventoryMovementDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    InventoryDto getInventoryByProductId(Long productId);

    InventoryDto adjustAvailableQuantity(
            Long productId,
            InventoryAdjustmentDto dto
    );
    Page<InventoryMovementDto> getMovementHistory(
            Long productId,
            Pageable pageable
    );
}
