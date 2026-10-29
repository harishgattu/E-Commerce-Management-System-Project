package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.entity.*;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.repository.*;
import com.ecommerce.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InventoryAuditRepository inventoryAuditRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "taxRate", 0.08);
        ReflectionTestUtils.setField(orderService, "freeShippingThreshold", 50.0);
        ReflectionTestUtils.setField(orderService, "standardShippingFee", 5.0);

        testUser = new User("tester", "tester@eshop.com", "pass", "Tester User", "123456", Role.ROLE_CUSTOMER);
        testUser.setId(10L);

        testProduct = new Product("SKU-001", "Test Product", "Description", new BigDecimal("100.00"), 10, ProductStatus.ACTIVE, "img.jpg", null);
        testProduct.setId(100L);

        testCart = new Cart(testUser);
        testCart.setId(5L);

        testCartItem = new CartItem(testCart, testProduct, 2, new BigDecimal("100.00"));
        testCartItem.setId(50L);
        testCart.addItem(testCartItem);
    }

    @Test
    @DisplayName("Should successfully create order and decrement product stock")
    void testCreateOrderSuccess() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(inventoryAuditRepository.save(any(InventoryAudit.class))).thenReturn(new InventoryAudit());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(1L);
            return o;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                "123 Test Street",
                PaymentMethod.CREDIT_CARD,
                "Leave at door"
        );

        OrderResponse response = orderService.createOrderFromCart(10L, request);

        assertNotNull(response);
        assertEquals(8, testProduct.getStockQuantity(), "Product stock should be decremented from 10 to 8");
        assertEquals(OrderStatus.PENDING, response.getOrderStatus());
        verify(productRepository, times(1)).save(testProduct);
        verify(inventoryAuditRepository, times(1)).save(any(InventoryAudit.class));
        verify(cartRepository, times(1)).save(testCart);
        assertTrue(testCart.getItems().isEmpty(), "Cart should be cleared after checkout");
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when requested stock exceeds inventory")
    void testCreateOrderInsufficientStock() {
        testProduct.setStockQuantity(1); // Only 1 available, but cart requests 2

        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(10L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));

        CreateOrderRequest request = new CreateOrderRequest(
                "123 Test Street",
                PaymentMethod.CREDIT_CARD,
                "Urgent"
        );

        assertThrows(InsufficientStockException.class, () -> {
            orderService.createOrderFromCart(10L, request);
        });

        verify(orderRepository, never()).save(any(Order.class));
    }
}
