package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.UpdateOrderStatusRequest;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order checkout, status updates, fulfillment, and cancellation")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Place order from current cart (validates inventory, locks prices, deducts stock)")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrderFromCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }

    @GetMapping
    @Operation(summary = "Get order history for current customer")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getCustomerOrders(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId) {
        List<OrderResponse> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all customer orders in the system (Admin)")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status) {
        List<OrderResponse> orders = (status != null)
                ? orderService.getOrdersByStatus(status)
                : orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "ROLE_CUSTOMER") String role) {
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(role);
        OrderResponse order = orderService.getOrderById(id, userId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order fulfillment status (Admin) - replenishes stock if cancelled")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @RequestHeader(value = "X-User-Name", defaultValue = "admin") String adminUser) {
        OrderResponse order = orderService.updateOrderStatus(id, request, adminUser);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order by customer (restores product inventory)")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId) {
        OrderResponse order = orderService.cancelOrderByCustomer(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled and inventory restored", order));
    }
}
