package org.example.ecommerceapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    private  double subtotal;

    public CartItem() {
    }

    public CartItem(Long id,
                    Cart cart,
                    Product product,
                    int quantity) {

        this.id = id;
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    // getters setters
}