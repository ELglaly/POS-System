package org.example.cashier.services;

import org.example.cashier.core.entity.Product;

import java.util.List;
import java.util.Optional;

/**
 * Product lookup, search, CRUD, SKU generation, and barcode label generation.
 */
public interface ProductService {
    Optional<Product> findBySku(String sku);
    List<Product> findAll();
    List<Product> search(String query);
    Product save(Product product);
    void delete(Long productId);
    String generateSku();
    byte[] getBarcodeLabel(Product product);
}
