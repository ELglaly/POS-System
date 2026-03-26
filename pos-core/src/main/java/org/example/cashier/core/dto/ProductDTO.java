package org.example.cashier.core.dto;

import lombok.*;
import org.example.cashier.core.entity.Product;

import java.math.BigDecimal;

/**
 * Lightweight product projection used in barcode generation and UI autocomplete.
 * Only carries fields needed for barcode label rendering — not the full entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;
    private String sku;
    private String name;
    private BigDecimal price;
    private String categoryName;
    private String categoryCode;
    private int stockQuantity;
    private Product.StockStatus stockStatus;
    private String imagePath;
}
