package org.example.cashier.services;

import lombok.RequiredArgsConstructor;
import org.example.cashier.core.entity.Product;
import org.example.cashier.data.repository.ProductRepository;
import org.example.cashier.reports.barcode.ZXingBarcodeEngine;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CashierProductService {

    private final ProductRepository productRepository;
    private final ZXingBarcodeEngine barcodeEngine;

    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySkuAndActiveTrue(sku);
    }

    public List<Product> findAll() {
        return productRepository.findAllByActiveTrueOrderByName();
    }

    public List<Product> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        return productRepository.searchActive(query);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void delete(Long productId) {
        productRepository.deleteById(productId);
    }

    /**
     * Generate a unique SKU.
     * Format: P-XXXXXX where X is a random alphanumeric character.
     * Retries until the SKU does not conflict with an existing one.
     */
    public String generateSku() {
        String sku;
        do {
            String random = java.util.UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 6).toUpperCase();
            sku = "P-" + random;
        } while (productRepository.findBySkuAndActiveTrue(sku).isPresent());
        return sku;
    }
    public byte[] getBarcodeLabel(Product product) {
        return barcodeEngine.generateProductLabel(product.getSku(), product.getName());
    }
}
