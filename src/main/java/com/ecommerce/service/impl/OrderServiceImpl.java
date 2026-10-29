package com.ecommerce.service.impl;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderItemResponse;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.UpdateOrderStatusRequest;
import com.ecommerce.entity.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.repository.*;
import com.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryAuditRepository inventoryAuditRepository;

    @Value("${ecommerce.tax-rate:0.08}")
    private double taxRate;

    @Value("${ecommerce.free-shipping-threshold:50.00}")
    private double freeShippingThreshold;

    @Value("${ecommerce.standard-shipping-fee:5.00}")
    private double standardShippingFee;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            CartRepository cartRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository,
                            InventoryAuditRepository inventoryAuditRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.inventoryAuditRepository = inventoryAuditRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrderFromCart(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("No cart found for user."));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place order with an empty cart.");
        }

        // 1. Verify stock availability and build order items
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + cartItem.getProduct().getId()));

            int requestedQty = cartItem.getQuantity();
            int currentStock = product.getStockQuantity();

            if (currentStock < requestedQty) {
                throw new InsufficientStockException(product.getName(), requestedQty, currentStock);
            }

            // Deduct stock atomically
            int previousStock = currentStock;
            int newStock = currentStock - requestedQty;
            product.setStockQuantity(newStock);
            productRepository.save(product);

            // Record inventory movement audit
            InventoryAudit audit = new InventoryAudit(
                    product,
                    MovementType.ORDER_DEDUCTION,
                    -requestedQty,
                    previousStock,
                    newStock,
                    "Order deduction for customer: " + user.getUsername(),
                    user.getUsername()
            );
            inventoryAuditRepository.save(audit);

            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(requestedQty));
            subtotal = subtotal.add(itemSubtotal);

            OrderItem orderItem = new OrderItem(
                    null, // set below
                    product,
                    product.getName(),
                    product.getSku(),
                    product.getPrice(),
                    requestedQty,
                    itemSubtotal
            );
            orderItems.add(orderItem);
        }

        // 2. Calculate tax, shipping fee, total
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(taxRate)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shippingFee = subtotal.compareTo(BigDecimal.valueOf(freeShippingThreshold)) >= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(standardShippingFee).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax).add(shippingFee).setScale(2, RoundingMode.HALF_UP);

        // 3. Create Order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PAID); // Simulated instant payment
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.CREDIT_CARD);
        order.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        order.setTax(tax);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(total);
        order.setShippingAddress(request.getShippingAddress());
        order.setStatusNotes(request.getNotes() != null ? request.getNotes() : "Order placed successfully.");

        for (OrderItem oi : orderItems) {
            order.addItem(oi);
        }

        Order savedOrder = orderRepository.save(order);

        // 4. Clear the cart
        cart.clear();
        cartRepository.save(cart);

        return mapToOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long orderId, Long userId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!isAdmin && !order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this order.");
        }

        return mapToOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatusOrderByCreatedAtDesc(status).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request, String performedBy) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        OrderStatus oldStatus = order.getOrderStatus();
        OrderStatus newStatus = request.getOrderStatus();

        // If transitioning to CANCELLED from non-cancelled status, replenish stock!
        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            restoreStockForOrder(order, performedBy, "Order cancelled by " + performedBy);
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        order.setOrderStatus(newStatus);
        if (request.getStatusNotes() != null && !request.getStatusNotes().trim().isEmpty()) {
            order.setStatusNotes(request.getStatusNotes());
        }

        Order saved = orderRepository.save(order);
        return mapToOrderResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrderByCustomer(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only cancel your own orders.");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order cannot be cancelled because it is already " + order.getOrderStatus());
        }

        restoreStockForOrder(order, order.getUser().getUsername(), "Customer requested cancellation");
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        order.setStatusNotes("Cancelled by customer.");

        Order saved = orderRepository.save(order);
        return mapToOrderResponse(saved);
    }

    private void restoreStockForOrder(Order order, String performedBy, String reason) {
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                Product product = productRepository.findById(item.getProduct().getId()).orElse(null);
                if (product != null) {
                    int prevStock = product.getStockQuantity();
                    int restoredStock = prevStock + item.getQuantity();
                    product.setStockQuantity(restoredStock);
                    productRepository.save(product);

                    InventoryAudit audit = new InventoryAudit(
                            product,
                            MovementType.ORDER_CANCEL_REFUND,
                            item.getQuantity(),
                            prevStock,
                            restoredStock,
                            reason + " (Order #" + order.getOrderNumber() + ")",
                            performedBy
                    );
                    inventoryAuditRepository.save(audit);
                }
            }
        }
    }

    private String generateOrderNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + datePrefix + "-" + randomSuffix;
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setUserId(order.getUser().getId());
        response.setUserFullName(order.getUser().getFullName());
        response.setUserEmail(order.getUser().getEmail());
        response.setOrderStatus(order.getOrderStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setSubtotal(order.getSubtotal());
        response.setTax(order.getTax());
        response.setShippingFee(order.getShippingFee());
        response.setTotalAmount(order.getTotalAmount());
        response.setShippingAddress(order.getShippingAddress());
        response.setStatusNotes(order.getStatusNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        var items = new ArrayList<OrderItemResponse>();
        if (order.getItems() != null) {
            for (OrderItem oi : order.getItems()) {
                OrderItemResponse oir = new OrderItemResponse(
                        oi.getId(),
                        oi.getProduct() != null ? oi.getProduct().getId() : null,
                        oi.getProductSku(),
                        oi.getProductName(),
                        oi.getUnitPrice(),
                        oi.getQuantity(),
                        oi.getSubtotal()
                );
                items.add(oir);
            }
        }
        response.setItems(items);
        return response;
    }
}
