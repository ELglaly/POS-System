package org.example.cashier.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.cashier.core.enums.BarcodeType;

import java.time.LocalDateTime;

/**
 * Represents a generated barcode record linked to a product.
 *
 * A product may have multiple barcodes (e.g. internal CODE128 + external EAN-13).
 * Only one barcode per product per type should be active at any time — enforced
 * via the unique index on (product_id, barcode_type) for active records.
 *
 * Barcode value format for POS-internal codes:
 *   {@code POS-{categoryCode}-{productId}-{checkDigit}}
 *   Example: {@code POS-FD-42-7}
 */
@Entity
@Table(name = "barcodes", indexes = {
        @Index(name = "idx_barcode_value",  columnList = "barcode_value", unique = true),
        @Index(name = "idx_barcode_product",columnList = "product_id"),
        @Index(name = "idx_barcode_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Barcode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * The string value encoded inside the barcode image.
     * For EAN-13 this is the 12-digit numeric string (ZXing appends check digit).
     */
    @Column(name = "barcode_value", nullable = false, unique = true, length = 60)
    private String barcodeValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "barcode_type", nullable = false, length = 10)
    private BarcodeType barcodeType;

    /** Username of the cashier/admin who generated this barcode. */
    @Column(name = "generated_by", nullable = false, length = 60)
    private String generatedBy;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    /**
     * Only one barcode per product/type pair should be active.
     * Deactivating old barcodes on regeneration preserves audit history.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @PrePersist
    void onCreate() {
        if (generatedAt == null) generatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return barcodeType + ":" + barcodeValue;
    }
}