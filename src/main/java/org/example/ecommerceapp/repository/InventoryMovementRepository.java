
package org.example.ecommerceapp.repository;

import org.example.ecommerceapp.entity.InventoryMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovement, Long> {

    boolean existsByInventoryId(Long inventoryId);

    Page<InventoryMovement> findByInventoryProductIdOrderByCreatedAtDesc(
            Long productId,
            Pageable pageable
    );
}
