package org.example.ecommerceapp.repository;

import jakarta.persistence.LockModeType;
import org.example.ecommerceapp.entity.Order;
import org.example.ecommerceapp.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserEmail(String email);

    Optional<Order> findByIdAndUserEmail(Long orderId, String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT o FROM Order o
       WHERE o.id = :orderId
       AND o.user.email = :email
       """)
    Optional<Order> findByIdAndUserEmailWithLock(
            @Param("orderId") Long orderId,
            @Param("email") String email
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT o FROM Order o
    WHERE o.id = :orderId
    """)
    Optional<Order> findByIdWithLock(
            @Param("orderId") Long orderId
    );
}