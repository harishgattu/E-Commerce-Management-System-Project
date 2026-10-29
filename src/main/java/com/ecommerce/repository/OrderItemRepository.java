package com.ecommerce.repository;

import com.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi.productName, oi.productSku, SUM(oi.quantity), SUM(oi.subtotal) " +
           "FROM OrderItem oi GROUP BY oi.productName, oi.productSku ORDER BY SUM(oi.quantity) DESC LIMIT 5")
    List<Object[]> findTopSellingProducts();
}
