package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.UpdateOrderStatusRequest;
import com.ecommerce.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse createOrderFromCart(Long userId, CreateOrderRequest request);
    OrderResponse getOrderById(Long orderId, Long userId, boolean isAdmin);
    List<OrderResponse> getOrdersByUser(Long userId);
    List<OrderResponse> getAllOrders();
    List<OrderResponse> getOrdersByStatus(OrderStatus status);
    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, String performedBy);
    OrderResponse cancelOrderByCustomer(Long orderId, Long userId);
}
