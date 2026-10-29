package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardMetricsResponse {

    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private long totalOrders = 0;
    private long totalCustomers = 0;
    private long totalProducts = 0;
    private long lowStockCount = 0;
    private Map<String, Long> ordersByStatus = new HashMap<>();
    private List<TopProductDto> topSellingProducts = new ArrayList<>();
    private List<OrderResponse> recentOrders = new ArrayList<>();

    public DashboardMetricsResponse() {
    }

    public static class TopProductDto {
        private String productName;
        private String productSku;
        private long unitsSold;
        private BigDecimal totalRevenue;

        public TopProductDto() {
        }

        public TopProductDto(String productName, String productSku, long unitsSold, BigDecimal totalRevenue) {
            this.productName = productName;
            this.productSku = productSku;
            this.unitsSold = unitsSold;
            this.totalRevenue = totalRevenue;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductSku() {
            return productSku;
        }

        public void setProductSku(String productSku) {
            this.productSku = productSku;
        }

        public long getUnitsSold() {
            return unitsSold;
        }

        public void setUnitsSold(long unitsSold) {
            this.unitsSold = unitsSold;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
        }
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public Map<String, Long> getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(Map<String, Long> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }

    public List<TopProductDto> getTopSellingProducts() {
        return topSellingProducts;
    }

    public void setTopSellingProducts(List<TopProductDto> topSellingProducts) {
        this.topSellingProducts = topSellingProducts;
    }

    public List<OrderResponse> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<OrderResponse> recentOrders) {
        this.recentOrders = recentOrders;
    }
}
