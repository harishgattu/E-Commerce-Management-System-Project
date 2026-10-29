package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.InventoryAuditResponse;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.dto.StockAdjustmentRequest;
import com.ecommerce.service.InventoryService;
import com.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory Management", description = "Stock adjustments, low-stock threshold queries, and audit logs")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    public InventoryController(InventoryService inventoryService, ProductService productService) {
        this.inventoryService = inventoryService;
        this.productService = productService;
    }

    @PostMapping("/adjust/{productId}")
    @Operation(summary = "Adjust product inventory stock with movement audit log (Admin)")
    public ResponseEntity<ApiResponse<ProductResponse>> adjustStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustmentRequest request,
            @RequestHeader(value = "X-User-Name", defaultValue = "admin") String adminUser) {
        ProductResponse response = inventoryService.adjustStock(productId, request, adminUser);
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully", response));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get list of products with inventory below threshold")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStock(
            @RequestParam(required = false, defaultValue = "10") Integer threshold) {
        List<ProductResponse> lowStockProducts = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(ApiResponse.success(lowStockProducts));
    }

    @GetMapping("/audits")
    @Operation(summary = "Get top 50 recent inventory audit movements across the system (Admin)")
    public ResponseEntity<ApiResponse<List<InventoryAuditResponse>>> getRecentAudits() {
        List<InventoryAuditResponse> audits = inventoryService.getRecentAudits();
        return ResponseEntity.ok(ApiResponse.success(audits));
    }

    @GetMapping("/audits/product/{productId}")
    @Operation(summary = "Get inventory movement history for a specific product")
    public ResponseEntity<ApiResponse<List<InventoryAuditResponse>>> getProductAudits(@PathVariable Long productId) {
        List<InventoryAuditResponse> audits = inventoryService.getProductAudits(productId);
        return ResponseEntity.ok(ApiResponse.success(audits));
    }
}
