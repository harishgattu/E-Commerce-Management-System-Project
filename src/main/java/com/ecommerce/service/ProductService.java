package com.ecommerce.service;

import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductStatus;

import java.util.List;

public interface ProductService {
    List<ProductResponse> getAllProducts(Long categoryId, ProductStatus status, String keyword);
    ProductResponse getProductById(Long id);
    Product getProductEntityById(Long id);
    ProductResponse getProductBySku(String sku);
    ProductResponse createProduct(ProductRequest request, String performedBy);
    ProductResponse updateProduct(Long id, ProductRequest request, String performedBy);
    void deleteProduct(Long id, String performedBy);
    List<ProductResponse> getLowStockProducts(Integer threshold);
}
