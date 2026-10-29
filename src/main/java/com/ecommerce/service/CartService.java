package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;

public interface CartService {
    CartResponse getCartByUserId(Long userId);
    CartResponse addToCart(Long userId, AddToCartRequest request);
    CartResponse updateCartItemQuantity(Long userId, Long cartItemId, UpdateCartItemRequest request);
    CartResponse removeCartItem(Long userId, Long cartItemId);
    void clearCart(Long userId);
}
