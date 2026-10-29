package com.ecommerce.service.impl;

import com.ecommerce.dto.DashboardMetricsResponse;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.DashboardService;
import com.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    @Value("${ecommerce.low-stock-threshold:10}")
    private int lowStockThreshold;

    public DashboardServiceImpl(OrderRepository orderRepository,
                                OrderItemRepository orderItemRepository,
                                ProductRepository productRepository,
                                UserRepository userRepository,
                                OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
    }

    @Override
    public DashboardMetricsResponse getDashboardMetrics() {
        DashboardMetricsResponse metrics = new DashboardMetricsResponse();

        // 1. Total Revenue (excluding cancelled orders)
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(OrderStatus.CANCELLED);
        metrics.setTotalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        // 2. High-level counts
        metrics.setTotalOrders(orderRepository.count());
        metrics.setTotalCustomers(userRepository.count());
        metrics.setTotalProducts(productRepository.count());
        metrics.setLowStockCount(productRepository.countByStockQuantityLessThanEqual(lowStockThreshold));

        // 3. Orders by Status
        Map<String, Long> statusMap = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            statusMap.put(status.name(), orderRepository.countByOrderStatus(status));
        }
        metrics.setOrdersByStatus(statusMap);

        // 4. Top Selling Products
        List<Object[]> topRaw = orderItemRepository.findTopSellingProducts();
        var topSelling = topRaw.stream().map(row -> new DashboardMetricsResponse.TopProductDto(
                (String) row[0],
                (String) row[1],
                ((Number) row[2]).longValue(),
                (BigDecimal) row[3]
        )).collect(Collectors.toList());
        metrics.setTopSellingProducts(topSelling);

        // 5. Recent Orders
        metrics.setRecentOrders(orderService.getAllOrders().stream().limit(5).collect(Collectors.toList()));

        return metrics;
    }
}
