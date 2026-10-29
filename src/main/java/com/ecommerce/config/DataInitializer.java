package com.ecommerce.config;

import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final InventoryAuditRepository inventoryAuditRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           AddressRepository addressRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           CartRepository cartRepository,
                           OrderRepository orderRepository,
                           InventoryAuditRepository inventoryAuditRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.inventoryAuditRepository = inventoryAuditRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Checking database seed status...");

        // 1. Seed Users
        User admin = userRepository.findByUsername("admin").orElseGet(() -> {
            User newAdmin = new User(
                    "admin",
                    "admin@eshop.com",
                    passwordEncoder.encode("admin123"),
                    "Store Administrator",
                    "+1-555-0100",
                    Role.ROLE_ADMIN
            );
            return userRepository.save(newAdmin);
        });

        User customer = userRepository.findByUsername("john_doe").orElseGet(() -> {
            User newCustomer = new User(
                    "john_doe",
                    "john@eshop.com",
                    passwordEncoder.encode("customer123"),
                    "Johnathan Doe",
                    "+1-555-0199",
                    Role.ROLE_CUSTOMER
            );
            User saved = userRepository.save(newCustomer);

            // Add default shipping address
            Address address = new Address(
                    saved,
                    "742 Evergreen Terrace",
                    "Springfield",
                    "OR",
                    "97477",
                    "USA",
                    "SHIPPING",
                    true
            );
            addressRepository.save(address);

            // Create initial cart
            Cart cart = new Cart(saved);
            cartRepository.save(cart);

            return saved;
        });

        // Ensure customer has a cart
        cartRepository.findByUserId(customer.getId()).orElseGet(() -> {
            Cart cart = new Cart(customer);
            return cartRepository.save(cart);
        });

        // 2. Seed Categories
        if (categoryRepository.count() == 0) {
            log.info("Seeding categories...");
            categoryRepository.save(new Category(
                    "Electronics",
                    "electronics",
                    "Cutting-edge gadgets, laptops, audio equipment, and computer peripherals.",
                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600"
            ));
            categoryRepository.save(new Category(
                    "Footwear & Apparel",
                    "apparel",
                    "Premium activewear, designer shoes, and comfortable everyday clothing.",
                    "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600"
            ));
            categoryRepository.save(new Category(
                    "Home & Kitchen",
                    "home-kitchen",
                    "Modern kitchen appliances, cookware, and cozy home accessories.",
                    "https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?w=600"
            ));
            categoryRepository.save(new Category(
                    "Books & Media",
                    "books",
                    "Bestselling technical books, software engineering manuals, and literature.",
                    "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600"
            ));
        }

        // 3. Seed Products
        if (productRepository.count() == 0) {
            log.info("Seeding products and initial inventory audits...");
            Category electronics = categoryRepository.findBySlug("electronics").orElse(null);
            Category apparel = categoryRepository.findBySlug("apparel").orElse(null);
            Category homeKitchen = categoryRepository.findBySlug("home-kitchen").orElse(null);
            Category books = categoryRepository.findBySlug("books").orElse(null);

            List<Product> products = List.of(
                    new Product("ELEC-SNY-001", "Sony WH-1000XM5 Wireless Headphones",
                            "Industry-leading noise cancelation with two processors and 8 microphones. Up to 30 hours battery life with quick charging.",
                            new BigDecimal("399.99"), 25, ProductStatus.ACTIVE,
                            "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600", electronics),

                    new Product("ELEC-APL-002", "Apple MacBook Air M3 15-inch",
                            "Strikingly thin design with the lightning-fast Apple M3 chip, 16GB Unified Memory, and 512GB SSD storage.",
                            new BigDecimal("1299.00"), 8, ProductStatus.ACTIVE,
                            "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600", electronics),

                    new Product("ELEC-LOG-003", "Logitech MX Master 3S Wireless Mouse",
                            "Ergonomic performance mouse with quiet clicks, 8K DPI track-on-glass sensor, and ultrafast MagSpeed scrolling.",
                            new BigDecimal("99.99"), 45, ProductStatus.ACTIVE,
                            "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=600", electronics),

                    new Product("ELEC-DEL-004", "Dell UltraSharp 27 4K Monitor",
                            "Brilliant 4K UHD resolution with IPS Black technology, 98% DCI-P3 color gamut, and integrated USB-C hub.",
                            new BigDecimal("549.99"), 4, ProductStatus.ACTIVE,
                            "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600", electronics),

                    new Product("APPL-NIK-005", "Nike Air Zoom Pegasus 40 Running Shoes",
                            "Engineered mesh upper with dual Zoom Air units and responsive React foam for everyday running performance.",
                            new BigDecimal("130.00"), 32, ProductStatus.ACTIVE,
                            "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600", apparel),

                    new Product("APPL-PAT-006", "Patagonia Torrentshell 3L Rain Jacket",
                            "Simple and unpretentious 3-layer H2No Performance Standard shell for exceptional waterproof/breathable performance.",
                            new BigDecimal("179.00"), 16, ProductStatus.ACTIVE,
                            "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=600", apparel),

                    new Product("APPL-LEV-007", "Levi's 511 Slim Fit Stretch Jeans",
                            "A modern slim with room to move. Added stretch for all-day comfort and mobility in classic dark wash.",
                            new BigDecimal("69.50"), 50, ProductStatus.ACTIVE,
                            "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600", apparel),

                    new Product("HOME-DLG-008", "De'Longhi Magnifica S Espresso Machine",
                            "Compact bean-to-cup machine with manual milk frother, 15 bar pressure, and personalized coffee length.",
                            new BigDecimal("649.95"), 5, ProductStatus.ACTIVE,
                            "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=600", homeKitchen),

                    new Product("HOME-NNJ-009", "Ninja Air Fryer Pro 4-in-1",
                            "Crisp, roast, reheat, and dehydrate with 5-quart nonstick ceramic basket and rapid air circulation.",
                            new BigDecimal("119.99"), 28, ProductStatus.ACTIVE,
                            "https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?w=600", homeKitchen),

                    new Product("HOME-HDF-010", "Hydro Flask 32 oz Wide Mouth Bottle",
                            "TempShield double-wall vacuum insulation keeps drinks cold up to 24 hours or hot up to 12 hours.",
                            new BigDecimal("44.95"), 65, ProductStatus.ACTIVE,
                            "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=600", homeKitchen),

                    new Product("BOOK-DDA-011", "Designing Data-Intensive Applications",
                            "The definitive guide to the architecture, data models, storage engines, and consensus algorithms of distributed systems.",
                            new BigDecimal("42.50"), 38, ProductStatus.ACTIVE,
                            "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600", books),

                    new Product("BOOK-CLA-012", "Clean Architecture: A Craftsman's Guide",
                            "Robert C. Martin presents the universal rules of software architecture for high-quality, maintainable systems.",
                            new BigDecimal("34.99"), 24, ProductStatus.ACTIVE,
                            "https://images.unsplash.com/photo-1532012164546-f432f2e3777a?w=600", books)
            );

            for (Product p : products) {
                Product saved = productRepository.save(p);
                InventoryAudit audit = new InventoryAudit(
                        saved,
                        MovementType.INITIAL_SEED,
                        saved.getStockQuantity(),
                        0,
                        saved.getStockQuantity(),
                        "Initial product inventory setup",
                        "SYSTEM"
                );
                inventoryAuditRepository.save(audit);
            }

            // Seed a sample completed order
            Product headphone = productRepository.findBySku("ELEC-SNY-001").orElse(null);
            Product book = productRepository.findBySku("BOOK-DDA-011").orElse(null);

            if (headphone != null && book != null) {
                Order sampleOrder = new Order();
                sampleOrder.setOrderNumber("ORD-20261028-DEMO1");
                sampleOrder.setUser(customer);
                sampleOrder.setOrderStatus(OrderStatus.CONFIRMED);
                sampleOrder.setPaymentStatus(PaymentStatus.PAID);
                sampleOrder.setPaymentMethod(PaymentMethod.CREDIT_CARD);
                sampleOrder.setShippingAddress("742 Evergreen Terrace, Springfield, OR 97477, USA");
                sampleOrder.setStatusNotes("Demo order for previewing fulfillment flow");

                BigDecimal subtotal = headphone.getPrice().add(book.getPrice());
                BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.08)).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal shipping = BigDecimal.ZERO; // Free shipping over $50
                BigDecimal total = subtotal.add(tax).add(shipping);

                sampleOrder.setSubtotal(subtotal);
                sampleOrder.setTax(tax);
                sampleOrder.setShippingFee(shipping);
                sampleOrder.setTotalAmount(total);

                OrderItem item1 = new OrderItem(sampleOrder, headphone, headphone.getName(), headphone.getSku(), headphone.getPrice(), 1, headphone.getPrice());
                OrderItem item2 = new OrderItem(sampleOrder, book, book.getName(), book.getSku(), book.getPrice(), 1, book.getPrice());

                sampleOrder.addItem(item1);
                sampleOrder.addItem(item2);

                orderRepository.save(sampleOrder);
            }

            log.info("Database seeding completed successfully with 4 categories, 12 products, and demo data.");
        }
    }
}
