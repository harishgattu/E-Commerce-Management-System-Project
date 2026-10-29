package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Shopping Cart", description = "Customer shopping cart and line item management")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Get current user's shopping cart with price totals, taxes, and shipping")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId) {
        CartResponse cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the shopping cart (validating current inventory stock)")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId,
            @Valid @RequestBody AddToCartRequest request) {
        CartResponse cart = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update quantity of a line item in cart")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse cart = cartService.updateCartItemQuantity(userId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item quantity updated", cart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove an item from the cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId,
            @PathVariable Long itemId) {
        CartResponse cart = cartService.removeCartItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping
    @Operation(summary = "Clear all items from the cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader(value = "X-User-Id", defaultValue = "2") Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
