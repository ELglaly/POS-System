package org.example.cashier.core.dto;

import lombok.*;
import org.example.cashier.core.entity.Product;

import java.math.BigDecimal;

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
