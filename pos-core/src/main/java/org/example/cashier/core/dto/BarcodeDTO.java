package org.example.cashier.core.dto;

import lombok.*;
import org.example.cashier.core.enums.BarcodeType;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarcodeDTO {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String barcodeValue;
    private BarcodeType barcodeType;
    private String generatedBy;
    private LocalDateTime generatedAt;
    private boolean active;
    private transient byte[] renderedImagePng;
}
