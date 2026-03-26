package org.example.cashier.services;

import org.example.cashier.core.entity.Product;

import java.util.List;
import java.util.Optional;

/**
 * Product lookup, search, CRUD, SKU generation, and barcode label generation.
 */
public interface ProductService {

    /** Find an active product by its exact SKU — primary scan lookup. */
    Optional<Product> findBySku(String sku);

    /** All active products sorted by name. */
    List<Product> findAll();

    /**
     * Search active products by name or SKU fragment.
     * Returns all products when {@code query} is null or blank.
     */
    List<Product> search(String query);

    Product save(Product product);

    void delete(Long productId);

    /**
     * Generate a unique SKU in the format {@code P-XXXXXX}.
     * Retries until no conflict with existing active SKUs.
     */
    String generateSku();

    /**
     * Generate a CODE128 product label PNG: barcode + name + SKU.
     * The label is produced on-the-fly and never persisted.
     */
    byte[] getBarcodeLabel(Product product);
}
