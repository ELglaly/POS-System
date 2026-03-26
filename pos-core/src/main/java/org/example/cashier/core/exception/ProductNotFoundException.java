package org.example.cashier.core.exception;

/**
 * Thrown when a product lookup by ID or SKU yields no result.
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Product not found: id=" + id);
    }

    public ProductNotFoundException(String sku) {
        super("Product not found: sku=" + sku);
    }
}
