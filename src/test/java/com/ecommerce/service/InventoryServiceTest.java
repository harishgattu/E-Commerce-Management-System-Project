package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.dto.StockAdjustmentRequest;
import com.ecommerce.entity.InventoryAudit;
import com.ecommerce.entity.MovementType;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.repository.InventoryAuditRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryAuditRepository inventoryAuditRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product("TEST-SKU", "Mechanical Keyboard", "RGB Keycaps", new BigDecimal("79.99"), 15, ProductStatus.ACTIVE, "url", null);
        sampleProduct.setId(1L);
    }

    @Test
    @DisplayName("Should successfully restock product and record inventory movement audit")
    void testRestockSuccess() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        when(inventoryAuditRepository.save(any(InventoryAudit.class))).thenReturn(new InventoryAudit());

        StockAdjustmentRequest request = new StockAdjustmentRequest(10, MovementType.RESTOCK, "Supplier Batch #88");

        ProductResponse response = inventoryService.adjustStock(1L, request, "admin");

        assertNotNull(response);
        assertEquals(25, sampleProduct.getStockQuantity());
        verify(productRepository, times(1)).save(sampleProduct);
        verify(inventoryAuditRepository, times(1)).save(any(InventoryAudit.class));
    }

    @Test
    @DisplayName("Should reject inventory adjustment if result would cause negative inventory")
    void testNegativeStockRejection() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        StockAdjustmentRequest request = new StockAdjustmentRequest(-20, MovementType.MANUAL_ADJUSTMENT, "Shrinkage");

        assertThrows(BadRequestException.class, () -> {
            inventoryService.adjustStock(1L, request, "admin");
        });

        assertEquals(15, sampleProduct.getStockQuantity(), "Stock quantity should remain unchanged");
        verify(productRepository, never()).save(any(Product.class));
        verify(inventoryAuditRepository, never()).save(any(InventoryAudit.class));
    }
}
