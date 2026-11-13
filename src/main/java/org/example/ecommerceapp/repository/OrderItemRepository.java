package org.example.ecommerceapp.repository;

import org.example.ecommerceapp.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
    boolean existsByProductId(Long productId);

}
