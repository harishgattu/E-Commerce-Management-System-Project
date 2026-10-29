package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartResponse {

    private Long id;
    private Long userId;
    private List<CartItemResponse> items = new ArrayList<>();
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal estimatedTax = BigDecimal.ZERO;
    private BigDecimal estimatedShipping = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private int totalItemCount = 0;

    public CartResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getEstimatedTax() {
        return estimatedTax;
    }

    public void setEstimatedTax(BigDecimal estimatedTax) {
        this.estimatedTax = estimatedTax;
    }

    public BigDecimal getEstimatedShipping() {
        return estimatedShipping;
    }

    public void setEstimatedShipping(BigDecimal estimatedShipping) {
        this.estimatedShipping = estimatedShipping;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTotalItemCount() {
        return totalItemCount;
    }

    public void setTotalItemCount(int totalItemCount) {
        this.totalItemCount = totalItemCount;
    }
}
