package com.ecommerce.service;

import com.ecommerce.dto.InventoryAuditResponse;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.dto.StockAdjustmentRequest;

import java.util.List;

public interface InventoryService {
    ProductResponse adjustStock(Long productId, StockAdjustmentRequest request, String performedBy);
    List<InventoryAuditResponse> getProductAudits(Long productId);
    List<InventoryAuditResponse> getRecentAudits();
}
