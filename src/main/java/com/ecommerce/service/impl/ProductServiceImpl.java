package com.ecommerce.service.impl;

import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.InventoryAudit;
import com.ecommerce.entity.MovementType;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductStatus;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.InventoryAuditRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryAuditRepository inventoryAuditRepository;

    @Value("${ecommerce.low-stock-threshold:10}")
    private int lowStockThreshold;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              InventoryAuditRepository inventoryAuditRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryAuditRepository = inventoryAuditRepository;
    }

    @Override
    public List<ProductResponse> getAllProducts(Long categoryId, ProductStatus status, String keyword) {
        List<Product> products = productRepository.filterProducts(
                categoryId,
                status,
                (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null
        );
        return products.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return mapToResponse(getProductEntityById(id));
    }

    @Override
    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    @Override
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request, String performedBy) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with SKU '" + request.getSku() + "' already exists.");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
        }

        Product product = new Product(
                request.getSku().toUpperCase().trim(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity() != null ? request.getStockQuantity() : 0,
                request.getStatus(),
                request.getImageUrl(),
                category
        );

        Product savedProduct = productRepository.save(product);

        // Record initial inventory movement
        if (savedProduct.getStockQuantity() > 0) {
            InventoryAudit audit = new InventoryAudit(
                    savedProduct,
                    MovementType.INITIAL_SEED,
                    savedProduct.getStockQuantity(),
                    0,
                    savedProduct.getStockQuantity(),
                    "Product catalog creation",
                    performedBy != null ? performedBy : "ADMIN"
            );
            inventoryAuditRepository.save(audit);
        }

        return mapToResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, String performedBy) {
        Product product = getProductEntityById(id);

        if (!product.getSku().equalsIgnoreCase(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with SKU '" + request.getSku() + "' already exists.");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
        }

        int previousStock = product.getStockQuantity();
        product.setSku(request.getSku().toUpperCase().trim());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setStatus(request.getStatus());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);

        // If stock changed directly in product update, log manual adjustment audit
        if (previousStock != updatedProduct.getStockQuantity()) {
            int diff = updatedProduct.getStockQuantity() - previousStock;
            InventoryAudit audit = new InventoryAudit(
                    updatedProduct,
                    MovementType.MANUAL_ADJUSTMENT,
                    diff,
                    previousStock,
                    updatedProduct.getStockQuantity(),
                    "Product details modification",
                    performedBy != null ? performedBy : "ADMIN"
            );
            inventoryAuditRepository.save(audit);
        }

        return mapToResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, String performedBy) {
        Product product = getProductEntityById(id);
        // Soft delete / archive to preserve historic order integrity
        product.setStatus(ProductStatus.ARCHIVED);
        productRepository.save(product);
    }

    @Override
    public List<ProductResponse> getLowStockProducts(Integer threshold) {
        int th = threshold != null ? threshold : lowStockThreshold;
        return productRepository.findByStockQuantityLessThanEqual(th).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.getImageUrl(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
