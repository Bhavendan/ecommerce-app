
package org.example.ecommerceapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "inventory_movements",
        indexes = {
                @Index(
                        name = "idx_movement_inventory",
                        columnList = "inventory_id"
                ),
                @Index(
                        name = "idx_movement_created_at",
                        columnList = "created_at"
                )
        }
)
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryMovementType movementType;

    // Absolute quantity affected by this movement
    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int previousQuantityOnHand;

    @Column(nullable = false)
    private int newQuantityOnHand;

    @Column(nullable = false)
    private int previousReservedQuantity;

    @Column(nullable = false)
    private int newReservedQuantity;

    // Related order ID; null for standalone stock adjustments
    private Long referenceId;

    // Admin email or SYSTEM
    @Column(nullable = false)
    private String changedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
