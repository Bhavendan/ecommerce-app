
package org.example.ecommerceapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_product",
                        columnNames = "product_id"
                )
        }
)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantityOnHand;

    @Column(nullable = false)
    private int reservedQuantity;

    @Version
    private Long version;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Available stock = Physical stock - Reserved stock
    @Transient
    public int getAvailableQuantity() {
        return quantityOnHand - reservedQuantity;
    }

    // Reserve stock when an order is placed
    public void reserve(int quantity) {

        validateQuantity(quantity);

        if (getAvailableQuantity() < quantity) {
            throw new IllegalStateException(
                    "Insufficient available inventory"
            );
        }

        reservedQuantity += quantity;
        updateTimestamp();
    }

    // Release reserved stock when an order is cancelled
    public void releaseReservation(int quantity) {

        validateQuantity(quantity);

        if (reservedQuantity < quantity) {
            throw new IllegalStateException(
                    "Insufficient reserved inventory"
            );
        }

        reservedQuantity -= quantity;
        updateTimestamp();
    }

    // Consume reserved stock when an order is shipped
    public void shipReserved(int quantity) {

        validateQuantity(quantity);

        if (reservedQuantity < quantity ||
                quantityOnHand < quantity) {

            throw new IllegalStateException(
                    "Invalid inventory reservation"
            );
        }

        quantityOnHand -= quantity;
        reservedQuantity -= quantity;
        updateTimestamp();
    }

    // Admin adjusts the desired available stock
    public void adjustAvailableQuantity(int newAvailableQuantity) {

        if (newAvailableQuantity < 0) {
            throw new IllegalArgumentException(
                    "Available quantity cannot be negative"
            );
        }

        quantityOnHand = newAvailableQuantity + reservedQuantity;
        updateTimestamp();
    }

    // Common quantity validation
    private void validateQuantity(int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }

    // Update the last-modified timestamp
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
