package com.ecommerce.repository;

import com.ecommerce.entity.InventoryAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryAuditRepository extends JpaRepository<InventoryAudit, Long> {
    List<InventoryAudit> findByProductIdOrderByCreatedAtDesc(Long productId);
    List<InventoryAudit> findAllByOrderByCreatedAtDesc();
    List<InventoryAudit> findTop50ByOrderByCreatedAtDesc();
}
