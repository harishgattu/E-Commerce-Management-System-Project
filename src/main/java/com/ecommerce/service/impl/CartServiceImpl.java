package com.ecommerce.service.impl;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Value("${ecommerce.tax-rate:0.08}")
    private double taxRate;

    @Value("${ecommerce.free-shipping-threshold:50.00}")
    private double freeShippingThreshold;

    @Value("${ecommerce.standard-shipping-fee:5.00}")
    private double standardShippingFee;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CartResponse getCartByUserId(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        if (product.getStockQuantity() <= 0) {
            throw new InsufficientStockException(product.getName(), request.getQuantity(), 0);
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (newQuantity > product.getStockQuantity()) {
                throw new InsufficientStockException(product.getName(), newQuantity, product.getStockQuantity());
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setUnitPrice(product.getPrice());
        } else {
            if (request.getQuantity() > product.getStockQuantity()) {
                throw new InsufficientStockException(product.getName(), request.getQuantity(), product.getStockQuantity());
            }
            CartItem newItem = new CartItem(cart, product, request.getQuantity(), product.getPrice());
            cart.addItem(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItemQuantity(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        Product product = cartItem.getProduct();
        if (request.getQuantity() > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getName(), request.getQuantity(), product.getStockQuantity());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItem.setUnitPrice(product.getPrice());

        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        cart.removeItem(cartItem);
        Cart savedCart = cartRepository.save(cart);
        return mapToCartResponse(savedCart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
                    Cart newCart = new Cart(user);
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUser().getId());

        BigDecimal subtotal = BigDecimal.ZERO;
        int totalCount = 0;
        var itemResponses = new ArrayList<CartItemResponse>();

        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                subtotal = subtotal.add(item.getSubtotal());
                totalCount += item.getQuantity();

                CartItemResponse itemRes = new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getSku(),
                        item.getProduct().getName(),
                        item.getProduct().getImageUrl(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal(),
                        item.getProduct().getStockQuantity()
                );
                itemResponses.add(itemRes);
            }
        }

        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(taxRate)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shipping = BigDecimal.ZERO;
        if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
            shipping = subtotal.compareTo(BigDecimal.valueOf(freeShippingThreshold)) >= 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(standardShippingFee).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal total = subtotal.add(tax).add(shipping);

        response.setItems(itemResponses);
        response.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        response.setEstimatedTax(tax);
        response.setEstimatedShipping(shipping);
        response.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        response.setTotalItemCount(totalCount);

        return response;
    }
}
