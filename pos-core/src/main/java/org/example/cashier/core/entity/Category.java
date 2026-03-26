package org.example.cashier.core.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Product category.  The {@code code} field is a short alphanumeric identifier
 * (e.g. "FD" for Food, "BV" for Beverages) used as part of the barcode value
 * format: {@code POS-{categoryCode}-{productId}-{checkDigit}}.
 */
@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_category_code", columnList = "code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable name shown in the UI. */
    @Column(nullable = false, unique = true, length = 80)
    private String name;

    /**
     * Short uppercase code (max 4 chars) embedded in product barcode values.
     * E.g. "FD", "BV", "EL", "HC".
     */
    @Column(nullable = false, unique = true, length = 4)
    private String code;

    @Column(length = 255)
    private String description;

    /** Display colour for UI category pills (hex, e.g. #00C896). */
    @Column(name = "ui_color", length = 7)
    @Builder.Default
    private String uiColor = "#3D8EFF";

    @Override
    public String toString() { return name + " [" + code + "]"; }
}