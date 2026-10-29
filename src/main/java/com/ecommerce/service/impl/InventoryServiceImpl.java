package com.ecommerce.service.impl;

import com.ecommerce.dto.InventoryAuditResponse;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.dto.StockAdjustmentRequest;
import com.ecommerce.entity.InventoryAudit;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.InventoryAuditRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final InventoryAuditRepository inventoryAuditRepository;

    public InventoryServiceImpl(ProductRepository productRepository,
                                InventoryAuditRepository inventoryAuditRepository) {
        this.productRepository = productRepository;
        this.inventoryAuditRepository = inventoryAuditRepository;
    }

    @Override
    @Transactional
    public ProductResponse adjustStock(Long productId, StockAdjustmentRequest request, String performedBy) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        int previousStock = product.getStockQuantity();
        int newStock = previousStock + request.getQuantityChange();

        if (newStock < 0) {
            throw new BadRequestException("Adjustment of " + request.getQuantityChange() +
                    " would cause stock for '" + product.getName() + "' to fall below zero (Current: " + previousStock + ").");
        }

        product.setStockQuantity(newStock);
        Product savedProduct = productRepository.save(product);

        InventoryAudit audit = new InventoryAudit(
                savedProduct,
                request.getMovementType(),
                request.getQuantityChange(),
                previousStock,
                newStock,
                request.getReason(),
                performedBy != null ? performedBy : "ADMIN"
        );
        inventoryAuditRepository.save(audit);

        return new ProductResponse(
                savedProduct.getId(),
                savedProduct.getSku(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                savedProduct.getStockQuantity(),
                savedProduct.getStatus(),
                savedProduct.getImageUrl(),
                savedProduct.getCategory() != null ? savedProduct.getCategory().getId() : null,
                savedProduct.getCategory() != null ? savedProduct.getCategory().getName() : null,
                savedProduct.getVersion(),
                savedProduct.getCreatedAt(),
                savedProduct.getUpdatedAt()
        );
    }

    @Override
    public List<InventoryAuditResponse> getProductAudits(Long productId) {
        return inventoryAuditRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::mapToAuditResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryAuditResponse> getRecentAudits() {
        return inventoryAuditRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::mapToAuditResponse)
                .collect(Collectors.toList());
    }

    private InventoryAuditResponse mapToAuditResponse(InventoryAudit audit) {
        return new InventoryAuditResponse(
                audit.getId(),
                audit.getProduct().getId(),
                audit.getProduct().getName(),
                audit.getProduct().getSku(),
                audit.getMovementType(),
                audit.getQuantityChanged(),
                audit.getPreviousQuantity(),
                audit.getNewQuantity(),
                audit.getReason(),
                audit.getPerformedBy(),
                audit.getCreatedAt()
        );
    }
}
