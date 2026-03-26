package org.example.cashier.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Core product entity.  Products are read-heavy (every scan hits this table).
 * L2 caching uses standard JPA @Cacheable; the Hibernate cache region is
 * configured in application.properties (hibernate.cache.*).
 *
 * Barcode value format: {@code POS-{categoryCode}-{productId}-{checkDigit}}
 * Example: {@code POS-FD-42-7}
 */
@Entity
@Cacheable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Stock-Keeping Unit — unique human-entered code such as "APPLE-001".
     * This is different from the generated barcode value.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    /** Retail selling price (inclusive of tax where applicable). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Cost / wholesale price — used for margin calculations. */
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private int stockQuantity = 0;

    /** When stock falls at or below this value, a low-stock alert is raised. */
    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private int lowStockThreshold = 10;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** Filesystem path relative to the data directory (e.g. "images/apple.jpg"). */
    @Column(name = "image_path", length = 255)
    private String imagePath;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** Computed stock status — used in UI without extra queries. */
    @Transient
    public StockStatus getStockStatus() {
        if (stockQuantity <= 0)              return StockStatus.OUT_OF_STOCK;
        if (stockQuantity <= lowStockThreshold) return StockStatus.LOW;
        return StockStatus.IN_STOCK;
    }

    public enum StockStatus { IN_STOCK, LOW, OUT_OF_STOCK }

    @Override
    public String toString() { return name + " [" + sku + "]"; }
}