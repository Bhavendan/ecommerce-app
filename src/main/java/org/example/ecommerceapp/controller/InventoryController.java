
package org.example.ecommerceapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.ecommerceapp.dto.InventoryAdjustmentDto;
import org.example.ecommerceapp.dto.InventoryDto;
import org.example.ecommerceapp.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.ecommerceapp.dto.InventoryMovementDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/admin/inventory")
@Tag(
        name = "Inventory Management",
        description = "Admin APIs for viewing and adjusting inventory"
)
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // GET INVENTORY BY PRODUCT ID

    @GetMapping("/{productId}")
    @Operation(
            summary = "Get product inventory",
            description = "Returns stock, reserved quantity and available quantity"
    )
    public ResponseEntity<InventoryDto> getInventory(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                inventoryService.getInventoryByProductId(productId)
        );
    }

    // ADJUST AVAILABLE QUANTITY

    @PutMapping("/{productId}/available-quantity")
    @Operation(
            summary = "Adjust available inventory",
            description = "Updates the desired available quantity for a product"
    )
    public ResponseEntity<InventoryDto> adjustAvailableQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryAdjustmentDto dto) {

        return ResponseEntity.ok(
                inventoryService.adjustAvailableQuantity(productId, dto)
        );
    }
    @GetMapping("/{productId}/movements")
    @Operation(
            summary = "Get inventory movement history",
            description = "Returns paginated inventory movement records for a product"
    )
    public ResponseEntity<Page<InventoryMovementDto>> getMovementHistory(
            @PathVariable Long productId,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                inventoryService.getMovementHistory(productId, pageable)
        );
    }
}
